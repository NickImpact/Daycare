package com.nickimpact.daycare;

import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.nickimpact.daycare.api.dependencies.DependencyManager;
import com.nickimpact.daycare.api.dependencies.classloader.PluginClassLoader;
import com.nickimpact.daycare.api.pens.DaycarePokemonWrapper;
import com.nickimpact.daycare.api.pens.Pen;
import com.nickimpact.daycare.api.pens.Ranch;
import com.nickimpact.daycare.common.json.PenAdapter;
import com.nickimpact.daycare.common.json.PokemonWrapperAdapter;
import com.nickimpact.daycare.common.json.RanchAdapter;
import com.nickimpact.daycare.common.plugin.DaycarePlugin;
import com.nickimpact.daycare.implementation.SpongeDaycarePokemonWrapper;
import com.nickimpact.daycare.implementation.SpongePen;
import com.nickimpact.daycare.implementation.SpongeRanch;
import com.nickimpact.daycare.tasks.DaycareRunningTasks;
import com.nickimpact.daycare.text.TextParsingUtils;
import com.nickimpact.impactor.api.commands.Command;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.api.logging.Logger;
import com.nickimpact.impactor.api.platform.Platform;
import com.nickimpact.impactor.api.plugin.ImpactorPlugin;
import com.nickimpact.impactor.api.plugin.PluginInfo;
import com.nickimpact.impactor.sponge.logging.SpongeLogger;
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

	@Inject
	private org.slf4j.Logger fallback;

	private Config msgConfig;

	private EconomyService economy;

	private TextParsingUtils textParsingUtils;

	public SpongeDaycarePlugin() {
		spongeInstance = this;
	}

	@Listener
	public void onPreInit(GamePreInitializationEvent event) {
		this.logger = new SpongeLogger(this, fallback);

		RanchAdapter r = new RanchAdapter(this);
		PenAdapter p = new PenAdapter(this);
		PokemonWrapperAdapter pwa = new PokemonWrapperAdapter(this);

		this.gson = new GsonBuilder().setPrettyPrinting()
				.registerTypeAdapter(Ranch.class, r)
				.registerTypeAdapter(Pen.class, p)
				.registerTypeAdapter(DaycarePokemonWrapper.class, pwa)
				.create();

		try {
			r.getRegistry().register(SpongeRanch.class);
			p.getRegistry().register(SpongePen.class);
			pwa.getRegistry().register(SpongeDaycarePokemonWrapper.class);
		} catch (Exception e) {
			this.getPluginLogger().error("Unable to register class typings into API Service...");
		}
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
