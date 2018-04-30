package com.nickimpact.daycare;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.nickimpact.daycare.api.text.Tokens;
import com.nickimpact.daycare.commands.DaycareCmd;
import com.nickimpact.daycare.configuration.ConfigKeys;
import com.nickimpact.daycare.configuration.MsgConfigKeys;
import com.nickimpact.daycare.internal.TextParsingUtils;
import com.nickimpact.daycare.listeners.ConnectionListener;
import com.nickimpact.daycare.listeners.NPCListener;
import com.nickimpact.daycare.ranch.DaycareNPC;
import com.nickimpact.daycare.ranch.Ranch;
import com.nickimpact.daycare.storage.Storage;
import com.nickimpact.daycare.storage.StorageFactory;
import com.nickimpact.daycare.storage.StorageType;
import com.nickimpact.daycare.utils.DaycareRunningTasks;
import com.nickimpact.daycare.utils.MessageUtils;
import com.nickimpact.impactor.CoreInfo;
import com.nickimpact.impactor.api.configuration.AbstractConfig;
import com.nickimpact.impactor.api.configuration.AbstractConfigAdapter;
import com.nickimpact.impactor.api.configuration.ConfigBase;
import com.nickimpact.impactor.api.configuration.ConfigKey;
import com.nickimpact.impactor.api.logger.Logger;
import com.nickimpact.impactor.api.plugins.ConfigurableSpongePlugin;
import com.nickimpact.impactor.api.plugins.PluginInfo;
import com.nickimpact.impactor.logging.ConsoleLogger;
import com.nickimpact.impactor.logging.SpongeLogger;
import lombok.Getter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.AsynchronousExecutor;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.*;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@Getter
@Plugin(id = DaycareInfo.ID, name = DaycareInfo.NAME, version = DaycareInfo.VERSION, description = DaycareInfo.DESCRIPTION, dependencies = @Dependency(id = CoreInfo.ID))
public class DaycarePlugin extends ConfigurableSpongePlugin {

	@Getter private static DaycarePlugin instance;

	@Inject private PluginContainer pluginContainer;

	private Logger logger;
	@Inject private org.slf4j.Logger fallback;

	/** The pathing to the config directory for GTS */
	@Inject @ConfigDir(sharedRoot = false) private Path configDir;

	@Inject @AsynchronousExecutor private SpongeExecutorService asyncExecutorService;

	/** Used to keep track of any potential start up issues which will prevent the plugin from initializing properly */
	private Throwable error = null;

	/** The economy service present on the server */
	private EconomyService economy;

	/** Used and held for quick access of the user storage service registry */
	private UserStorageService userStorageService;

	public static final Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();

	private ConfigBase config;
	private ConfigBase msgConfig;

	private Storage storage;

	private List<Ranch> ranches = Lists.newArrayList();
	private List<DaycareNPC> npcs = Lists.newArrayList();

	/** An internal provider set to help decode variables in strings */
	private TextParsingUtils textParsingUtils = new TextParsingUtils();

	@Listener
	public void onInit(GameInitializationEvent e) {
		instance = this;
		this.logger = new ConsoleLogger(this, new SpongeLogger(this, fallback));
		DaycareInfo.startup();

		try {
			this.logger.info(Text.of(TextColors.GRAY, "Now entering the init phase..."));
			this.logger.info(Text.of(TextColors.GRAY, "Loading configuration..."));
			this.config = new AbstractConfig(this, new AbstractConfigAdapter(this), new ConfigKeys(), "daycare.conf");
			this.config.init();
			this.msgConfig = new AbstractConfig(this, new AbstractConfigAdapter(this), new MsgConfigKeys(), String.format("lang/%s.conf", this.config.get(ConfigKeys.LANG)));
			this.msgConfig.init();

			this.logger.info(MessageUtils.fetchMsg(MsgConfigKeys.STARTUP_INIT_NUCLEUS));
			new Tokens();

			this.logger.info(MessageUtils.fetchMsg(MsgConfigKeys.STARTUP_INIT_COMMANDS));
			new DaycareCmd(this).register(this);

			this.logger.info(MessageUtils.fetchMsg(MsgConfigKeys.STARTUP_INIT_LISTENERS));
			Sponge.getEventManager().registerListeners(this, new ConnectionListener());
			Sponge.getEventManager().registerListeners(this, new NPCListener());

			this.logger.info(MessageUtils.fetchMsg(MsgConfigKeys.STARTUP_INIT_STORAGE));
			this.storage = StorageFactory.getInstance(this, StorageType.H2);
			try {
				this.npcs = this.storage.getNPCS().get();
			} catch (InterruptedException | ExecutionException e1) {
				e1.printStackTrace();
			}

			this.logger.info(MessageUtils.fetchMsg(MsgConfigKeys.STARTUP_INIT_COMPLETE));
			connect();
		} catch (Exception exc) {
			this.error = exc;
			disable();
			exc.printStackTrace();
		}
	}

	@Listener
	public void onStart(GameStartedServerEvent e) {
		if(!Sponge.getServiceManager().isRegistered(EconomyService.class)) {
			List<Text> error = Lists.newArrayList();
			addTri(error);
			error.addAll(MessageUtils.fetchMsgs(MsgConfigKeys.STARTUP_NO_ECONOMY_SERVICE));
			this.getLogger().send(Logger.Prefixes.NONE, error);
			this.disconnect();
			return;
		}

		this.logger.info(MessageUtils.fetchMsg(MsgConfigKeys.STARTUP_STARTED_PHASE));
		this.logger.info(MessageUtils.fetchMsg(MsgConfigKeys.STARTUP_STARTED_TASKS));

		DaycareRunningTasks.runLvlTask();
		DaycareRunningTasks.runBreedingTask();
		this.logger.info(MessageUtils.fetchMsg(MsgConfigKeys.STARTUP_STARTED_COMPLETE));
		this.logger.info(MessageUtils.fetchMsg(MsgConfigKeys.STARTUP_COMPLETE));
	}

	@Listener
	public void onStop(GameStoppingServerEvent e) {
		disconnect();
	}

	@Listener
	public void registerServices(ChangeServiceProviderEvent e){
		if(e.getService().equals(EconomyService.class))
			this.economy = (EconomyService) e.getNewProviderRegistration().getProvider();
		else if(e.getService().equals(UserStorageService.class))
			this.userStorageService = (UserStorageService) e.getNewProviderRegistration().getProvider();
	}

	@Listener
	public void reload(GameReloadEvent e) {
		// Reload configuration here
	}

	public String getID() {
		return DaycareInfo.ID;
	}

	public String getName() {
		return DaycareInfo.NAME;
	}

	public String getVersion() {
		return DaycareInfo.VERSION;
	}

	@Override
	public PluginInfo getPluginInfo() {
		return new DaycareInfo();
	}

	@Override
	public void doConnect() {

	}

	@Override
	public void doDisconnect() {
		for(Ranch ranch : this.getRanches()) {
			this.getStorage().updateRanch(ranch);
		}

		try {
			this.storage.shutdown();
		} catch (Exception e) {
			this.getLogger().error("Unable to shutdown database properly...");
			e.printStackTrace();
		}
	}

	@Override
	public Logger getLogger() {
		return this.logger;
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

	public void addNPC(DaycareNPC npc) {
		this.npcs.add(npc);
		this.storage.addNPC(npc);
	}

	public void deleteNPC(DaycareNPC npc) {
		this.npcs.remove(npc);
		this.storage.deleteNPC(npc);
	}

	private void addTri(List<Text> messages) {
		messages.add(Text.of(TextColors.YELLOW, "        /\\"));
		messages.add(Text.of(TextColors.YELLOW, "       /  \\"));
		messages.add(Text.of(TextColors.YELLOW, "      / || \\"));
		messages.add(Text.of(TextColors.YELLOW, "     /  ||  \\"));
		messages.add(Text.of(TextColors.YELLOW, "    /   ||   \\"));
		messages.add(Text.of(TextColors.YELLOW, "   /    ||    \\"));
		messages.add(Text.of(TextColors.YELLOW, "  /            \\"));
		messages.add(Text.of(TextColors.YELLOW, " /      **      \\"));
		messages.add(Text.of(TextColors.YELLOW, "------------------"));
	}

	private void disable() {
		// Disable everything, just in case. Thanks to pie-flavor: https://forums.spongepowered.org/t/disable-plugin-disable-itself/15831/8
		Sponge.getEventManager().unregisterPluginListeners(this);
		Sponge.getCommandManager().getOwnedBy(this).forEach(Sponge.getCommandManager()::removeMapping);
		Sponge.getScheduler().getScheduledTasks(this).forEach(Task::cancel);

		// Re-register this to warn people about the error.
		Sponge.getEventManager().registerListener(this, GameStartedServerEvent.class, e -> errorOnStartup());
	}

	private void errorOnStartup() {
		Sponge.getServer().getConsole().sendMessages(getErrorMessage());
	}

	private List<Text> getErrorMessage() {
		List<Text> error = Lists.newArrayList();
		error.add(Text.of(TextColors.RED, "----------------------------"));
		error.add(Text.of(TextColors.RED, "-  DAYCARE FAILED TO LOAD  -"));
		error.add(Text.of(TextColors.RED, "----------------------------"));
		error.add(Text.EMPTY);
		error.add(Text.of(TextColors.RED, "Daycare encountered an error which prevented startup to succeed. All commands, listeners, and tasks have been halted..."));
		error.add(Text.of(TextColors.RED, "----------------------------"));
		if(this.error != null) {
			if(this.error instanceof IOException) {
				error.add(Text.of(TextColors.RED, "It appears that there is an error in your configuration file! The error is: "));
				error.add(Text.of(TextColors.RED, this.error.getMessage()));
				error.add(Text.of(TextColors.RED, "Please correct this and restart your server."));
				error.add(Text.of(TextColors.YELLOW, "----------------------------"));
			} else if(this.error instanceof SQLException) {
				error.add(Text.of(TextColors.RED, "It appears that there is an error with the Daycare storage provider! The error is: "));
				error.add(Text.of(TextColors.RED, this.error.getMessage()));
				error.add(Text.of(TextColors.RED, "Please correct this and restart your server."));
				error.add(Text.of(TextColors.YELLOW, "----------------------------"));
			}

			error.add(Text.of(TextColors.YELLOW, "(The error that was thrown is shown below)"));

			try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
				this.error.printStackTrace(pw);
				pw.flush();
				String[] stackTrace = sw.toString().split("(\r)?\n");
				for (String s : stackTrace) {
					error.add(Text.of(TextColors.YELLOW, s));
				}
			} catch (IOException e) {
				this.error.printStackTrace();
			}
		}

		return error;
	}
}
