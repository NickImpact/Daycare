package com.nickimpact.daycare;

import com.nickimpact.daycare.api.dependencies.DependencyManager;
import com.nickimpact.daycare.api.dependencies.classloader.PluginClassLoader;
import com.nickimpact.daycare.common.plugin.DaycarePlugin;
import com.nickimpact.daycare.tasks.DaycareRunningTasks;
import com.nickimpact.daycare.text.TextParsingUtils;
import com.nickimpact.impactor.api.commands.Command;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.api.logging.Logger;
import com.nickimpact.impactor.api.platform.Platform;
import com.nickimpact.impactor.api.plugin.ImpactorPlugin;
import com.nickimpact.impactor.api.plugin.PluginInfo;
import lombok.Getter;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.service.economy.EconomyService;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

@Getter
public class SpongeDaycarePlugin extends DaycarePlugin {

	@Getter private static SpongeDaycarePlugin spongeInstance;

	private Config msgConfig;

	private EconomyService economy;

	private TextParsingUtils textParsingUtils;

	@Listener
	public void onPreInit(GamePreInitializationEvent event) {

	}

	@Listener
	public void onInit(GameInitializationEvent event) {

	}

	@Listener
	public void onStart(GameStartedServerEvent event) {
		DaycareRunningTasks.breedingTask();
		DaycareRunningTasks.levelTask();
	}

	@Listener
	public void onDisconnect(GameStoppingServerEvent event) {

	}

	@Override
	public ScheduledExecutorService getAsyncExecutor() {
		return null;
	}

	@Override
	public PluginClassLoader getPluginClassLoader() {
		return null;
	}

	@Override
	public DependencyManager getDependencyManager() {
		return null;
	}

	@Override
	public Config getConfiguration() {
		return null;
	}

	@Override
	public Platform getPlatform() {
		return Platform.Sponge;
	}

	@Override
	public PluginInfo getPluginInfo() {
		return null;
	}

	@Override
	public Logger getPluginLogger() {
		return null;
	}

	@Override
	public List<Config> getConfigs() {
		return null;
	}

	@Override
	public List<Command> getCommands() {
		return null;
	}

	@Override
	public List<Object> getListeners() {
		return null;
	}

	@Override
	public Consumer<ImpactorPlugin> onReload() {
		return null;
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
}
