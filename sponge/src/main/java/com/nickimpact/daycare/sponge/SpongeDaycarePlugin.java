package com.nickimpact.daycare.sponge;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.SpongeCommandManager;
import com.google.common.collect.Lists;
import com.nickimpact.daycare.api.util.PluginInstance;
import com.nickimpact.daycare.common.DaycarePluginInfo;
import com.nickimpact.daycare.common.plugin.DaycarePlugin;
import com.nickimpact.daycare.sponge.text.DaycareTokens;
import com.nickimpact.daycare.sponge.text.TextParsingUtils;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.api.logging.Logger;
import com.nickimpact.impactor.api.platform.Platform;
import com.nickimpact.impactor.api.plugin.ImpactorPlugin;
import com.nickimpact.impactor.api.plugin.PluginInfo;
import lombok.Getter;
import lombok.Setter;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.service.economy.EconomyService;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

@Getter
@Setter
public abstract class SpongeDaycarePlugin extends DaycarePlugin {

	@Getter protected static SpongeDaycarePlugin spongeInstance;

	protected Config msgConfig;

	protected EconomyService economy;
	protected TextParsingUtils textParsingUtils;

	protected SpongeCommandManager cmdManager;

	protected DaycareTokens tokens;

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
	public Platform getPlatform() {
		return Platform.Sponge;
	}

	@Override
	public PluginInfo getPluginInfo() {
		return new DaycarePluginInfo();
	}

	@Override
	public Logger getPluginLogger() {
		return this.logger;
	}

	@Override
	public List<Config> getConfigs() {
		return Lists.newArrayList();
	}

	@Override
	public List<BaseCommand> getCommands() {
		return Lists.newArrayList();
	}

	@Override
	public List<Object> getListeners() {
		return Lists.newArrayList();
	}

	@Override
	public Consumer<ImpactorPlugin> onReload() {
		return x -> {};
	}

	@Override
	public boolean isConnected() {
		return false;
	}

	@Override
	public void setConnected() {

	}

	@Override
	public void handleDisconnect() {

	}

	public abstract org.slf4j.Logger getFallbackLogger();

	public abstract PluginContainer getPluginContainer();
}
