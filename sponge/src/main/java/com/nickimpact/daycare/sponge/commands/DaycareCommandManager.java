package com.nickimpact.daycare.sponge.commands;

import com.nickimpact.daycare.sponge.SpongeDaycarePlugin;
import org.spongepowered.api.plugin.PluginContainer;

public class DaycareCommandManager {

    private final PluginContainer container;

    public DaycareCommandManager(PluginContainer container) {
        this.container = container;
    }

    public void register() {
        new DaycareCmd(SpongeDaycarePlugin.getSpongeInstance()).register();
    }

}
