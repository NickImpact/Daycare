package com.nickimpact.daycare.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.configuration.ConfigKeys;
import com.nickimpact.daycare.configuration.MsgConfigKeys;
import com.nickimpact.daycare.ranch.Pokemon;
import com.nickimpact.daycare.ranch.Ranch;
import com.nickimpact.daycare.stats.Statistics;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Moveset;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.evolution.types.LevelingEvolution;
import gg.psyduck.pmixins.api.EvolutionPatch;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.time.Instant;
import java.util.*;
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
		if (DaycarePlugin.getInstance().getConfig().get(ConfigKeys.LEVELING_ENABLED)) {
			Sponge.getScheduler().createTaskBuilder().execute(() -> {
				final List<Ranch> ranches = ImmutableList.copyOf(DaycarePlugin.getInstance().getRanches().stream().filter(ranch -> ranch.getSettings().canLevel()).collect(Collectors.toList()));
				for (Ranch ranch : ranches) {
					ranch.getPens().stream().filter(pen -> !pen.isEmpty()).forEach(pen -> {
						Sponge.getScheduler().createTaskBuilder().execute(() -> {
							pen.getAtPosition(1).ifPresent(pokemon -> DaycareRunningTasks.levelTask(ranch, pokemon));
							pen.getAtPosition(2).ifPresent(pokemon -> DaycareRunningTasks.levelTask(ranch, pokemon));
						}).submit(DaycarePlugin.getInstance());
					});
				}
			})
					.interval(DaycarePlugin.getInstance().getConfig().get(ConfigKeys.LVL_TASK_TIME), TimeUnit.SECONDS)
					.async()
					.submit(DaycarePlugin.getInstance());
		}
	}

	private static void levelTask(Ranch ranch, Pokemon pokemon) {
		if (pokemon.getStartLvl() + pokemon.getGainedLvls() >= 100) {
			if (pokemon.getStartLvl() + pokemon.getGainedLvls() > 100) {
				pokemon.setGainedLvls(100 - pokemon.getStartLvl());
			}
			return;
		}

		Date lastLvl = pokemon.getLastLvl();
		if (lastLvl == null) {
			lastLvl = Date.from(Instant.now());
		}
		if (Date.from(Instant.now()).after(Date.from(lastLvl.toInstant().plusSeconds(Pokemon.waitTime)))) {
			pokemon.setLastLvl(Date.from(Instant.now()));
			pokemon.incrementGainedLvls();
			ranch.getStats().incrementStat(Statistics.Stats.NUM_GAINED_LVLS);

			if (ranch.getSettings().canLearnMoves()) {
				attemptMoveLearn(ranch, pokemon);
			}

			if (Sponge.getPluginManager().isLoaded("pmixins")) {
				if (ranch.getSettings().canEvolve()) {
					if (attemptEvolution(ranch, pokemon)) {
						if (ranch.getSettings().canLearnMoves()) {
							attemptMoveLearn(ranch, pokemon);
						}
					}
				}
			}


			DaycarePlugin.getInstance().getStorage().updateRanch(ranch);
		}
	}

	private static boolean attemptEvolution(Ranch ranch, Pokemon pokemon) {
		ArrayList<LevelingEvolution> evolutions = pokemon.getPokemon().getEvolutions(LevelingEvolution.class);
		if (evolutions.size() == 0) {
			return false;
		}

		for (LevelingEvolution evolution : evolutions) {
			if (evolution.to == null || evolution.to.name == null) continue; // Ignore broken evolutions

			if (evolution.getLevel() <= pokemon.getStartLvl() + pokemon.getGainedLvls() && evolution.conditions.stream().allMatch(condition -> {
				if (condition instanceof EvolutionPatch) {
					return ((EvolutionPatch) condition).passes(pokemon.getPokemon());
				}

				return false;
			})) {
				Optional<Player> optPl = Sponge.getServer().getPlayer(ranch.getOwnerUUID());
				if (optPl.isPresent()) {
					Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
					tokens.put("pokemon_before_evo", src -> Optional.of(Text.of(pokemon.getPokemon().getSpecies().getPokemonName())));
					tokens.put("pokemon_after_evo", src -> Optional.of(Text.of(evolution.to.name)));
					optPl.get().sendMessages(MessageUtils.fetchAndParseMsgs(optPl.get(), MsgConfigKeys.EVOLVE, tokens, null));
				}
				pokemon.evolve(evolution.to);

				return true;
			}
		}

		return false;
	}

	private static void attemptMoveLearn(Ranch ranch, Pokemon pokemon) {
		Moveset moveset = pokemon.getPokemon().getMoveset();
		LinkedHashMap<Integer, ArrayList<Attack>> levelupMoves = pokemon.getPokemon().getBaseStats().levelUpMoves;
		int currLevel = pokemon.getCurrentLvl();

		ArrayList<Attack> attacks = levelupMoves.get(currLevel);
		if (attacks != null) {
			for (Attack attack : attacks) {
				Optional<Player> optPl = Sponge.getServer().getPlayer(ranch.getOwnerUUID());
				optPl.ifPresent(player -> {
					if (moveset.size() < 4) {
						moveset.add(attack);

						Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
						tokens.put("pokemon_new_move", src -> Optional.of(Text.of(attack.baseAttack.getLocalizedName())));

						Map<String, Object> variables = Maps.newHashMap();
						variables.put("dummy", pokemon.getPokemon());
						variables.put("dummy2", pokemon);

						player.sendMessages(MessageUtils.fetchAndParseMsgs(player, MsgConfigKeys.LEARN_MOVE, tokens, variables));
					} else {
						Attack old = moveset.remove(0);
						moveset.add(attack);

						Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
						tokens.put("pokemon_old_move", src -> Optional.of(Text.of(old.baseAttack.getLocalizedName())));
						tokens.put("pokemon_new_move", src -> Optional.of(Text.of(attack.baseAttack.getLocalizedName())));

						Map<String, Object> variables = Maps.newHashMap();
						variables.put("dummy", pokemon.getPokemon());
						variables.put("dummy2", pokemon);

						player.sendMessages(MessageUtils.fetchAndParseMsgs(player, MsgConfigKeys.LEARN_MOVE_REPLACE, tokens, variables));
					}
				});
			}
		}

	}
}
