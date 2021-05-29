package com.nickimpact.daycare.api;

import com.google.gson.Gson;
import net.impactdev.impactor.api.plugin.ImpactorPlugin;
import net.impactdev.impactor.api.plugin.components.Configurable;
import net.impactdev.impactor.api.plugin.components.Depending;

import java.io.InputStream;
import java.util.concurrent.ScheduledExecutorService;

public interface IDaycarePlugin extends ImpactorPlugin, Configurable, Depending {

	DaycareService getService();

	ScheduledExecutorService getAsyncExecutor();

	Gson getGson();

	default InputStream getResourceStream(String path) {
		return getClass().getClassLoader().getResourceAsStream(path);
	}

}
