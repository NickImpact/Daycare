package com.nickimpact.daycare.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.DaycareInfo;
import com.nickimpact.daycare.configuration.ConfigKeys;
import com.nickimpact.daycare.configuration.MsgConfigKeys;
import com.nickimpact.daycare.ranch.Pokemon;
import com.nickimpact.daycare.ranch.Ranch;
import com.nickimpact.daycare.stats.Statistics;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Moveset;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.evolution.Evolution;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.evolution.types.LevelingEvolution;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * (Some note will appear here)
 *
 * @author NickImpact (Nick DeGruccio)
 */
public class DaycareRunningTasks {

	public static void runLvlTask() {
		Sponge.getScheduler().createTaskBuilder().execute(() -> {
			final List<Ranch> ranches = ImmutableList.copyOf(DaycarePlugin.getInstance().getRanches().stream().filter(ranch -> ranch.getSettings().canLevel()).collect(Collectors.toList()));
			for(Ranch ranch : ranches) {
				ranch.getPens().stream().filter(pen -> !pen.isEmpty()).forEach(pen -> {
					pen.getAtPosition(1).ifPresent(pokemon -> DaycareRunningTasks.levelTask(ranch, pokemon));
					pen.getAtPosition(2).ifPresent(pokemon -> DaycareRunningTasks.levelTask(ranch, pokemon));
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
						player.sendMessages(MessageUtils.fetchMsgs(player, MsgConfigKeys.EGGS_AVAILABLE));
					});
				}
			}
		})
		.interval(DaycarePlugin.getInstance().getConfig().get(ConfigKeys.BREEDING_TASK_TIME), TimeUnit.SECONDS)
		.submit(DaycarePlugin.getInstance());
	}

	private static void levelTask(Ranch ranch, Pokemon pokemon) {
		if(pokemon.getStartLvl() + pokemon.getGainedLvls() >= 100) {
			if(pokemon.getStartLvl() + pokemon.getGainedLvls() > 100) {
				pokemon.setGainedLvls(100 - pokemon.getStartLvl());
			}
			return;
		}

		Date lastLvl = pokemon.getLastLvl();
		if(lastLvl == null) {
			lastLvl = Date.from(Instant.now());
		}
		if(Date.from(Instant.now()).after(Date.from(lastLvl.toInstant().plusSeconds(Pokemon.waitTime)))) {
			pokemon.setLastLvl(Date.from(Instant.now()));
			pokemon.incrementGainedLvls();
			ranch.getStats().incrementStat(Statistics.Stats.NUM_GAINED_LVLS);
			if(ranch.getSettings().canEvolve()) {
				attemptEvolution(ranch, pokemon);
			}

			// Unsupported until Pixelmon 6.3
			if(ranch.getSettings().canLearnMoves()) {
				attemptMoveLearn(pokemon);
			}
			DaycarePlugin.getInstance().getStorage().updateRanch(ranch);
		}
	}

	private static void attemptEvolution(Ranch ranch, Pokemon pokemon) {
		for(LevelingEvolution evo : pokemon.getPokemon().getEvolutions(LevelingEvolution.class)) {
			if(evo.level <= pokemon.getStartLvl() + pokemon.getGainedLvls()) {
				Optional<Player> optPl = Sponge.getServer().getPlayer(ranch.getOwnerUUID());
				optPl.ifPresent(player -> {
					Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
					tokens.put("pokemon_before_evo", src -> Optional.of(Text.of(pokemon.getPokemon())));
					tokens.put("pokemon_after_evo", src -> Optional.of(Text.of(evo.to.name)));
					player.sendMessages(MessageUtils.fetchAndParseMsgs(player, MsgConfigKeys.EVOLVE, tokens, null));
				});
				pokemon.evolve(evo.to);
			}
		}
	}

	private static void attemptMoveLearn(Pokemon pokemon) {
		Moveset current = pokemon.getPokemon().getMoveset();
		// TODO - Get the next available move, and if levels match, do work for it
	}
}
