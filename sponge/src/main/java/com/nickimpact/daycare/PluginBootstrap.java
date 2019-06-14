package com.nickimpact.daycare;

import co.aikar.commands.SpongeCommandManager;
import com.google.common.collect.ImmutableSet;
import com.google.gson.GsonBuilder;
import com.nickimpact.daycare.api.configuration.ConfigKeys;
import com.nickimpact.daycare.api.dependencies.DependencyManager;
import com.nickimpact.daycare.api.dependencies.classloader.ReflectionClassLoader;
import com.nickimpact.daycare.api.pens.DaycarePokemonWrapper;
import com.nickimpact.daycare.api.pens.Pen;
import com.nickimpact.daycare.api.pens.Ranch;
import com.nickimpact.daycare.api.util.GsonUtils;
import com.nickimpact.daycare.commands.DaycareCmd;
import com.nickimpact.daycare.common.json.PenAdapter;
import com.nickimpact.daycare.common.json.PokemonWrapperAdapter;
import com.nickimpact.daycare.common.json.RanchAdapter;
import com.nickimpact.daycare.common.storage.StorageFactory;
import com.nickimpact.daycare.configuration.MsgConfigKeys;
import com.nickimpact.daycare.implementation.SpongeDaycareService;
import com.nickimpact.daycare.implementation.SpongePen;
import com.nickimpact.daycare.implementation.SpongeRanch;
import com.nickimpact.daycare.listeners.JoinListener;
import com.nickimpact.daycare.provided.EconomicModule;
import com.nickimpact.daycare.tasks.DaycareRunningTasks;
import com.nickimpact.daycare.text.DaycareTokens;
import com.nickimpact.daycare.text.TextParsingUtils;
import com.nickimpact.impactor.api.storage.StorageType;
import com.nickimpact.impactor.sponge.configuration.SpongeConfig;
import com.nickimpact.impactor.sponge.configuration.SpongeConfigAdapter;
import com.nickimpact.impactor.sponge.logging.SpongeLogger;
import io.github.nucleuspowered.nucleus.api.service.NucleusMessageTokenService;
import lombok.Getter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.service.economy.EconomyService;

import java.io.File;
import java.util.EnumSet;

public class PluginBootstrap {

    private SpongeDaycarePlugin plugin;

    public PluginBootstrap(SpongeDaycarePlugin plugin) {
        this.plugin = plugin;
    }

    @Getter
    private GsonBuilder builder = new GsonBuilder().setPrettyPrinting();

    public void preInit() {
        this.plugin.setLogger(new SpongeLogger(this.plugin, this.plugin.fallback));
        this.plugin.getPluginLogger().info("Initializing Daycare...");
        this.plugin.setService(new SpongeDaycareService());
        new GsonUtils(this.plugin);

        RanchAdapter r = new RanchAdapter(this.plugin);
        PenAdapter p = new PenAdapter(this.plugin);

        builder.registerTypeAdapter(Ranch.class, r)
                .registerTypeAdapter(Pen.class, p);

        try {
            r.getRegistry().register(SpongeRanch.class);
            p.getRegistry().register(SpongePen.class);
        } catch (Exception e) {
            this.plugin.getPluginLogger().error("Unable to register class typings into API Service...");
        }

        this.plugin.getService().registerUnlocker("economic", new EconomicModule());
        this.plugin.getService().getBuilderRegistry().register(Ranch.RanchBuilder.class, SpongeRanch.SpongeRanchBuilder.class);

        this.plugin.getPluginLogger().info("Registering tokens with Nucleus...");
        this.plugin.setTokens(new DaycareTokens(this.plugin));
    }

    public void init() {
        this.plugin.setGson(builder.create());
        this.plugin.getPluginLogger().info("Loading configuration...");
        this.plugin.setConfig(new SpongeConfig(new SpongeConfigAdapter(this.plugin, new File(this.plugin.getConfigDir().toFile(), "daycare.conf")), new ConfigKeys()));
        this.plugin.setMsgConfig(new SpongeConfig(new SpongeConfigAdapter(this.plugin, new File(this.plugin.getConfigDir().toFile(), "lang/en_us.conf")), new MsgConfigKeys()));

        this.plugin.getService().setActiveModule(this.plugin.getConfig().get(ConfigKeys.RANCH_UNLOCK_MODULE));

        this.plugin.getPluginLogger().info("Initializing additional dependencies...");
        this.plugin.setLoader(new ReflectionClassLoader(this));
        this.plugin.setDependencyManager(new DependencyManager(this.plugin));
        this.plugin.getDependencyManager().loadDependencies(EnumSet.of(com.nickimpact.daycare.api.dependencies.Dependency.CONFIGURATE_CORE, com.nickimpact.daycare.api.dependencies.Dependency.CONFIGURATE_HOCON, com.nickimpact.daycare.api.dependencies.Dependency.HOCON_CONFIG, com.nickimpact.daycare.api.dependencies.Dependency.CONFIGURATE_GSON, com.nickimpact.daycare.api.dependencies.Dependency.CONFIGURATE_YAML));

        StorageType st = StorageType.parse(this.plugin.getConfig().get(ConfigKeys.STORAGE_METHOD));
        this.plugin.getDependencyManager().loadStorageDependencies(ImmutableSet.of(st != null ? st : StorageType.H2));

        this.plugin.getPluginLogger().info("Registering commands with ACF...");
        this.plugin.setCmdManager(new SpongeCommandManager(this.plugin.getPluginContainer()));
        this.plugin.getCmdManager().enableUnstableAPI("help");

        this.plugin.getCmdManager().registerCommand(new DaycareCmd());

        Sponge.getEventManager().registerListeners(this.plugin, new JoinListener());

        this.plugin.getLogger().info("Initializing and reading storage...");
        ((SpongeDaycareService) this.plugin.getService()).setStorage(new StorageFactory(this.plugin).getInstance(StorageType.JSON));
    }

    public void started() {
        DaycareRunningTasks.breedingTask();
        DaycareRunningTasks.levelTask();
    }

    public void disconnecting() {

    }

    public void serviceRegistry(ChangeServiceProviderEvent e) {
        if(e.getNewProvider() instanceof EconomyService) {
            this.plugin.setEconomy((EconomyService) e.getNewProvider());
        } else if(e.getService().equals(NucleusMessageTokenService.class)) {
            this.plugin.setTextParsingUtils(new TextParsingUtils(this.plugin));
        }
    }

}
