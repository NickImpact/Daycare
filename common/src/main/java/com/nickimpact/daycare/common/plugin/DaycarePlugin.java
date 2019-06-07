package com.nickimpact.daycare.common.plugin;

import com.google.gson.Gson;
import com.nickimpact.daycare.api.DaycareService;
import com.nickimpact.daycare.api.IDaycarePlugin;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.api.logging.Logger;
import lombok.Getter;

import java.nio.file.Path;

@Getter
public abstract class DaycarePlugin implements IDaycarePlugin {

	protected DaycareService service;

	protected Logger logger;

	protected Path configDir;
	protected Config config;

	protected Gson gson;

}
