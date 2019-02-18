package com.nickimpact.daycare.impl;

import com.google.common.collect.Maps;
import com.nickimpact.daycare.api.DaycareService;
import com.nickimpact.daycare.api.breeding.BreedStyleRegistry;
import com.nickimpact.daycare.api.pens.PenUnlockModule;

import java.util.Map;

public class DaycareServiceImpl implements DaycareService {

	private BreedStyleRegistry bsr = new BreedStyleRegistry();

	private Map<String, PenUnlockModule> modules = Maps.newHashMap();

	@Override
	public BreedStyleRegistry getBreedStyleRegistry() {
		return bsr;
	}

	@Override
	public Map<String, PenUnlockModule> getUnlockModules() {
		return this.modules;
	}

	@Override
	public boolean registerUnlocker(String key, PenUnlockModule module) {
		if(modules.containsKey(key.toLowerCase())) {
			return false;
		}

		modules.put(key.toLowerCase(), module);
		return true;
	}
}
