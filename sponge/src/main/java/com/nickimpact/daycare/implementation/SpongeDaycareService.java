package com.nickimpact.daycare.implementation;

import com.google.common.collect.Maps;
import com.nickimpact.daycare.api.DaycareService;
import com.nickimpact.daycare.api.manager.RanchManager;
import com.nickimpact.daycare.api.pens.PenUnlockModule;
import com.nickimpact.daycare.api.storage.IDaycareStorage;
import com.nickimpact.impactor.api.registry.BuilderRegistry;
import lombok.Setter;

import java.util.Map;

public class SpongeDaycareService implements DaycareService {

	private RanchManager ranchManager = new RanchManager();
	@Setter private IDaycareStorage storage;

	@Setter private PenUnlockModule module;
	private Map<String, PenUnlockModule> unlockers = Maps.newHashMap();

	private BuilderRegistry builders = new BuilderRegistry();

	@Override
	public IDaycareStorage getStorage() {
		return this.storage;
	}

	@Override
	public RanchManager getRanchManager() {
		return this.ranchManager;
	}

	@Override
	public PenUnlockModule getActiveModule() {
		return this.module;
	}

	@Override
	public void setActiveModule(String key) {
		this.module = this.unlockers.get(key);
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

	@Override
	public BuilderRegistry getBuilderRegistry() {
		return this.builders;
	}

}
