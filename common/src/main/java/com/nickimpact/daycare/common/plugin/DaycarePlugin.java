package com.nickimpact.daycare.common.plugin;

import com.google.gson.Gson;
import com.nickimpact.daycare.api.DaycareService;
import com.nickimpact.daycare.api.IDaycarePlugin;
import com.nickimpact.impactor.api.configuration.Config;
import lombok.Getter;

import java.nio.file.Path;

@Getter
public abstract class DaycarePlugin implements IDaycarePlugin {

	@Getter private static DaycarePlugin instance;

	private DaycareService service;

	private Path configDir;
	private Config config;

	private Gson gson;

}
