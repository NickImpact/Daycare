package com.nickimpact.daycare.reforged;

import com.nickimpact.daycare.PluginBootstrap;
import com.nickimpact.daycare.SpongeDaycarePlugin;
import com.nickimpact.daycare.api.pens.DaycarePokemonWrapper;
import com.nickimpact.daycare.api.pens.Pen;
import com.nickimpact.daycare.common.json.PokemonWrapperAdapter;
import com.nickimpact.daycare.implementation.SpongePen;
import com.nickimpact.daycare.implementation.SpongeRanch;
import com.nickimpact.daycare.reforged.implementation.ReforgedDaycarePokemonWrapper;
import com.nickimpact.daycare.reforged.implementation.ReforgedPen;
import com.nickimpact.daycare.reforged.pokemon.PokemonTokens;
import com.nickimpact.daycare.reforged.ui.ReforgedPenUI;
import com.nickimpact.daycare.ui.PenUI;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

@Plugin(id = "daycare", name = "Daycare", version = "2.0.0-DEV", dependencies = {@Dependency(id = "impactor"), @Dependency(id = "nucleus")})
public class DaycareReforged extends SpongeDaycarePlugin {

    private PluginBootstrap bootstrap;

    public DaycareReforged() {
        super();
        this.bootstrap = new PluginBootstrap(this);
    }

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        this.bootstrap.preInit();

        PokemonWrapperAdapter pwa = new PokemonWrapperAdapter(this);
        try {
            pwa.getRegistry().register(ReforgedDaycarePokemonWrapper.class);
        } catch (Exception e) {
            this.getPluginLogger().error("Unable to register class typings into API Service...");
        }
        this.bootstrap.getBuilder().registerTypeAdapter(DaycarePokemonWrapper.class, pwa);

        this.getService().getBuilderRegistry().register(Pen.PenBuilder.class, ReforgedPen.ReforgedPenBuilder.class);
        this.getService().getBuilderRegistry().register(PenUI.PenUIBuilder.class, ReforgedPenUI.ReforgedPenUIBuilder.class);
    }

    @Listener
    public void onInit(GameInitializationEvent event) {
        this.bootstrap.init();
        new PokemonTokens().getTokens().forEach(tokens::register);
    }

    @Listener
    public void onServiceRegister(ChangeServiceProviderEvent e) {
        this.bootstrap.serviceRegistry(e);
    }

    @Listener
    public void onStart(GameStartedServerEvent event) {
        this.bootstrap.started();
    }

    @Listener
    public void onDisconnect(GameStoppingServerEvent event) {

    }

}
