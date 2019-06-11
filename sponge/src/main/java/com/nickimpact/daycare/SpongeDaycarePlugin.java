package com.nickimpact.daycare;

import co.aikar.commands.SpongeCommandManager;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.nickimpact.daycare.api.configuration.ConfigKeys;
import com.nickimpact.daycare.api.dependencies.DependencyManager;
import com.nickimpact.daycare.api.dependencies.classloader.PluginClassLoader;
import com.nickimpact.daycare.api.dependencies.classloader.ReflectionClassLoader;
import com.nickimpact.daycare.api.pens.DaycarePokemonWrapper;
import com.nickimpact.daycare.api.pens.Pen;
import com.nickimpact.daycare.api.pens.Ranch;
import com.nickimpact.daycare.api.util.PluginInstance;
import com.nickimpact.daycare.commands.DaycareCmd;
import com.nickimpact.daycare.common.json.PenAdapter;
import com.nickimpact.daycare.common.json.PokemonWrapperAdapter;
import com.nickimpact.daycare.common.json.RanchAdapter;
import com.nickimpact.daycare.common.plugin.DaycarePlugin;
import com.nickimpact.daycare.common.storage.StorageFactory;
import com.nickimpact.daycare.configuration.MsgConfigKeys;
import com.nickimpact.daycare.implementation.SpongeDaycarePokemonWrapper;
import com.nickimpact.daycare.implementation.SpongeDaycareService;
import com.nickimpact.daycare.implementation.SpongePen;
import com.nickimpact.daycare.implementation.SpongeRanch;
import com.nickimpact.daycare.listeners.JoinListener;
import com.nickimpact.daycare.pixelmon.PokemonTokens;
import com.nickimpact.daycare.provided.EconomicModule;
import com.nickimpact.daycare.tasks.DaycareRunningTasks;
import com.nickimpact.daycare.text.DaycareTokens;
import com.nickimpact.daycare.text.TextParsingUtils;
import com.nickimpact.impactor.api.commands.Command;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.api.logging.Logger;
import com.nickimpact.impactor.api.platform.Platform;
import com.nickimpact.impactor.api.plugin.ImpactorPlugin;
import com.nickimpact.impactor.api.plugin.PluginInfo;
import com.nickimpact.impactor.api.storage.StorageType;
import com.nickimpact.impactor.sponge.configuration.SpongeConfig;
import com.nickimpact.impactor.sponge.configuration.SpongeConfigAdapter;
import com.nickimpact.impactor.sponge.logging.SpongeLogger;
import io.github.nucleuspowered.nucleus.api.service.NucleusMessageTokenService;
import lombok.Getter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;

import java.io.File;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

@Getter
@Plugin(id = "daycare", name = "Daycare", version = "2.0.0-DEV", dependencies = {@Dependency(id = "impactor"), @Dependency(id = "nucleus")})
public class SpongeDaycarePlugin extends DaycarePlugin {

	@Getter private static SpongeDaycarePlugin spongeInstance;

	@Inject
	@Getter
	private PluginContainer pluginContainer;

	@Inject
	private org.slf4j.Logger fallback;

	@Inject
	@ConfigDir(sharedRoot = false)
	private Path configDir;
	private Config msgConfig;

	private EconomyService economy;
	private TextParsingUtils textParsingUtils;

	private SpongeCommandManager cmdManager;

	private PluginClassLoader loader;
	private DependencyManager dependencyManager;

	public SpongeDaycarePlugin() {
		spongeInstance = this;
		PluginInstance.setPlugin(this);
	}

	@Listener
	public void onPreInit(GamePreInitializationEvent event) {
		this.logger = new SpongeLogger(this, fallback);
		logger.info("Initializing Daycare...");
		this.service = new SpongeDaycareService();

		RanchAdapter r = new RanchAdapter(this);
		PenAdapter p = new PenAdapter(this);
		PokemonWrapperAdapter pwa = new PokemonWrapperAdapter(this);

		this.gson = new GsonBuilder().setPrettyPrinting()
				.registerTypeAdapter(Ranch.class, r)
				.registerTypeAdapter(Pen.class, p)
				.registerTypeAdapter(DaycarePokemonWrapper.class, pwa)
				.create();

		try {
			r.getRegistry().register(SpongeRanch.class);
			p.getRegistry().register(SpongePen.class);
			pwa.getRegistry().register(SpongeDaycarePokemonWrapper.class);
		} catch (Exception e) {
			this.getPluginLogger().error("Unable to register class typings into API Service...");
		}

		this.service.registerUnlocker("economic", new EconomicModule());
		this.service.getBuilderRegistry().register(Ranch.RanchBuilder.class, SpongeRanch.SpongeRanchBuilder.class);
		this.service.getBuilderRegistry().register(Pen.PenBuilder.class, SpongePen.SpongePenBuilder.class);

		logger.info("Registering tokens with Nucleus...");
		DaycareTokens tokens = new DaycareTokens(this);
		new PokemonTokens().getTokens().forEach(tokens::register);
	}

	@Listener
	public void onInit(GameInitializationEvent event) {
		logger.info("Loading configuration...");
		this.config = new SpongeConfig(new SpongeConfigAdapter(this, new File(this.configDir.toFile(), "daycare.conf")), new ConfigKeys());
		this.msgConfig = new SpongeConfig(new SpongeConfigAdapter(this, new File(this.configDir.toFile(), "lang/en_us.conf")), new MsgConfigKeys());

		this.service.setActiveModule(this.config.get(ConfigKeys.RANCH_UNLOCK_MODULE));

		logger.info("Initializing additional dependencies...");
		this.loader = new ReflectionClassLoader(this);
		this.dependencyManager = new DependencyManager(this);
		this.dependencyManager.loadDependencies(EnumSet.of(com.nickimpact.daycare.api.dependencies.Dependency.CONFIGURATE_CORE, com.nickimpact.daycare.api.dependencies.Dependency.CONFIGURATE_HOCON, com.nickimpact.daycare.api.dependencies.Dependency.HOCON_CONFIG, com.nickimpact.daycare.api.dependencies.Dependency.CONFIGURATE_GSON, com.nickimpact.daycare.api.dependencies.Dependency.CONFIGURATE_YAML));

		StorageType st = StorageType.parse(this.config.get(ConfigKeys.STORAGE_METHOD));
		this.dependencyManager.loadStorageDependencies(ImmutableSet.of(st != null ? st : StorageType.H2));

		logger.info("Registering commands with ACF...");
		this.cmdManager = new SpongeCommandManager(this.getPluginContainer());
		this.cmdManager.enableUnstableAPI("help");

		this.cmdManager.registerCommand(new DaycareCmd());

		Sponge.getEventManager().registerListeners(this, new JoinListener());

		logger.info("Initializing and reading storage...");
		((SpongeDaycareService) this.service).setStorage(new StorageFactory(this).getInstance(StorageType.JSON));
	}

	@Listener
	public void onServiceRegister(ChangeServiceProviderEvent e) {
		if(e.getNewProvider() instanceof EconomyService) {
			this.economy = (EconomyService) e.getNewProvider();
		} else if(e.getService().equals(NucleusMessageTokenService.class)) {
			this.textParsingUtils = new TextParsingUtils(this);
		}
	}

	@Listener
	public void onStart(GameStartedServerEvent event) {
		DaycareRunningTasks.breedingTask();
		DaycareRunningTasks.levelTask();
	}

	@Listener
	public void onDisconnect(GameStoppingServerEvent event) {

	}

	@Override
	public ScheduledExecutorService getAsyncExecutor() {
		return null;
	}

	@Override
	public PluginClassLoader getPluginClassLoader() {
		return this.loader;
	}

	@Override
	public DependencyManager getDependencyManager() {
		return this.dependencyManager;
	}

	@Override
	public Path getConfigDir() {
		return this.configDir;
	}

	@Override
	public Config getConfiguration() {
		return this.config;
	}

	@Override
	public Platform getPlatform() {
		return Platform.Sponge;
	}

	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo() {
			@Override
			public String getID() {
				return "daycare";
			}

			@Override
			public String getName() {
				return "Daycare";
			}

			@Override
			public String getVersion() {
				return "2.0.0";
			}

			@Override
			public String getDescription() {
				return "The pokemon daycare";
			}
		};
	}

	@Override
	public Logger getPluginLogger() {
		return this.logger;
	}

	@Override
	public List<Config> getConfigs() {
		return Lists.newArrayList();
	}

	@Override
	public List<Command> getCommands() {
		return Lists.newArrayList();
	}

	@Override
	public List<Object> getListeners() {
		return Lists.newArrayList();
	}

	@Override
	public Consumer<ImpactorPlugin> onReload() {
		return x -> {};
	}

	@Override
	public boolean isConnected() {
		return false;
	}

	@Override
	public void setConnected() {

	}

	@Override
	public void handleDisconnect() {

	}
}
