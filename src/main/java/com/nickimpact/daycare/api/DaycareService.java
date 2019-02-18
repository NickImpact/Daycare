package com.nickimpact.daycare.api;

import com.nickimpact.daycare.api.breeding.BreedStyleRegistry;
import com.nickimpact.daycare.api.pens.PenUnlockModule;

import java.util.Map;

public interface DaycareService {
	BreedStyleRegistry getBreedStyleRegistry();

	Map<String, PenUnlockModule> getUnlockModules();

	boolean registerUnlocker(String key, PenUnlockModule module);
}
