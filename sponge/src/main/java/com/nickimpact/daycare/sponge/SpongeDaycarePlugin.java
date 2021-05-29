package com.nickimpact.daycare.sponge;

import com.nickimpact.daycare.api.util.PluginInstance;
import com.nickimpact.daycare.common.plugin.DaycarePlugin;
import lombok.Getter;
import lombok.Setter;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.logging.Logger;
import net.impactdev.impactor.api.plugin.PluginMetadata;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.service.economy.EconomyService;

import java.util.concurrent.ScheduledExecutorService;

@Getter
@Setter
public abstract class SpongeDaycarePlugin extends DaycarePlugin {

	@Getter protected static SpongeDaycarePlugin spongeInstance;

	protected Config msgConfig;

	protected EconomyService economy;

	protected SpongeExecutorService asyncExecutor;

	public SpongeDaycarePlugin() {
		spongeInstance = this;
		PluginInstance.setPlugin(this);
	}

	@Override
	public ScheduledExecutorService getAsyncExecutor() {
		return this.asyncExecutor;
	}

	@Override
	public Config getConfiguration() {
		return this.config;
	}

	@Override
	public PluginMetadata getMetadata() {
		return PluginMetadata.builder()
				.id("daycare")
				.name("Daycare")
				.version("@version@")
				.build();
	}

	@Override
	public Logger getPluginLogger() {
		return this.logger;
	}

	public abstract org.slf4j.Logger getFallbackLogger();

	public abstract PluginContainer getPluginContainer();
}
