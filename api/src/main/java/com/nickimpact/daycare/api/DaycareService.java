package com.nickimpact.daycare.api;

import com.nickimpact.daycare.api.manager.RanchManager;
import com.nickimpact.daycare.api.pens.PenUnlockModule;
import com.nickimpact.daycare.api.storage.IDaycareStorage;
import com.nickimpact.impactor.api.registry.BuilderRegistry;

import java.util.Map;

public interface DaycareService {

	IDaycareStorage getStorage();

	RanchManager getRanchManager();

	PenUnlockModule getActiveModule();

	void setActiveModule(String key);

	Map<String, PenUnlockModule> getUnlockModules();

	boolean registerUnlocker(String key, PenUnlockModule module);

	BuilderRegistry getBuilderRegistry();

}
