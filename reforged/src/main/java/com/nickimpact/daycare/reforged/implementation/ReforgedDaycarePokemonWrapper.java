package com.nickimpact.daycare.reforged.implementation;

import com.google.common.collect.Maps;
import com.nickimpact.daycare.api.configuration.ConfigKeys;
import com.nickimpact.daycare.api.pens.DaycarePokemonWrapper;
import com.nickimpact.daycare.api.pens.Pen;
import com.nickimpact.daycare.api.pens.Ranch;
import com.nickimpact.daycare.api.util.GsonUtils;
import com.nickimpact.daycare.sponge.SpongeDaycarePlugin;
import com.nickimpact.daycare.sponge.configuration.MsgConfigKeys;
import com.nickimpact.daycare.sponge.events.DaycareEventImpl;
import com.nickimpact.daycare.sponge.text.TextParsingUtils;
import com.nickimpact.impactor.api.json.JsonTyping;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Moveset;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.evolution.conditions.EvoCondition;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.evolution.types.LevelingEvolution;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import gg.psyduck.pmixins.api.EvolutionPatch;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;

@JsonTyping("daycare_reforged_sponge_pokemon_wrapper")
public class ReforgedDaycarePokemonWrapper extends DaycarePokemonWrapper<Pokemon> {

	public ReforgedDaycarePokemonWrapper(Pokemon pokemon) {
		super(pokemon);
	}

	private ReforgedDaycarePokemonWrapper(ReforgedDaycarePokemonWrapperBuilder builder) {
		super(Pixelmon.pokemonFactory.create(GsonUtils.deserialize(builder.json)));
		this.json = builder.json;
		this.gainedLvls = builder.gainedLvls;
		this.lastLevelTime = builder.lastLvl;
	}

	@Override
	public Pokemon getDelegate() {
		return this.delegate != null ? this.delegate : Pixelmon.pokemonFactory.create(GsonUtils.deserialize(this.json));
	}

	@Override
	public NBTTagCompound toNBT(Pokemon pokemon) {
		return pokemon.writeToNBT(new NBTTagCompound());
	}

	public void createEgg(Ranch ranch, Pen pen) {
		ReforgedDaycarePokemonWrapper p1 = (ReforgedDaycarePokemonWrapper) pen.getAtPosition(1).get();
		ReforgedDaycarePokemonWrapper p2 = (ReforgedDaycarePokemonWrapper) pen.getAtPosition(2).get();
		int id = p1.getDelegate().getGender() == Gender.Male ? 1 : 2;

		DaycareEventImpl.Breed event = new DaycareEventImpl.Breed(ranch.getOwnerUUID(), pen, id == 1 ? p1 : p2, id == 1 ? p2 : p1, this);
		if(!Sponge.getEventManager().post(event)) {
			pen.setEgg(this);
		}
	}

	@Override
	public void tryLevelUp(Ranch ranch, Pen pen) {
		if(this.getDelegate().getLevel() + this.getGainedLevels() > 100) {
			this.setGainedLevels(100 - this.getDelegate().getLevel());
			return;
		}

		if(this.getDelegate().getLevel() + this.getGainedLevels() == 100) {
			return;
		}

		LocalDateTime lastLevel = this.getLastLevelApplyTime();
		if(lastLevel == null) {
			this.setLastLevelApplyTime(lastLevel = LocalDateTime.now());
		}

		if(LocalDateTime.now().isAfter(lastLevel.plusSeconds(SpongeDaycarePlugin.getSpongeInstance().getConfig().get(ConfigKeys.LVL_WAIT_TIME)))) {
			DaycareEventImpl.LevelUp event = new DaycareEventImpl.LevelUp(ranch.getOwnerUUID(), pen, this, this.getDelegate().getLevel() + this.getGainedLevels() + 1);
			if(!Sponge.getEventManager().post(event)) {
				this.setLastLevelApplyTime(LocalDateTime.now());
				this.incrementGainedLevels();

				boolean shouldUpdate = false;
				if (pen.getSettings().canLearnMoves()) {
					if (learnMove(ranch, pen)) {
						shouldUpdate = true;
					}
				}

				if (Sponge.getPluginManager().isLoaded("pmixins")) {
					if (pen.getSettings().canEvolve()) {
						if (evolve(ranch, pen)) {
							if (pen.getSettings().canLearnMoves()) {
								learnMove(ranch, pen);
							}

							shouldUpdate = true;
						}
					}
				}

				if (shouldUpdate) {
					this.updatePokemon();
					SpongeDaycarePlugin.getSpongeInstance().getService().getStorage().updateRanch(ranch);
				}
			}
		}
	}

	private static final BiPredicate<Pokemon, List<EvoCondition>> evoConditionCheck = (pokemon, conditions) -> conditions.stream().allMatch(condition -> {
		if (condition instanceof EvolutionPatch) {
			return ((EvolutionPatch) condition).passes(pokemon);
		}

		return false;
	});

	@Override
	public boolean evolve(Ranch ranch, Pen pen) {
		Pokemon pokemon = this.getDelegate();
		ArrayList<LevelingEvolution> evolutions = this.getDelegate().getEvolutions(LevelingEvolution.class);
		if(evolutions.size() == 0) {
			return false;
		}

		for(LevelingEvolution evolution : evolutions) {
			if(evolution.to == null || evolution.to.name == null) continue;

			if(evolution.getLevel() <= pokemon.getLevel() + this.getGainedLevels() && evoConditionCheck.test(pokemon, evolution.conditions)) {
				DaycareEventImpl.Evolve event = new DaycareEventImpl.Evolve(ranch.getOwnerUUID(), pen, this, EnumSpecies.getFromNameAnyCase(evolution.to.name));
				if(!Sponge.getEventManager().post(event)) {
					TextParsingUtils parser = SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils();
					Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();

					String prior = pokemon.getSpecies().getPokemonName();
					tokens.put("pokemon_before_evo", src -> Optional.of(Text.of(prior)));
					tokens.put("pokemon_after_evo", src -> Optional.of(Text.of(evolution.to.name)));

					Map<String, Object> variables = Maps.newHashMap();
					variables.put("pokemon", this.getDelegate());
					variables.put("wrapper", this);

					pokemon.evolve(evolution.to);
					this.delegate = pokemon;
					Sponge.getServer().getPlayer(ranch.getOwnerUUID()).ifPresent(player -> player.sendMessages(parser.fetchAndParseMsgs(player, MsgConfigKeys.EVOLVE, tokens, variables)));

					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean learnMove(Ranch ranch, Pen pen) {
		TextParsingUtils parser = SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils();
		Moveset moveset = this.getDelegate().getMoveset();
		LinkedHashMap<Integer, ArrayList<Attack>> levelupMoves = this.getDelegate().getBaseStats().levelUpMoves;
		int currentLvl = this.getGainedLevels() + this.getDelegate().getLevel();

		Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();

		Map<String, Object> variables = Maps.newHashMap();
		variables.put("pokemon", this.getDelegate());
		variables.put("wrapper", this);

		ArrayList<Attack> attacks = levelupMoves.get(currentLvl);
		if(attacks != null) {
			for(Attack attack : attacks) {
				if(moveset.size() < 4) {
					DaycareEventImpl.LearnMove event = new DaycareEventImpl.LearnMove(ranch.getOwnerUUID(), pen, this, attack.getActualMove());
					if(!Sponge.getEventManager().post(event)) {
						moveset.add(attack);

						tokens.put("pokemon_new_move", src -> Optional.of(Text.of(attack.getActualMove().getTranslatedName().getUnformattedText())));
						Sponge.getServer().getPlayer(ranch.getOwnerUUID()).ifPresent(player -> player.sendMessages(parser.fetchAndParseMsgs(player, MsgConfigKeys.LEARN_MOVE, tokens, variables)));
					}
				} else {
					DaycareEventImpl.LearnMove event = new DaycareEventImpl.LearnMove(ranch.getOwnerUUID(), pen, this, attack.getActualMove(), moveset.get(0).getActualMove());
					if(!Sponge.getEventManager().post(event)) {
						Attack old = moveset.remove(0);
						moveset.add(attack);

						tokens.put("pokemon_old_move", src -> Optional.of(Text.of(old.getActualMove().getTranslatedName().getUnformattedText())));
						tokens.put("pokemon_new_move", src -> Optional.of(Text.of(attack.getActualMove().getTranslatedName().getUnformattedText())));
						Sponge.getServer().getPlayer(ranch.getOwnerUUID()).ifPresent(player -> player.sendMessages(parser.fetchAndParseMsgs(player, MsgConfigKeys.LEARN_MOVE_REPLACE, tokens, variables)));
					}

				}

			}

			return true;
		}

		return false;
	}

	public static class ReforgedDaycarePokemonWrapperBuilder implements DaycarePokemonWrapperBuilder {

		private String json;
		private int gainedLvls;
		private LocalDateTime lastLvl;

		public ReforgedDaycarePokemonWrapperBuilder json(String json) {
			this.json = json;
			return this;
		}

		public ReforgedDaycarePokemonWrapperBuilder gainedLvls(int lvls) {
			this.gainedLvls = lvls;
			return this;
		}

		public ReforgedDaycarePokemonWrapperBuilder lastLvl(LocalDateTime lastLvl) {
			this.lastLvl = lastLvl;
			return this;
		}

		@Override
		public ReforgedDaycarePokemonWrapper build() {
			return new ReforgedDaycarePokemonWrapper(this);
		}

	}
}
