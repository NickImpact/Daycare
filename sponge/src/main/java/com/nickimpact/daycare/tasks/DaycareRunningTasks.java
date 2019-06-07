package com.nickimpact.daycare.tasks;

import com.google.common.collect.Maps;
import com.nickimpact.daycare.SpongeDaycarePlugin;
import com.nickimpact.daycare.api.configuration.ConfigKeys;
import com.nickimpact.daycare.api.pens.Pen;
import com.nickimpact.daycare.api.pens.Ranch;
import com.nickimpact.daycare.configuration.MsgConfigKeys;
import com.nickimpact.daycare.events.DaycareEventImpl;
import com.nickimpact.daycare.implementation.SpongeDaycarePokemonWrapper;
import com.nickimpact.daycare.implementation.SpongePen;
import com.nickimpact.daycare.implementation.SpongeRanch;
import com.nickimpact.daycare.text.TextParsingUtils;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Moveset;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.evolution.conditions.EvoCondition;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.evolution.types.LevelingEvolution;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import gg.psyduck.pmixins.api.EvolutionPatch;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.function.Function;
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
						for (SpongePen pen : ranch.getPens().stream().filter(pen -> pen.isUnlocked() && pen.isFull() && pen.getEgg().isPresent()).collect(Collectors.toList())) {
							if(pen.canBreed() && pen.getSecondsElapsedSinceLastEgg() > SpongeDaycarePlugin.getSpongeInstance().getConfig().get(ConfigKeys.MIN_EGG_WAIT)) {
								if(rng.nextDouble() * 100 < SpongeDaycarePlugin.getSpongeInstance().getConfig().get(ConfigKeys.EGG_CHANCE) || pen.getSecondsElapsedSinceLastEgg() >= SpongeDaycarePlugin.getSpongeInstance().getConfig().get(ConfigKeys.MAX_EGG_WAIT)) {
									Optional<SpongeDaycarePokemonWrapper> wrapper = pen.createEgg();
									wrapper.ifPresent(dpw -> {
										SpongeDaycarePokemonWrapper p1 = pen.getAtPosition(1).get();
										SpongeDaycarePokemonWrapper p2 = pen.getAtPosition(2).get();
										int id = p1.getDelegate().getGender() == Gender.Male ? 1 : 2;

										DaycareEventImpl.Breed event = new DaycareEventImpl.Breed(ranch.getOwnerUUID(), pen, id == 1 ? p1.getDelegate() : p2.getDelegate(), id == 1 ? p2.getDelegate() : p1.getDelegate(), dpw.getDelegate());
										if(!Sponge.getEventManager().post(event)) {
											pen.setEgg(dpw);
										}
									});
									SpongeDaycarePlugin.getSpongeInstance().getService().getStorage().updateRanch(ranch);
								} else {
									pen.incrementTimeElapsed();
								}
							}
						}
					});
		}).interval(1, TimeUnit.SECONDS).submit(SpongeDaycarePlugin.getSpongeInstance());
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
							pen.getAtPosition(1).ifPresent(pokemon -> tryLevelUp(ranch, pen, pokemon));
							pen.getAtPosition(2).ifPresent(pokemon -> tryLevelUp(ranch, pen, pokemon));
						}
					});
		}).async().interval(1, TimeUnit.SECONDS).submit(SpongeDaycarePlugin.getSpongeInstance());
	}

	private static void tryLevelUp(Ranch ranch, Pen pen, SpongeDaycarePokemonWrapper pokemon) {
		if(pokemon.getDelegate().getLevel() + pokemon.getGainedLevels() > 100) {
			pokemon.setGainedLevels(100 - pokemon.getDelegate().getLevel());
			return;
		}

		LocalDateTime lastLevel = pokemon.getLastLevelApplyTime();
		if(lastLevel == null) {
			pokemon.setLastLevelApplyTime(lastLevel = LocalDateTime.now());
		}

		if(LocalDateTime.now().isAfter(lastLevel.plusSeconds(SpongeDaycarePlugin.getSpongeInstance().getConfig().get(ConfigKeys.LVL_WAIT_TIME)))) {
			DaycareEventImpl.LevelUp event = new DaycareEventImpl.LevelUp(ranch.getOwnerUUID(), pen, pokemon, pokemon.getDelegate().getLevel() + pokemon.getGainedLevels() + 1);
			if(!Sponge.getEventManager().post(event)) {
				pokemon.setLastLevelApplyTime(LocalDateTime.now());
				pokemon.incrementGainedLevels();

				boolean shouldUpdate = false;
				if (pen.getSettings().canLearnMoves()) {
					if (attemptMoveLearn(ranch, pen, pokemon)) {
						shouldUpdate = true;
					}
				}

				if (Sponge.getPluginManager().isLoaded("pmixins")) {
					if (pen.getSettings().canEvolve()) {
						if (attemptEvolution(ranch, pen, pokemon)) {
							if (pen.getSettings().canLearnMoves()) {
								attemptMoveLearn(ranch, pen, pokemon);
							}

							shouldUpdate = true;
						}
					}
				}

				if (shouldUpdate) {
					pokemon.updatePokemon();
					SpongeDaycarePlugin.getSpongeInstance().getService().getStorage().updateRanch(ranch);
				}
			}
		}
	}

	private static void fireSync(Runnable runnable) {
		Task.builder().execute(runnable).submit(SpongeDaycarePlugin.getSpongeInstance());
	}

	private static final BiPredicate<Pokemon, List<EvoCondition>> evoConditionCheck = (pokemon, conditions) -> conditions.stream().allMatch(condition -> {
		if (condition instanceof EvolutionPatch) {
			return ((EvolutionPatch) condition).passes(pokemon);
		}

		return false;
	});

	private static boolean attemptEvolution(Ranch ranch, Pen pen, SpongeDaycarePokemonWrapper wrapper) {
		Pokemon pokemon = wrapper.getDelegate();
		ArrayList<LevelingEvolution> evolutions = wrapper.getDelegate().getEvolutions(LevelingEvolution.class);
		if(evolutions.size() == 0) {
			return false;
		}

		for(LevelingEvolution evolution : evolutions) {
			if(evolution.to == null || evolution.to.name == null) continue;

			if(evolution.getLevel() <= pokemon.getLevel() + wrapper.getGainedLevels() && evoConditionCheck.test(pokemon, evolution.conditions)) {
				DaycareEventImpl.Evolve event = new DaycareEventImpl.Evolve(ranch.getOwnerUUID(), pen, pokemon, EnumSpecies.getFromNameAnyCase(evolution.to.name));
				if(!Sponge.getEventManager().post(event)) {
					TextParsingUtils parser = SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils();
					Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
					tokens.put("daycare_before_evo", src -> Optional.of(Text.of(pokemon.getSpecies().getPokemonName())));
					tokens.put("daycare_after_evo", src -> Optional.of(Text.of(evolution.to.name)));

					Map<String, Object> variables = Maps.newHashMap();
					variables.put("pokemon", wrapper.getDelegate());
					variables.put("wrapper", wrapper);

					pokemon.evolve(evolution.to);
					Sponge.getServer().getPlayer(ranch.getOwnerUUID()).ifPresent(player -> player.sendMessages(parser.fetchAndParseMsgs(player, MsgConfigKeys.EVOLVE, tokens, variables)));

					return true;
				}
			}
		}

		return false;
	}

	private static boolean attemptMoveLearn(Ranch ranch, Pen pen, SpongeDaycarePokemonWrapper wrapper) {
		TextParsingUtils parser = SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils();
		Moveset moveset = wrapper.getDelegate().getMoveset();
		LinkedHashMap<Integer, ArrayList<Attack>> levelupMoves = wrapper.getDelegate().getBaseStats().levelUpMoves;
		int currentLvl = wrapper.getGainedLevels() + wrapper.getDelegate().getLevel();

		Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();

		Map<String, Object> variables = Maps.newHashMap();
		variables.put("pokemon", wrapper.getDelegate());
		variables.put("wrapper", wrapper);

		ArrayList<Attack> attacks = levelupMoves.get(currentLvl);
		if(attacks != null) {
			for(Attack attack : attacks) {
				if(moveset.size() < 4) {
					DaycareEventImpl.LearnMove event = new DaycareEventImpl.LearnMove(ranch.getOwnerUUID(), pen, wrapper.getDelegate(), attack.baseAttack);
					if(!Sponge.getEventManager().post(event)) {
						moveset.add(attack);

						tokens.put("daycare_new_move", src -> Optional.of(Text.of(attack.baseAttack.getTranslatedName().getUnformattedText())));
						Sponge.getServer().getPlayer(ranch.getOwnerUUID()).ifPresent(player -> player.sendMessages(parser.fetchAndParseMsgs(player, MsgConfigKeys.LEARN_MOVE, tokens, variables)));
					}
				} else {
					DaycareEventImpl.LearnMove event = new DaycareEventImpl.LearnMove(ranch.getOwnerUUID(), pen, wrapper.getDelegate(), attack.baseAttack, moveset.get(0).baseAttack);
					if(!Sponge.getEventManager().post(event)) {
						Attack old = moveset.remove(0);
						moveset.add(attack);

						tokens.put("daycare_old_move", src -> Optional.of(Text.of(old.baseAttack.getTranslatedName().getUnformattedText())));
						tokens.put("daycare_new_move", src -> Optional.of(Text.of(attack.baseAttack.getTranslatedName().getUnformattedText())));
						Sponge.getServer().getPlayer(ranch.getOwnerUUID()).ifPresent(player -> player.sendMessages(parser.fetchAndParseMsgs(player, MsgConfigKeys.LEARN_MOVE_REPLACE, tokens, variables)));
					}

				}

			}

			return true;
		}

		return false;
	}
}
