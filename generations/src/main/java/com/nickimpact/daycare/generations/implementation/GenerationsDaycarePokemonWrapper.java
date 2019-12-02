package com.nickimpact.daycare.generations.implementation;

import com.google.common.collect.Maps;
import com.nickimpact.daycare.sponge.SpongeDaycarePlugin;
import com.nickimpact.daycare.api.configuration.ConfigKeys;
import com.nickimpact.daycare.api.pens.DaycarePokemonWrapper;
import com.nickimpact.daycare.api.pens.Pen;
import com.nickimpact.daycare.api.pens.Ranch;
import com.nickimpact.daycare.api.util.GsonUtils;
import com.nickimpact.daycare.sponge.configuration.MsgConfigKeys;
import com.nickimpact.daycare.sponge.events.DaycareEventImpl;
import com.nickimpact.daycare.sponge.text.TextParsingUtils;
import com.nickimpact.impactor.api.json.JsonTyping;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.database.DatabaseMoves;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Moveset;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.evolution.types.LevelingEvolution;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

@JsonTyping("daycare_generations_pokemon_wrapper")
public class GenerationsDaycarePokemonWrapper extends DaycarePokemonWrapper<EntityPixelmon> {

	public GenerationsDaycarePokemonWrapper(EntityPixelmon pokemon) {
		super(pokemon);
	}

	@Override
	public EntityPixelmon getDelegate() {
		return this.delegate != null ? this.delegate : (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(GsonUtils.deserialize(this.json), (World) Sponge.getServer().getWorlds().iterator().next());
	}

	@Override
	public NBTTagCompound toNBT(EntityPixelmon pokemon) {
		return pokemon.writeToNBT(new NBTTagCompound());
	}

	@Override
	public void createEgg(Ranch ranch, Pen pen) {
		GenerationsDaycarePokemonWrapper p1 = (GenerationsDaycarePokemonWrapper) pen.getAtPosition(1).get();
		GenerationsDaycarePokemonWrapper p2 = (GenerationsDaycarePokemonWrapper) pen.getAtPosition(2).get();
		int id = p1.getDelegate().getGender() == Gender.Male ? 1 : 2;

		DaycareEventImpl.Breed event = new DaycareEventImpl.Breed(ranch.getOwnerUUID(), pen, id == 1 ? p1 : p2, id == 1 ? p2 : p1, this);
		if(!Sponge.getEventManager().post(event)) {
			pen.setEgg(this);
		}
	}

	@Override
	public void tryLevelUp(Ranch ranch, Pen pen) {
		if(this.getDelegate().getLvl().getLevel() + this.getGainedLevels() > 100) {
			this.setGainedLevels(100 - this.getDelegate().getLvl().getLevel());
			return;
		}

		LocalDateTime lastLevel = this.getLastLevelApplyTime();
		if(lastLevel == null) {
			this.setLastLevelApplyTime(lastLevel = LocalDateTime.now());
		}

		if(LocalDateTime.now().isAfter(lastLevel.plusSeconds(SpongeDaycarePlugin.getSpongeInstance().getConfig().get(ConfigKeys.LVL_WAIT_TIME)))) {
			DaycareEventImpl.LevelUp event = new DaycareEventImpl.LevelUp(ranch.getOwnerUUID(), pen, this, this.getDelegate().getLvl().getLevel() + this.getGainedLevels() + 1);
			if(!Sponge.getEventManager().post(event)) {
				this.setLastLevelApplyTime(LocalDateTime.now());
				this.incrementGainedLevels();

				boolean shouldUpdate = false;
				if (pen.getSettings().canLearnMoves()) {
					if (learnMove(ranch, pen)) {
						shouldUpdate = true;
					}
				}

				if (pen.getSettings().canEvolve()) {
					if (evolve(ranch, pen)) {
						if (pen.getSettings().canLearnMoves()) {
							learnMove(ranch, pen);
						}

						shouldUpdate = true;
					}
				}

				if (shouldUpdate) {
					this.updatePokemon();
					SpongeDaycarePlugin.getSpongeInstance().getService().getStorage().updateRanch(ranch);
				}
			}
		}
	}

	@Override
	public boolean evolve(Ranch ranch, Pen pen) {
		EntityPixelmon pokemon = this.getDelegate();
		ArrayList<LevelingEvolution> evolutions = this.getDelegate().getEvolutions(LevelingEvolution.class);
		if(evolutions.size() == 0) {
			return false;
		}

		for(LevelingEvolution evolution : evolutions) {
			if(evolution.to == null || evolution.to.name == null) continue;

			if(evolution.level <= pokemon.getLvl().getLevel() + this.getGainedLevels() && evolution.conditions.stream().allMatch(condition -> condition.passes(pokemon))) {
				DaycareEventImpl.Evolve event = new DaycareEventImpl.Evolve(ranch.getOwnerUUID(), pen, this, EnumPokemon.getFromNameAnyCase(evolution.to.name));
				if(!Sponge.getEventManager().post(event)) {
					TextParsingUtils parser = SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils();

					String prior = pokemon.getName();
					Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
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
		int currentLvl = this.getGainedLevels() + this.getDelegate().getLvl().getLevel();
		ArrayList<Attack> attacks = DatabaseMoves.getAttacksAtLevel(this.getDelegate(), currentLvl);

		Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();

		Map<String, Object> variables = Maps.newHashMap();
		variables.put("pokemon", this.getDelegate());
		variables.put("wrapper", this);

		if(attacks != null) {
			for(Attack attack : attacks) {
				if(moveset.size() < 4) {
					DaycareEventImpl.LearnMove event = new DaycareEventImpl.LearnMove(ranch.getOwnerUUID(), pen, this, attack.baseAttack);
					if(!Sponge.getEventManager().post(event)) {
						moveset.add(attack);

						tokens.put("pokemon_new_move", src -> Optional.of(Text.of(attack.baseAttack.getLocalizedName())));
						Sponge.getServer().getPlayer(ranch.getOwnerUUID()).ifPresent(player -> player.sendMessages(parser.fetchAndParseMsgs(player, MsgConfigKeys.LEARN_MOVE, tokens, variables)));
					}
				} else {
					DaycareEventImpl.LearnMove event = new DaycareEventImpl.LearnMove(ranch.getOwnerUUID(), pen, this, attack.baseAttack, moveset.get(0).baseAttack);
					if(!Sponge.getEventManager().post(event)) {
						Attack old = moveset.remove(0);
						moveset.add(attack);

						tokens.put("pokemon_old_move", src -> Optional.of(Text.of(old.baseAttack.getLocalizedName())));
						tokens.put("pokemon_new_move", src -> Optional.of(Text.of(attack.baseAttack.getLocalizedName())));
						Sponge.getServer().getPlayer(ranch.getOwnerUUID()).ifPresent(player -> player.sendMessages(parser.fetchAndParseMsgs(player, MsgConfigKeys.LEARN_MOVE_REPLACE, tokens, variables)));
					}

				}

			}

			return true;
		}

		return false;
	}
}
