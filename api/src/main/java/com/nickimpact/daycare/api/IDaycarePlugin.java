package com.nickimpact.daycare.api;

import com.google.gson.Gson;
import com.nickimpact.impactor.api.plugin.Configurable;
import com.nickimpact.impactor.api.plugin.Dependable;
import com.nickimpact.impactor.api.plugin.ImpactorPlugin;

import java.io.InputStream;
import java.util.concurrent.ScheduledExecutorService;

public interface IDaycarePlugin extends ImpactorPlugin, Configurable, Dependable {

	DaycareService getService();

	ScheduledExecutorService getAsyncExecutor();

	Gson getGson();

	default InputStream getResourceStream(String path) {
		return getClass().getClassLoader().getResourceAsStream(path);
	}

}
