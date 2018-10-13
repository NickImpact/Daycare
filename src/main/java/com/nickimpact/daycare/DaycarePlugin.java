package com.nickimpact.daycare;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.nickimpact.daycare.api.DaycareService;
import com.nickimpact.daycare.api.breeding.BreedStyle;
import com.nickimpact.daycare.api.text.Tokens;
import com.nickimpact.daycare.commands.DaycareCmd;
import com.nickimpact.daycare.configuration.ConfigKeys;
import com.nickimpact.daycare.configuration.MsgConfigKeys;
import com.nickimpact.daycare.impl.DaycareServiceImpl;
import com.nickimpact.daycare.internal.TextParsingUtils;
import com.nickimpact.daycare.listeners.ConnectionListener;
import com.nickimpact.daycare.listeners.NPCListener;
import com.nickimpact.daycare.ranch.DaycareNPC;
import com.nickimpact.daycare.ranch.Ranch;
import com.nickimpact.daycare.ranch.breeding.BreedStyleInstanceAdapter;
import com.nickimpact.daycare.ranch.breeding.BreedStylePixelmonNative;
import com.nickimpact.daycare.ranch.breeding.BreedStyleTimed;
import com.nickimpact.daycare.storage.Storage;
import com.nickimpact.daycare.storage.StorageFactory;
import com.nickimpact.daycare.storage.StorageType;
import com.nickimpact.daycare.utils.DaycareRunningTasks;
import com.nickimpact.daycare.utils.MessageUtils;
import com.nickimpact.impactor.CoreInfo;
import com.nickimpact.impactor.api.commands.SpongeCommand;
import com.nickimpact.impactor.api.configuration.AbstractConfig;
import com.nickimpact.impactor.api.configuration.AbstractConfigAdapter;
import com.nickimpact.impactor.api.configuration.ConfigBase;
import com.nickimpact.impactor.api.logger.Logger;
import com.nickimpact.impactor.api.plugins.PluginInfo;
import com.nickimpact.impactor.api.plugins.SpongePlugin;
import com.nickimpact.impactor.api.services.plan.PlanData;
import com.nickimpact.impactor.logging.ConsoleLogger;
import com.nickimpact.impactor.logging.SpongeLogger;
import lombok.Getter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@Getter
@Plugin(id = DaycareInfo.ID, name = DaycareInfo.NAME, version = DaycareInfo.VERSION, description = DaycareInfo.DESCRIPTION, dependencies = @Dependency(id = CoreInfo.ID))
public class DaycarePlugin extends SpongePlugin {

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

	public static final Gson prettyGson = new GsonBuilder()
			.setPrettyPrinting()
			.registerTypeAdapter(BreedStyle.Instance.class, new BreedStyleInstanceAdapter())
			.create();

	private ConfigBase config;
	private ConfigBase msgConfig;

	private DaycareCmd command;

	private ConnectionListener cListener;
	private NPCListener npcListener;

	private Storage storage;

	private List<Ranch> ranches = Lists.newArrayList();
	private List<DaycareNPC> npcs = Lists.newArrayList();

	/** An internal provider set to help decode variables in strings */
	private TextParsingUtils textParsingUtils = new TextParsingUtils();

	private DaycareService service = new DaycareServiceImpl();

	private BreedStyle breedStyle;

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
	public Logger getLogger() {
		return this.logger;
	}

	@Override
	public Optional<PlanData> getPlanData() {
		return Optional.empty();
	}

	@Override
	public List<ConfigBase> getConfigs() {
		return Lists.newArrayList(config, msgConfig);
	}

	@Override
	public List<SpongeCommand> getCommands() {
		return Collections.singletonList(command);
	}

	@Override
	public List<Object> getListeners() {
		return Lists.newArrayList();
	}

	@Listener
	public void onPreInit(GamePreInitializationEvent e) {
		instance = this;
		Sponge.getServiceManager().setProvider(this, DaycareService.class, service);
		this.service.getBreedStyleRegistry().register(Lists.newArrayList(
				new BreedStylePixelmonNative(),
				new BreedStyleTimed()
		));
		try {
			this.service.getBreedStyleRegistry().getInstanceRegistry().register(BreedStylePixelmonNative.BSPNInstance.class);
			this.service.getBreedStyleRegistry().getInstanceRegistry().register(BreedStyleTimed.BSTInstance.class);
		} catch (Exception ignored) {}
	}

	@Listener
	public void onInit(GameInitializationEvent e) {
		this.logger = new ConsoleLogger(this, new SpongeLogger(this, fallback));
		DaycareInfo.startup();

		try {
			if(!validVersion()) {
				throw new InvalidPixelmonException();
			}

			this.logger.info(Text.of(TextColors.GRAY, "Now entering the init phase..."));
			this.logger.info(Text.of(TextColors.GRAY, "Loading configuration..."));
			this.config = new AbstractConfig(this, new AbstractConfigAdapter(this), new ConfigKeys(), "daycare.conf");
			this.config.init();
			this.msgConfig = new AbstractConfig(this, new AbstractConfigAdapter(this), new MsgConfigKeys(), String.format("lang/%s.conf", this.config.get(ConfigKeys.LANG)));
			this.msgConfig.init();

			this.logger.info(MessageUtils.fetchMsg(MsgConfigKeys.STARTUP_INIT_NUCLEUS));
			new Tokens();

			this.logger.info(MessageUtils.fetchMsg(MsgConfigKeys.STARTUP_INIT_COMMANDS));
			(command = new DaycareCmd(this)).register(this);

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

			this.breedStyle = this.service.getBreedStyleRegistry().getFromName(this.config.get(ConfigKeys.BREED_STYLE)).orElse(this.service.getBreedStyleRegistry().getFirst());

			this.logger.info(MessageUtils.fetchMsg(MsgConfigKeys.STARTUP_INIT_COMPLETE));
		} catch (Exception exc) {
			this.error = exc;
			disable();
			exc.printStackTrace();
		}
	}

	@Listener
	public void onServerStarted(GameStartedServerEvent e) {
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
		this.logger.info(MessageUtils.fetchMsg(MsgConfigKeys.STARTUP_STARTED_COMPLETE));
		this.logger.info(MessageUtils.fetchMsg(MsgConfigKeys.STARTUP_COMPLETE));

		Sponge.getScheduler().createTaskBuilder()
				.execute(() -> this.getStorage().updateAll(this.getRanches()))
				.interval(this.config.get(ConfigKeys.GENERAL_UPDATE_PERIOD), TimeUnit.MINUTES)
				.submit(this);
	}

	@Listener
	public void onServerStopping(GameStoppingServerEvent e) {
		this.onDisconnect();
	}

	@Override
	public void onDisconnect() {
		this.getStorage().updateAll(this.getRanches()).thenAccept(after -> {
			try {
				this.getRanches().forEach(Ranch::shutdown);
				this.storage.shutdown();
			} catch (Exception e1) {
				this.getLogger().error("Unable to shutdown database properly...");
				e1.printStackTrace();
			}
		});
	}

	@Override
	public void onReload() {
		DaycareRunningTasks.runLvlTask();
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
			} else if(this.error instanceof InvalidPixelmonException) {
				error.add(Text.of(TextColors.RED, "Daycare detected an incompatible version of Pixelmon..."));
				error.add(Text.of(TextColors.RED, "Either you are using Pixelmon 6.2.3 and below, or you"));
				error.add(Text.of(TextColors.RED, "are using Pixelmon Generations. As such, Daycare was unable to start."));
				error.add(Text.of(TextColors.YELLOW, "----------------------------"));

				return error;
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

	public static boolean validVersion() {
		String version = Sponge.getPluginManager().getPlugin("pixelmon").get().getVersion().get();
		String[] identifiers = version.split("\\.");
		int major = Integer.parseInt(identifiers[0]);
		int minor = Integer.parseInt(identifiers[1]);
		// We really don't care about the technical patch version, but we will store it for potential use later on
		int patch = Integer.parseInt(identifiers[2]);

		if(major == 6) {
			return minor >= 3;
		}

		return major >= 7;
	}

	private class InvalidPixelmonException extends Exception {}
}
