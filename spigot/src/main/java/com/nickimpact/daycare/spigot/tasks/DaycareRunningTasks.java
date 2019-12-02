package com.nickimpact.daycare.spigot.tasks;

import com.nickimpact.daycare.spigot.SpigotDaycarePlugin;
import com.nickimpact.daycare.api.configuration.ConfigKeys;
import com.nickimpact.daycare.api.pens.DaycarePokemonWrapper;
import com.nickimpact.daycare.api.pens.Pen;
import com.nickimpact.daycare.api.pens.Ranch;
import com.nickimpact.daycare.spigot.implementation.SpigotPen;
import com.nickimpact.daycare.spigot.implementation.SpigotRanch;
import com.nickimpact.daycare.spigot.observers.PenObservers;
import com.nickimpact.daycare.spigot.ui.PenUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class DaycareRunningTasks {

	private static final Random rng = new Random();

	public static void breedingTask() {
		Bukkit.getScheduler().runTaskTimer(SpigotDaycarePlugin.getInstance(), () -> {
			SpigotDaycarePlugin.getInstance().getService()
					.getRanchManager()
					.getLoadedRanches()
					.stream()
					.map(ranch -> (SpigotRanch) ranch)
					.forEach(ranch -> {
						if (SpigotDaycarePlugin.getInstance().getConfiguration().get(ConfigKeys.BREEDING_ENABLED)) {
							for (SpigotPen pen : ranch.getPens().stream().filter(pen -> pen.isUnlocked() && pen.isFull() && !pen.getEgg().isPresent()).collect(Collectors.toList())) {
								if (pen.canBreed()) {
									if (pen.getStage() == null) {
										pen.advanceBreeding();
										PenObservers.getObserver(ranch.getOwnerUUID()).ifPresent(PenUI::update);
									}
									long max = SpigotDaycarePlugin.getInstance().getConfiguration().get(ConfigKeys.BREEDING_MAX_WAIT);
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
											if (pen.getSecondsElapsedSinceLastEgg() % SpigotDaycarePlugin.getInstance().getConfiguration().get(ConfigKeys.BREEDING_STAGE_ADVANCE_ATTEMPT_INTERVAL) == 0) {
												if (rng.nextDouble() * 100 < pen.getChance() + (SpigotDaycarePlugin.getInstance().getConfiguration().get(ConfigKeys.BREEDING_STAGE_ADVANCE_CHANCE))) {
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
		}, 0, 20);
	}

	private static void produceEgg(Ranch ranch, Pen pen) {
		Optional<DaycarePokemonWrapper> wrapper = pen.createEgg();
		wrapper.ifPresent(dpw -> {
			dpw.createEgg(ranch, pen);
		});

		OfflinePlayer owner = Bukkit.getOfflinePlayer(ranch.getOwnerUUID());
		if(owner.isOnline()) {
			owner.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&eDaycare &7\u00bb A new egg has been discovered in your ranches!"));
		}

		SpigotDaycarePlugin.getInstance().getService().getStorage().updateRanch(ranch);
		pen.resetEggTimer();
	}

	public static void levelTask() {
		Bukkit.getScheduler().runTaskTimer(SpigotDaycarePlugin.getInstance(), () -> {
			SpigotDaycarePlugin.getInstance().getService()
					.getRanchManager()
					.getLoadedRanches()
					.stream()
					.map(ranch -> (SpigotRanch) ranch)
					.forEach(ranch -> {
						for (SpigotPen pen : ranch.getPens().stream().filter(pen -> pen.isUnlocked() && !pen.isEmpty()).collect(Collectors.toList())) {
							if (pen.getSettings().canLevel()) {
								pen.getAtPosition(1).ifPresent(pokemon -> ((DaycarePokemonWrapper) pokemon).tryLevelUp(ranch, pen));
								pen.getAtPosition(2).ifPresent(pokemon -> ((DaycarePokemonWrapper) pokemon).tryLevelUp(ranch, pen));
							}
						}
					});
		}, 0, 20);
	}
}
