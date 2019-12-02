package com.nickimpact.daycare.spigot;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nickimpact.daycare.api.DaycareService;
import com.nickimpact.daycare.api.IDaycarePlugin;
import com.nickimpact.daycare.api.configuration.ConfigKeys;
import com.nickimpact.daycare.api.pens.DaycarePokemonWrapper;
import com.nickimpact.daycare.api.pens.Pen;
import com.nickimpact.daycare.api.pens.Ranch;
import com.nickimpact.daycare.api.util.GsonUtils;
import com.nickimpact.daycare.api.util.PluginInstance;
import com.nickimpact.daycare.spigot.commands.DaycareCmd;
import com.nickimpact.daycare.common.DaycarePluginInfo;
import com.nickimpact.daycare.common.json.PenAdapter;
import com.nickimpact.daycare.common.json.PokemonWrapperAdapter;
import com.nickimpact.daycare.common.json.RanchAdapter;
import com.nickimpact.daycare.common.storage.StorageFactory;
import com.nickimpact.daycare.spigot.implementation.SpigotDaycarePokemonWrapper;
import com.nickimpact.daycare.spigot.implementation.SpigotDaycareService;
import com.nickimpact.daycare.spigot.implementation.SpigotPen;
import com.nickimpact.daycare.spigot.implementation.SpigotRanch;
import com.nickimpact.daycare.spigot.listeners.JoinListener;
import com.nickimpact.daycare.spigot.listeners.NPCInteractionListener;
import com.nickimpact.daycare.spigot.provided.EconomicModule;
import com.nickimpact.daycare.spigot.tasks.DaycareRunningTasks;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.api.logging.Logger;
import com.nickimpact.impactor.api.platform.Platform;
import com.nickimpact.impactor.api.plugin.ImpactorPlugin;
import com.nickimpact.impactor.api.plugin.PluginInfo;
import com.nickimpact.impactor.api.storage.StorageType;
import com.nickimpact.impactor.api.storage.dependencies.DependencyManager;
import com.nickimpact.impactor.api.storage.dependencies.classloader.PluginClassLoader;
import com.nickimpact.impactor.spigot.SpigotImpactorPlugin;
import com.nickimpact.impactor.spigot.configuration.SpigotConfig;
import com.nickimpact.impactor.spigot.configuration.SpigotConfigAdapter;
import com.nickimpact.impactor.spigot.logging.SpigotLogger;
import lombok.Getter;
import lombok.Setter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

public class SpigotDaycarePlugin extends JavaPlugin implements IDaycarePlugin {

    @Getter
    private static SpigotDaycarePlugin instance;

    private DaycareService service;
    private Logger logger;

    private Path configDir;
    private Config config;

    private Gson gson;

    private PaperCommandManager cmdManager;

    private ScheduledExecutorService asyncExecutor;

    @Getter @Setter
    private Economy economy;

    @Override
    public void onLoad() {
        instance = this;
        PluginInstance.setPlugin(this);
        this.logger = new SpigotLogger(this);
        this.logger.info("&aEnabling Daycare...");
        this.logger.info("Initializing API service...");
        this.service = new SpigotDaycareService();
        new GsonUtils(this);

        RanchAdapter r = new RanchAdapter(this);
        PenAdapter p = new PenAdapter(this);

        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        builder.registerTypeAdapter(Ranch.class, r)
                .registerTypeAdapter(Pen.class, p);

        try {
            r.getRegistry().register(SpigotRanch.class);
            p.getRegistry().register(SpigotPen.class);
        } catch (Exception e) {
            this.getPluginLogger().error("Unable to register class typings into API Service...");
        }

        this.getService().registerUnlocker("economic", new EconomicModule());
        this.getService().getBuilderRegistry().register(Ranch.RanchBuilder.class, SpigotRanch.SpigotRanchBuilder.class);

        PokemonWrapperAdapter pwa = new PokemonWrapperAdapter(this);
        try {
            pwa.getRegistry().register(SpigotDaycarePokemonWrapper.class);
        } catch (Exception e) {
            this.getPluginLogger().error("Unable to register class typings into API Service...");
        }
        builder.registerTypeAdapter(DaycarePokemonWrapper.class, pwa);
        this.gson = builder.create();

        this.getService().getBuilderRegistry().register(Pen.PenBuilder.class, SpigotPen.SpigotPenBuilder.class);
        this.getService().getBuilderRegistry().register(DaycarePokemonWrapper.DaycarePokemonWrapperBuilder.class, SpigotDaycarePokemonWrapper.SpigotDaycarePokemonWrapperBuilder.class);
    }

    @Override
    public void onEnable() {
        this.getPluginLogger().info("Loading configuration...");
        this.configDir = this.getDataFolder().toPath();
        this.config = new SpigotConfig(new SpigotConfigAdapter(this, new File(this.getConfigDir().toFile(), "daycare.conf")), new ConfigKeys());

        this.getService().setActiveModule(this.getConfiguration().get(ConfigKeys.RANCH_UNLOCK_MODULE));

        this.getPluginLogger().info("Caching economy service...");
        RegisteredServiceProvider<Economy> economy = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if(economy == null) {
            logger.error("No economy service available, plugin shutting down...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        this.economy = economy.getProvider();

        Bukkit.getPluginManager().registerEvents(new JoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new NPCInteractionListener(), this);

        this.getPluginLogger().info("Registering commands with ACF...");
        this.cmdManager = (new PaperCommandManager(this));
        this.cmdManager.enableUnstableAPI("help");

        this.cmdManager.registerCommand(new DaycareCmd());
        this.getPluginLogger().info("Initializing and reading storage...");
        ((SpigotDaycareService) this.getService()).setStorage(new StorageFactory(this).getInstance(StorageType.JSON));

        this.getService().getStorage().getAllNPCs().thenAccept(npcs -> npcs.forEach(npc -> {
            this.getService().getNPCManager().addNPC(npc);
        }));
        DaycareRunningTasks.breedingTask();
        DaycareRunningTasks.levelTask();
    }

    @Override
    public DaycareService getService() {
        return this.service;
    }

    @Override
    public ScheduledExecutorService getAsyncExecutor() {
        return asyncExecutor != null ? asyncExecutor : (asyncExecutor = Executors.newSingleThreadScheduledExecutor());
    }

    @Override
    public Gson getGson() {
        return this.gson;
    }

    @Override
    public PluginClassLoader getPluginClassLoader() {
        return SpigotImpactorPlugin.getInstance().getPluginClassLoader();
    }

    @Override
    public DependencyManager getDependencyManager() {
        return SpigotImpactorPlugin.getInstance().getDependencyManager();
    }

    @Override
    public List<StorageType> getStorageTypes() {
        return null;
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
        return Platform.Spigot;
    }

    @Override
    public PluginInfo getPluginInfo() {
        return new DaycarePluginInfo();
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
    public List<BaseCommand> getCommands() {
        return Lists.newArrayList();
    }

    @Override
    public List<Object> getListeners() {
        return Lists.newArrayList();
    }

    @Override
    public Consumer<ImpactorPlugin> onReload() {
        return plugin -> {};
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
