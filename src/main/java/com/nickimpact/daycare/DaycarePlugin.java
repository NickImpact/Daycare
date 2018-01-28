package com.nickimpact.daycare;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.nickimpact.daycare.api.configuration.AbstractConfig;
import com.nickimpact.daycare.api.configuration.ConfigBase;
import com.nickimpact.daycare.commands.DaycareCmd;
import com.nickimpact.daycare.configuration.DaycareConfigAdapter;
import com.nickimpact.daycare.listeners.ConnectionListener;
import com.nickimpact.daycare.ranch.Ranch;
import com.nickimpact.daycare.storage.Storage;
import com.nickimpact.daycare.storage.StorageFactory;
import com.nickimpact.daycare.storage.StorageType;
import com.nickimpact.daycare.storage.dao.file.FileWatcher;
import com.nickimpact.daycare.utils.DaycareRunningTasks;
import lombok.Getter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.AsynchronousExecutor;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@Getter
@Plugin(id = PluginInfo.ID, name = PluginInfo.NAME, version = PluginInfo.VERSION, description = PluginInfo.DESCRIPTION)
public class DaycarePlugin {

	@Getter private static DaycarePlugin instance;

	@Inject private PluginContainer pluginContainer;

	/** The pathing to the config directory for GTS */
	@Inject @ConfigDir(sharedRoot = false) private Path configDir;

	@Inject @AsynchronousExecutor private SpongeExecutorService asyncExecutorService;

	/** The economy service present on the server */
	private EconomyService economy;

	/** Used and held for quick access of the user storage service registry */
	private UserStorageService userStorageService;

	public static final Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();

	private ConfigBase config;
	private ConfigBase msgConfig;

	private Storage storage;
	private FileWatcher fileWatcher;

	private List<Ranch> ranches = Lists.newArrayList();

	@Listener
	public void onInit(GameInitializationEvent e) {
		instance = this;
		PluginInfo.startup();

		getConsole().ifPresent(console -> console.sendMessages(Text.of(PluginInfo.PREFIX, "Now entering the init phase")));
		getConsole().ifPresent(console -> console.sendMessages(Text.of(PluginInfo.PREFIX, "Loading configuration...")));
		this.config = new AbstractConfig(this, new DaycareConfigAdapter(this), "assets/daycare.conf");
		this.config.init();
		this.msgConfig = new AbstractConfig(this, new DaycareConfigAdapter(this), "assets/messages.conf");
		this.msgConfig.init();

		getConsole().ifPresent(console -> console.sendMessages(Text.of(PluginInfo.PREFIX, "Initializing commands...")));
		new DaycareCmd().register();

		getConsole().ifPresent(console -> console.sendMessages(Text.of(PluginInfo.PREFIX, "Registering listeners...")));
		Sponge.getEventManager().registerListeners(this, new ConnectionListener());

		getConsole().ifPresent(console -> console.sendMessages(Text.of(PluginInfo.PREFIX, "Loading data storage...")));
		this.storage = StorageFactory.getInstance(this, StorageType.H2);

		getConsole().ifPresent(console -> console.sendMessages(Text.of(PluginInfo.PREFIX, "Retrieving data from storage provider...")));
		try {
			this.ranches = this.storage.getAllRanches().get();
		} catch (InterruptedException | ExecutionException e1) {
			e1.printStackTrace();
		}

		getConsole().ifPresent(console -> console.sendMessages(Text.of(PluginInfo.PREFIX, "Initialization complete!")));
	}

	@Listener
	public void onStart(GameStartedServerEvent e) {
		getConsole().ifPresent(console -> console.sendMessages(Text.of(PluginInfo.PREFIX, "Now entering the startup phase")));
		getConsole().ifPresent(console -> console.sendMessages(Text.of(PluginInfo.PREFIX, "Initializing tasks...")));

		DaycareRunningTasks.runLvlTask();
		DaycareRunningTasks.runBreedingTask();
		getConsole().ifPresent(console -> console.sendMessages(Text.of(PluginInfo.PREFIX, "Startup procedures complete!")));
		getConsole().ifPresent(console -> console.sendMessages(Text.of(PluginInfo.PREFIX, "Daycare is now fully initialized!")));

	}

	@Listener
	public void registerServices(ChangeServiceProviderEvent e){
		if(e.getService().equals(EconomyService.class))
			this.economy = (EconomyService) e.getNewProviderRegistration().getProvider();
		else if(e.getService().equals(UserStorageService.class))
			this.userStorageService = (UserStorageService) e.getNewProviderRegistration().getProvider();
	}

	public Optional<ConsoleSource> getConsole() {
		return Optional.ofNullable(Sponge.isServerAvailable() ? Sponge.getServer().getConsole() : null);
	}

	public File getDataDirectory() {
		File root = configDir.toFile().getParentFile().getParentFile();
		File gtsDir = new File(root, "daycare");
		gtsDir.mkdirs();
		return gtsDir;
	}

	public InputStream getResourceStream(String path) {
		return getClass().getClassLoader().getResourceAsStream(path);
	}

	public Optional<FileWatcher> getFileWatcher() {
		return Optional.ofNullable(this.fileWatcher);
	}

}
