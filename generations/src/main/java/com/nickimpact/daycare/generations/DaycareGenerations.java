package com.nickimpact.daycare.generations;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.nickimpact.daycare.api.configuration.ConfigKeys;
import com.nickimpact.daycare.sponge.PluginBootstrap;
import com.nickimpact.daycare.sponge.SpongeDaycarePlugin;
import com.nickimpact.daycare.api.pens.DaycarePokemonWrapper;
import com.nickimpact.daycare.api.pens.Pen;
import com.nickimpact.daycare.api.pens.Ranch;
import com.nickimpact.daycare.common.json.PokemonWrapperAdapter;
import com.nickimpact.daycare.generations.implementation.GenerationsDaycarePokemonWrapper;
import com.nickimpact.daycare.generations.implementation.GenerationsPen;
import com.nickimpact.daycare.generations.pokemon.PokemonTokens;
import com.nickimpact.daycare.generations.ui.GenerationsPenUI;
import com.nickimpact.daycare.api.pens.DaycarePokemonWrapper;
import com.nickimpact.daycare.api.pens.Pen;
import com.nickimpact.daycare.api.pens.Ranch;
import com.nickimpact.daycare.common.json.PokemonWrapperAdapter;
import com.nickimpact.daycare.sponge.ui.PenUI;
import com.nickimpact.impactor.api.storage.StorageType;
import com.nickimpact.impactor.api.storage.dependencies.DependencyManager;
import com.nickimpact.impactor.api.storage.dependencies.classloader.PluginClassLoader;
import com.nickimpact.impactor.sponge.SpongeImpactorPlugin;
import lombok.Getter;
import org.slf4j.Logger;
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

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Plugin(id = "daycare", name = "Daycare", version = "2.1.0", dependencies = {@Dependency(id = "impactor"), @Dependency(id = "nucleus")})
public class DaycareGenerations extends SpongeDaycarePlugin {

    private PluginBootstrap bootstrap;
    
    private DependencyManager dependencyManager;

    @Inject
    @Getter
    protected PluginContainer pluginContainer;

    @Inject
    protected org.slf4j.Logger fallback;

    @Inject
    @ConfigDir(sharedRoot = false)
    protected Path configDir;

    public DaycareGenerations() {
        super();
        this.bootstrap = new PluginBootstrap(this);
    }

    @Override
    public Logger getFallbackLogger() {
        return this.fallback;
    }

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        bootstrap.preInit();

        PokemonWrapperAdapter pwa = new PokemonWrapperAdapter(this);
        try {
            pwa.getRegistry().register(GenerationsDaycarePokemonWrapper.class);
        } catch (Exception e) {
            this.getPluginLogger().error("Unable to register class typings into API Service...");
        }
        this.bootstrap.getBuilder().registerTypeAdapter(DaycarePokemonWrapper.class, pwa);

        this.getService().getBuilderRegistry().register(Pen.PenBuilder.class, GenerationsPen.GenerationsPenBuilder.class);
        this.getService().getBuilderRegistry().register(PenUI.PenUIBuilder.class, GenerationsPenUI.GenerationsPenUIBuilder.class);
    }

    @Listener
    public void onInit(GameInitializationEvent event) {
        //must be set before booststrap init, otherwise high risk of NPE
        this.dependencyManager = new DependencyManager(this);
        
        bootstrap.init();
        new PokemonTokens().getTokens().forEach(tokens::register);
    }

    @Listener
    public void onServiceRegister(ChangeServiceProviderEvent e) {
        bootstrap.serviceRegistry(e);
    }

    @Listener
    public void onStart(GameStartedServerEvent event) {
        bootstrap.started(false);
    }

    @Listener
    public void onDisconnect(GameStoppingServerEvent event) {
        logger.info("Saving ranches...");
        for(Ranch ranch : this.service.getRanchManager().getLoadedRanches()) {
            try {
                this.service.getStorage().updateRanch(ranch).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Path getConfigDir() {
        return this.configDir;
    }

    @Override
    public PluginClassLoader getPluginClassLoader() {
        return SpongeImpactorPlugin.getInstance().getPluginClassLoader();
    }

    @Override
    public DependencyManager getDependencyManager() {
        //this may or may not work and fully depends on whether or not the Impactor plugin is initialized, if it isnt, the plugin crashes
        //return SpongeImpactorPlugin.getInstance().getDependencyManager();
        return this.dependencyManager;
    }

    @Override
    public List<StorageType> getStorageTypes() {
        return Lists.newArrayList(StorageType.parse(this.config.get(ConfigKeys.STORAGE_METHOD)));
    }
}
