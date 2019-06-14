package com.nickimpact.daycare.common;

import com.nickimpact.impactor.api.plugin.PluginInfo;

public class DaycarePluginInfo implements PluginInfo {

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
}
