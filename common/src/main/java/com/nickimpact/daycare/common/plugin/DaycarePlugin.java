package com.nickimpact.daycare.common.plugin;

import com.google.gson.Gson;
import com.nickimpact.daycare.api.DaycareService;
import com.nickimpact.daycare.api.IDaycarePlugin;
import lombok.Getter;
import lombok.Setter;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.logging.Logger;

@Getter
@Setter
public abstract class DaycarePlugin implements IDaycarePlugin {

	protected DaycareService service;

	protected Logger logger;

	protected Config config;

	protected Gson gson;

}
