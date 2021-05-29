package com.nickimpact.daycare.reforged;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.nickimpact.daycare.reforged.pokemon.placeholders.ReforgedPlaceholderManager;
import com.nickimpact.daycare.sponge.PluginBootstrap;
import com.nickimpact.daycare.sponge.SpongeDaycarePlugin;
import com.nickimpact.daycare.api.pens.DaycarePokemonWrapper;
import com.nickimpact.daycare.api.pens.Pen;
import com.nickimpact.daycare.api.pens.Ranch;
import com.nickimpact.daycare.common.json.PokemonWrapperAdapter;
import com.nickimpact.daycare.reforged.implementation.ReforgedDaycarePokemonWrapper;
import com.nickimpact.daycare.reforged.implementation.ReforgedPen;
import com.nickimpact.daycare.reforged.ui.ReforgedPenUI;
import com.nickimpact.daycare.sponge.listeners.NPCInteractionListener;
import com.nickimpact.daycare.sponge.placeholders.DaycarePlaceholderManager;
import com.nickimpact.daycare.sponge.ui.PenUI;
import com.pixelmonmod.pixelmon.util.ITranslatable;
import lombok.Getter;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.storage.StorageType;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.GameRegistryEvent;
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
import org.spongepowered.api.text.placeholder.PlaceholderParser;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Plugin(id = "daycare", name = "Daycare", version = "@version@", dependencies = {@Dependency(id = "impactor")})
public class DaycareReforged extends SpongeDaycarePlugin {

    private PluginBootstrap bootstrap;

    @Inject
    @Getter
    protected PluginContainer pluginContainer;

    @Inject
    protected org.slf4j.Logger fallback;

    @Inject
    @ConfigDir(sharedRoot = false)
    protected Path configDir;

    @Inject
    @AsynchronousExecutor
    private SpongeExecutorService asyncExecutor;

    public DaycareReforged() {
        super();
        this.bootstrap = new PluginBootstrap(this);
    }

    @Override
    public Logger getFallbackLogger() {
        return this.fallback;
    }

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        this.bootstrap.preInit();

        super.asyncExecutor = this.asyncExecutor;

        PokemonWrapperAdapter pwa = new PokemonWrapperAdapter(this);
        try {
            pwa.getRegistry().register(ReforgedDaycarePokemonWrapper.class);
        } catch (Exception e) {
            this.getPluginLogger().error("Unable to register class typings into API Service...");
        }
        this.bootstrap.getBuilder().registerTypeAdapter(DaycarePokemonWrapper.class, pwa);

        Impactor.getInstance().getRegistry().registerBuilderSupplier(Pen.PenBuilder.class, ReforgedPen.ReforgedPenBuilder::new);
        Impactor.getInstance().getRegistry().registerBuilderSupplier(PenUI.PenUIBuilder.class, ReforgedPenUI.ReforgedPenUIBuilder::new);
        Impactor.getInstance().getRegistry().registerBuilderSupplier(DaycarePokemonWrapper.DaycarePokemonWrapperBuilder.class, ReforgedDaycarePokemonWrapper.ReforgedDaycarePokemonWrapperBuilder::new);
    }

    @Listener(order = Order.LATE)
    public void onInit(GameInitializationEvent event) {
        this.bootstrap.init();
        Sponge.getEventManager().registerListeners(this, new NPCInteractionListener());
    }

    @Listener
    public void onServiceRegister(ChangeServiceProviderEvent e) {
        this.bootstrap.serviceRegistry(e);
    }

    @Listener
    public void onPlaceholderRegistryEvent(GameRegistryEvent.Register<PlaceholderParser> event) {
        DaycarePlaceholderManager manager = new DaycarePlaceholderManager();
        for(PlaceholderParser parser : manager.getAllParsers()) {
            event.register(parser);
        }

        ReforgedPlaceholderManager reforged = new ReforgedPlaceholderManager();
        for(PlaceholderParser parser : reforged.getAllParsers()) {
            event.register(parser);
        }

    }

    @Listener
    public void onStart(GameStartedServerEvent event) {
        this.bootstrap.started(true);
    }

    @Listener
    public void onDisconnect(GameStoppingServerEvent event) {
        logger.info("Saving ranches...");
        for(Ranch ranch : this.service.getRanchManager().getLoadedRanches()) {
            try {
                this.service.getStorage().updateRanch(ranch).exceptionally(ex -> {
                    ex.printStackTrace();
                    return false;
                }).get();
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
    public List<StorageType> getStorageRequirements() {
        return Lists.newArrayList(StorageType.H2, StorageType.MYSQL, StorageType.MARIADB);
    }
}
