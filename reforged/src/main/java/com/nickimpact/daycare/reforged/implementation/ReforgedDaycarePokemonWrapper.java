package com.nickimpact.daycare.reforged.implementation;

import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import com.nickimpact.daycare.api.configuration.ConfigKeys;
import com.nickimpact.daycare.api.events.DaycareEvent;
import com.nickimpact.daycare.api.pens.DaycarePokemonWrapper;
import com.nickimpact.daycare.api.pens.Pen;
import com.nickimpact.daycare.api.pens.Ranch;
import com.nickimpact.daycare.api.util.GsonUtils;
import com.nickimpact.daycare.reforged.pokemon.placeholders.MoveUpdatePlaceholder;
import com.nickimpact.daycare.sponge.SpongeDaycarePlugin;
import com.nickimpact.daycare.sponge.configuration.MsgConfigKeys;
import com.nickimpact.daycare.sponge.utils.TextParser;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Moveset;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.evolution.conditions.EvoCondition;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.evolution.types.LevelingEvolution;
import me.nickimpact.pixelmonmixins.api.server.EvolutionPatch;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.json.JsonTyping;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.Sponge;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

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

		boolean event = Impactor.getInstance().getEventBus().post(
				DaycareEvent.Breed.class,
				new TypeToken<Pokemon>(){},
				ranch.getOwnerUUID(),
				pen,
				(id == 1 ? p1 : p2).getDelegate(),
				(id == 1 ? p2 : p1).getDelegate(),
				this.getDelegate()
		);

		if(!event) {
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
			boolean event = Impactor.getInstance().getEventBus().post(
					DaycareEvent.LevelUp.class,
					new TypeToken<Pokemon>(){},
					ranch.getOwnerUUID(),
					pen,
					this.getDelegate(),
					this.getDelegate().getLevel() + this.getGainedLevels() + 1,
					this.getGainedLevels()
			);

			if(!event) {
				this.setLastLevelApplyTime(LocalDateTime.now());
				this.incrementGainedLevels();

				boolean shouldUpdate = false;
				if (pen.getSettings().canLearnMoves()) {
					if (learnMove(ranch, pen)) {
						shouldUpdate = true;
					}
				}

				if (Sponge.getPluginManager().isLoaded("pixelmonmixins")) {
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
				boolean event = Impactor.getInstance().getEventBus().post(
						DaycareEvent.Evolve.class,
						new TypeToken<Pokemon>(){},
						ranch.getOwnerUUID(),
						pen,
						this.getDelegate(),
						evolution.to.name
				);

				if(!event) {
					List<Supplier<Object>> sources = Lists.newArrayList();
					sources.add(this::getDelegate);
					sources.add(() -> this);

					Sponge.getServer().getPlayer(ranch.getOwnerUUID()).ifPresent(player -> player.sendMessages(TextParser.parse(TextParser.read(MsgConfigKeys.EVOLVE), sources)));
					pokemon.evolve(evolution.to);
					this.delegate = pokemon;

					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean learnMove(Ranch ranch, Pen pen) {
		Moveset moveset = this.getDelegate().getMoveset();
		LinkedHashMap<Integer, ArrayList<Attack>> levelupMoves = this.getDelegate().getBaseStats().levelUpMoves;
		int currentLvl = this.getGainedLevels() + this.getDelegate().getLevel();

		List<Supplier<Object>> sources = Lists.newArrayList();
		sources.add(this::getDelegate);
		sources.add(() -> this);

		ArrayList<Attack> attacks = levelupMoves.get(currentLvl);
		if(attacks != null) {
			for(Attack attack : attacks) {
				List<Supplier<Object>> sourcesDeep = Lists.newArrayList(sources);
				sourcesDeep.add(() -> new MoveUpdatePlaceholder.MoveUpdateContext(attack.getActualMove(), MoveUpdatePlaceholder.MoveUpdateContext.Context.NEW));

				if(moveset.size() < 4) {
					boolean event = Impactor.getInstance().getEventBus().post(
							DaycareEvent.LearnMove.class,
							new TypeToken<Pokemon>(){},
							ranch.getOwnerUUID(),
							pen,
							this.getDelegate(),
							attack.getActualMove().getLocalizedName(),
							null
					);

					if(!event) {
						moveset.add(attack);

						Sponge.getServer().getPlayer(ranch.getOwnerUUID())
								.ifPresent(player -> player.sendMessages(TextParser.parse(TextParser.read(MsgConfigKeys.LEARN_MOVE), sourcesDeep)));
					}
				} else {
					boolean event = Impactor.getInstance().getEventBus().post(
							DaycareEvent.LearnMove.class,
							new TypeToken<Pokemon>(){},
							ranch.getOwnerUUID(),
							pen,
							this.getDelegate(),
							attack.getActualMove().getLocalizedName(),
							moveset.get(0).getActualMove().getLocalizedName()
					);

					if(!event) {
						Attack old = moveset.remove(0);
						moveset.add(attack);

						sourcesDeep.add(() -> new MoveUpdatePlaceholder.MoveUpdateContext(old.getActualMove(), MoveUpdatePlaceholder.MoveUpdateContext.Context.OLD));
						Sponge.getServer().getPlayer(ranch.getOwnerUUID())
								.ifPresent(player -> player.sendMessages(TextParser.parse(TextParser.read(MsgConfigKeys.LEARN_MOVE_REPLACE), sourcesDeep)));
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
		public DaycarePokemonWrapperBuilder from(DaycarePokemonWrapper daycarePokemonWrapper) {
			return null;
		}

		@Override
		public ReforgedDaycarePokemonWrapper build() {
			return new ReforgedDaycarePokemonWrapper(this);
		}

	}
}
