package com.nickimpact.daycare.tasks;

import com.nickimpact.daycare.SpongeDaycarePlugin;
import com.nickimpact.daycare.api.breeding.BreedStage;
import com.nickimpact.daycare.api.configuration.ConfigKeys;
import com.nickimpact.daycare.api.pens.DaycarePokemonWrapper;
import com.nickimpact.daycare.api.pens.Pen;
import com.nickimpact.daycare.api.pens.Ranch;
import com.nickimpact.daycare.implementation.SpongePen;
import com.nickimpact.daycare.implementation.SpongeRanch;
import com.nickimpact.impactor.api.utilities.Time;
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
						for (SpongePen pen : ranch.getPens().stream().filter(pen -> pen.isUnlocked() && pen.isFull() && !pen.getEgg().isPresent()).collect(Collectors.toList())) {
							if(pen.canBreed()) {
								if(pen.getStage() == null) {
									pen.advanceBreeding();
								}
								long max = SpongeDaycarePlugin.getSpongeInstance().getConfig().get(ConfigKeys.BREEDING_MAX_WAIT);
								long breaker = max / 5;

								if(pen.getSecondsElapsedSinceLastEgg() % 5 == 0) {
									SpongeDaycarePlugin.getSpongeInstance().getPluginLogger().debug("Breeding Attempt for Pen " + pen.getID());
									SpongeDaycarePlugin.getSpongeInstance().getPluginLogger().debug("  Time Elapsed: &e" + new Time(pen.getSecondsElapsedSinceLastEgg()).toString());
									SpongeDaycarePlugin.getSpongeInstance().getPluginLogger().debug("  Max Time: &e" + new Time(max).toString());
									SpongeDaycarePlugin.getSpongeInstance().getPluginLogger().debug("  Breaking Time: &e" + new Time(breaker).toString());
								}
								if(pen.getSecondsElapsedSinceLastEgg() != 0) {
									if (pen.getSecondsElapsedSinceLastEgg() % breaker == 0) {
										boolean result = pen.advanceBreeding();
										if (result) {
											produceEgg(ranch, pen);
										}
										pen.resetEggChance();
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
										}
									}
								}
								pen.incrementTimeElapsed();
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
		SpongeDaycarePlugin.getSpongeInstance().getService().getStorage().updateRanch(ranch);
		pen.resetEggTimer();
	}

	public static void levelTask() {
		Task.builder().execute(() -> {
			SpongeDaycarePlugin.getSpongeInstance().getService()
					.getRanchManager()
					.getLoadedRanches()
					.stream()
					.map(ranch -> (SpongeRanch) ranch)
					.forEach(ranch -> {
						for (SpongePen pen : ranch.getPens().stream().filter(pen -> pen.isUnlocked() && !pen.isEmpty()).collect(Collectors.toList())) {
							pen.getAtPosition(1).ifPresent(pokemon -> ((DaycarePokemonWrapper)pokemon).tryLevelUp(ranch, pen));
							pen.getAtPosition(2).ifPresent(pokemon -> ((DaycarePokemonWrapper)pokemon).tryLevelUp(ranch, pen));
						}
					});
		}).async().interval(1, TimeUnit.SECONDS).submit(SpongeDaycarePlugin.getSpongeInstance());
	}

	private static void fireSync(Runnable runnable) {
		Task.builder().execute(runnable).submit(SpongeDaycarePlugin.getSpongeInstance());
	}
}
