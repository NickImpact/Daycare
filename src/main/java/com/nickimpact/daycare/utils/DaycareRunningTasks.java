package com.nickimpact.daycare.utils;

import com.google.common.collect.ImmutableList;
import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.configuration.ConfigKeys;
import com.nickimpact.daycare.ranch.Pokemon;
import com.nickimpact.daycare.ranch.Ranch;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * (Some note will appear here)
 *
 * @author NickImpact (Nick DeGruccio)
 */
public class DaycareRunningTasks {

	public static void runLvlTask() {
		Sponge.getScheduler().createTaskBuilder().execute(() -> {
			final List<Ranch> ranches = ImmutableList.copyOf(DaycarePlugin.getInstance().getRanches());
			for(Ranch ranch : ranches) {
				ranch.getPens().stream().filter(pen -> !pen.isEmpty()).forEach(pen -> {
					pen.getAtPosition(1).ifPresent(DaycareRunningTasks::levelTask);
					pen.getAtPosition(2).ifPresent(DaycareRunningTasks::levelTask);
				});
			}
		})
		.interval(DaycarePlugin.getInstance().getConfig().get(ConfigKeys.LVL_TASK_TIME), TimeUnit.SECONDS)
		.async()
		.submit(DaycarePlugin.getInstance());
	}

	public static void runBreedingTask() {
		Sponge.getScheduler().createTaskBuilder().execute(() -> {
			final List<Ranch> ranches = ImmutableList.copyOf(DaycarePlugin.getInstance().getRanches());
			for(Ranch ranch : ranches) {
				boolean breeding = ranch.attemptBreeding();
				if (breeding) {
					Optional<Player> optPl = Sponge.getServer().getPlayer(ranch.getOwnerUUID());
					optPl.ifPresent(player -> {
						player.sendMessages(
								Text.of(),
								Text.of()
						);
					});
				}
			}
		})
		.interval(DaycarePlugin.getInstance().getConfig().get(ConfigKeys.BREEDING_TASK_TIME), TimeUnit.SECONDS)
		.submit(DaycarePlugin.getInstance());
	}

	private static void levelTask(Pokemon pokemon) {
		if(pokemon.getStartLvl() + pokemon.getGainedLvls() >= 100) {
			if(pokemon.getStartLvl() + pokemon.getGainedLvls() > 100) {
				pokemon.setGainedLvls(100 - pokemon.getStartLvl());
			}
			return;
		}

		Date lastLvl = pokemon.getLastLvl();
		if(Date.from(Instant.now()).after(Date.from(lastLvl.toInstant().plusSeconds(Pokemon.waitTime)))) {
			pokemon.setLastLvl(Date.from(Instant.now()));
			pokemon.incrementGainedLvls();
		}
	}
}
