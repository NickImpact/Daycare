package com.nickimpact.daycare;

import co.aikar.commands.PaperCommandManager;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.nickimpact.daycare.api.DaycareService;
import com.nickimpact.daycare.api.IDaycarePlugin;
import com.nickimpact.daycare.api.dependencies.DependencyManager;
import com.nickimpact.daycare.api.dependencies.classloader.PluginClassLoader;
import com.nickimpact.daycare.api.util.PluginInstance;
import com.nickimpact.daycare.common.DaycarePluginInfo;
import com.nickimpact.daycare.implementation.SpigotDaycareService;
import com.nickimpact.impactor.api.commands.Command;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.api.logging.Logger;
import com.nickimpact.impactor.api.platform.Platform;
import com.nickimpact.impactor.api.plugin.ImpactorPlugin;
import com.nickimpact.impactor.api.plugin.PluginInfo;
import com.nickimpact.impactor.spigot.logging.SpigotLogger;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

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

    private PluginClassLoader loader;
    private DependencyManager dependencyManager;

    private ScheduledExecutorService asyncExecutor;

    @Getter
    private Economy economy;

    @Override
    public void onLoad() {
        instance = this;
        PluginInstance.setPlugin(this);
        this.logger = new SpigotLogger(this);
        this.logger.info("&aEnabling Daycare...");
        this.logger.info("Initializing API service...");
        this.service = new SpigotDaycareService();
    }

    @Override
    public void onEnable() {

        RegisteredServiceProvider<Economy> economy = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if(economy == null) {
            logger.error("No economy service available, plugin shutting down...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        this.economy = economy.getProvider();
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
    public List<Command> getCommands() {
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
