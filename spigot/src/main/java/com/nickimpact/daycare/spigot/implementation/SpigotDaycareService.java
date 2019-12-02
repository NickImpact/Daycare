package com.nickimpact.daycare.spigot.implementation;

import com.google.common.collect.Maps;
import com.nickimpact.daycare.api.DaycareService;
import com.nickimpact.daycare.api.manager.NPCManager;
import com.nickimpact.daycare.api.manager.RanchManager;
import com.nickimpact.daycare.api.pens.PenUnlockModule;
import com.nickimpact.daycare.api.storage.IDaycareStorage;
import com.nickimpact.impactor.api.registry.BuilderRegistry;
import lombok.Setter;

import java.util.Map;

public class SpigotDaycareService implements DaycareService {

    @Setter private IDaycareStorage storage;
    private RanchManager manager = new RanchManager();
    private NPCManager npcManager = new NPCManager();
    private PenUnlockModule active;
    private Map<String, PenUnlockModule> modules = Maps.newHashMap();
    private BuilderRegistry builders = new BuilderRegistry();

    @Override
    public IDaycareStorage getStorage() {
        return this.storage;
    }

    @Override
    public RanchManager getRanchManager() {
        return this.manager;
    }

    @Override
    public NPCManager getNPCManager() {
        return npcManager;
    }

    @Override
    public PenUnlockModule getActiveModule() {
        return this.active;
    }

    @Override
    public void setActiveModule(String key) {
        this.active = getUnlockModules().getOrDefault(key, getUnlockModules().get("economic"));
    }

    @Override
    public Map<String, PenUnlockModule> getUnlockModules() {
        return this.modules;
    }

    @Override
    public boolean registerUnlocker(String key, PenUnlockModule module) {
        if(modules.containsKey(key)) {
            return false;
        }

        this.modules.put(key, module);
        return true;
    }

    @Override
    public BuilderRegistry getBuilderRegistry() {
        return this.builders;
    }
}
