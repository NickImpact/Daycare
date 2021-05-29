package com.nickimpact.daycare.sponge.implementation;

import com.google.common.collect.Maps;
import com.nickimpact.daycare.api.DaycareService;
import com.nickimpact.daycare.api.manager.NPCManager;
import com.nickimpact.daycare.api.manager.RanchManager;
import com.nickimpact.daycare.api.pens.PenUnlockModule;
import com.nickimpact.daycare.api.storage.IDaycareStorage;
import lombok.Setter;

import java.util.Map;

public class SpongeDaycareService implements DaycareService {

	private RanchManager ranchManager = new RanchManager();
	private NPCManager npcManager = new NPCManager();
	@Setter private IDaycareStorage storage;

	@Setter private PenUnlockModule module;
	private Map<String, PenUnlockModule> unlockers = Maps.newHashMap();

	@Override
	public IDaycareStorage getStorage() {
		return this.storage;
	}

	@Override
	public RanchManager getRanchManager() {
		return this.ranchManager;
	}

	@Override
	public NPCManager getNPCManager() {
		return this.npcManager;
	}

	@Override
	public PenUnlockModule getActiveModule() {
		return this.module;
	}

	@Override
	public void setActiveModule(String key) {
		this.module = getUnlockModules().getOrDefault(key, getUnlockModules().get("economic"));
	}

	@Override
	public Map<String, PenUnlockModule> getUnlockModules() {
		return this.unlockers;
	}

	@Override
	public boolean registerUnlocker(String key, PenUnlockModule module) {
		if(unlockers.containsKey(key)) {
			return false;
		}

		this.unlockers.put(key, module);
		return true;
	}

}
