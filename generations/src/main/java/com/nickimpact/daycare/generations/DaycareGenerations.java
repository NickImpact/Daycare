package com.nickimpact.daycare.generations;

import com.nickimpact.daycare.PluginBootstrap;
import com.nickimpact.daycare.SpongeDaycarePlugin;
import com.nickimpact.daycare.generations.pokemon.PokemonTokens;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

@Plugin(id = "daycare", name = "Daycare", version = "2.0.0-DEV", dependencies = {@Dependency(id = "impactor"), @Dependency(id = "nucleus")})
public class DaycareGenerations extends SpongeDaycarePlugin{

    private PluginBootstrap bootstrap;

    public DaycareGenerations() {
        super();
        this.bootstrap = new PluginBootstrap(this);
    }

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        bootstrap.preInit();
    }

    @Listener
    public void onInit(GameInitializationEvent event) {
        bootstrap.init();
        new PokemonTokens().getTokens().forEach(tokens::register);
    }

    @Listener
    public void onServiceRegister(ChangeServiceProviderEvent e) {
        bootstrap.serviceRegistry(e);
    }

    @Listener
    public void onStart(GameStartedServerEvent event) {
        bootstrap.started();
    }

    @Listener
    public void onDisconnect(GameStoppingServerEvent event) {

    }
}
