package com.nickimpact.daycare.sponge.tasks;

import com.nickimpact.daycare.sponge.SpongeDaycarePlugin;
import com.nickimpact.daycare.api.configuration.ConfigKeys;
import com.nickimpact.daycare.api.pens.DaycarePokemonWrapper;
import com.nickimpact.daycare.api.pens.Pen;
import com.nickimpact.daycare.api.pens.Ranch;
import com.nickimpact.daycare.sponge.configuration.MsgConfigKeys;
import com.nickimpact.daycare.sponge.implementation.SpongePen;
import com.nickimpact.daycare.sponge.implementation.SpongeRanch;
import com.nickimpact.daycare.sponge.observing.PenObservers;
import com.nickimpact.daycare.sponge.ui.PenUI;
import com.nickimpact.daycare.sponge.utils.TextParser;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DaycareRunningTasks {

	private static final Random rng = new Random();

	public static void breedingTask() {
		Task.builder().execute(() -> {
			SpongeDaycarePlugin.getSpongeInstance().getService()
					.getRanchManager()
					.getLoadedRanches()
					.stream()
					.map(ranch -> (SpongeRanch) ranch)
					.forEach(ranch -> {
						if (SpongeDaycarePlugin.getSpongeInstance().getConfiguration().get(ConfigKeys.BREEDING_ENABLED)) {
							for (SpongePen pen : ranch.getPens().stream().filter(pen -> pen.isUnlocked() && pen.isFull() && !pen.getEgg().isPresent()).collect(Collectors.toList())) {
								if (pen.canBreed()) {
									if (pen.getStage() == null) {
										pen.advanceBreeding();
										PenObservers.getObserver(ranch.getOwnerUUID()).ifPresent(PenUI::update);
									}
									long max = SpongeDaycarePlugin.getSpongeInstance().getConfig().get(ConfigKeys.BREEDING_MAX_WAIT);
									long breaker = max / 5;

									if (pen.getSecondsElapsedSinceLastEgg() != 0) {
										if (pen.getSecondsElapsedSinceLastEgg() % breaker == 0) {
											boolean result = pen.advanceBreeding();
											if (result) {
												produceEgg(ranch, pen);
											}
											pen.resetEggChance();
											PenObservers.getObserver(ranch.getOwnerUUID()).ifPresent(PenUI::update);
										} else {
											if (pen.getSecondsElapsedSinceLastEgg() % SpongeDaycarePlugin.getSpongeInstance().getConfig().get(ConfigKeys.BREEDING_STAGE_ADVANCE_ATTEMPT_INTERVAL) == 0) {
												if (rng.nextDouble() * 100 < pen.getChance() + (SpongeDaycarePlugin.getSpongeInstance().getConfig().get(ConfigKeys.BREEDING_STAGE_ADVANCE_CHANCE))) {
													boolean result = pen.advanceBreeding();
													pen.pushTimeToIncrement();
													if (result) {
														produceEgg(ranch, pen);
														pen.resetEggChance();
													}
												} else {
													pen.incrementEggChance();
												}
												PenObservers.getObserver(ranch.getOwnerUUID()).ifPresent(PenUI::update);
											}
										}
									}
									pen.incrementTimeElapsed();
								}
							}
						}
					});
		}).interval(1, TimeUnit.SECONDS).submit(SpongeDaycarePlugin.getSpongeInstance());
	}

	private static void produceEgg(Ranch ranch, Pen pen) {
		Optional<DaycarePokemonWrapper> wrapper = pen.createEgg();
		wrapper.ifPresent(dpw -> {
			dpw.createEgg(ranch, pen);
		});
		Sponge.getServer().getPlayer(ranch.getOwnerUUID()).ifPresent(player -> {
			player.sendMessages(TextParser.parse(TextParser.read(MsgConfigKeys.EGGS_AVAILABLE)));
		});
		SpongeDaycarePlugin.getSpongeInstance().getService().getStorage().updateRanch(ranch);
		pen.resetEggTimer();
	}

	public static void levelTask(boolean async) {
		Task.Builder task = Task.builder().execute(() -> {
			SpongeDaycarePlugin.getSpongeInstance().getService()
					.getRanchManager()
					.getLoadedRanches()
					.stream()
					.map(ranch -> (SpongeRanch) ranch)
					.forEach(ranch -> {
						for (SpongePen pen : ranch.getPens().stream().filter(pen -> pen.isUnlocked() && !pen.isEmpty()).collect(Collectors.toList())) {
							if (pen.getSettings().canLevel()) {
								pen.getAtPosition(1).ifPresent(pokemon -> ((DaycarePokemonWrapper) pokemon).tryLevelUp(ranch, pen));
								pen.getAtPosition(2).ifPresent(pokemon -> ((DaycarePokemonWrapper) pokemon).tryLevelUp(ranch, pen));
							}
						}
					});
		});

		if (async) {
			task.async();
		}

		task.interval(1, TimeUnit.SECONDS).submit(SpongeDaycarePlugin.getSpongeInstance());
	}

	private static void fireSync(Runnable runnable) {
		Task.builder().execute(runnable).submit(SpongeDaycarePlugin.getSpongeInstance());
	}
}
