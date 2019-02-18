package com.nickimpact.daycare.ranch;

import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.configuration.ConfigKeys;
import com.nickimpact.daycare.utils.GsonUtils;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.NBTTagCompound;

import java.time.Instant;
import java.util.Date;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class Pokemon {

	private transient com.pixelmonmod.pixelmon.api.pokemon.Pokemon pokemon;
	private transient NBTTagCompound nbt;
	private String json;

	@Getter
	private final int startLvl;

	@Getter
	private final Gender gender;

	@Getter @Setter
	private int gainedLvls;

	@Getter @Setter
	private Date lastLvl;

	@Getter @Setter private transient boolean cantLevel;

	public static final long waitTime = DaycarePlugin.getInstance().getConfig().get(ConfigKeys.LVL_WAIT_TIME);

	public Pokemon(com.pixelmonmod.pixelmon.api.pokemon.Pokemon pokemon) {
		this.pokemon = pokemon;
		this.startLvl = pokemon.getLevel();
		this.gender = pokemon.getGender();
		NBTTagCompound nbt = new NBTTagCompound();
		this.nbt = pokemon.writeToNBT(nbt);
		json = GsonUtils.serialize(this.nbt);
		this.lastLvl = Date.from(Instant.now());
	}

	public com.pixelmonmod.pixelmon.api.pokemon.Pokemon getPokemon() {
		if(this.pokemon == null) {
			this.pokemon = Pixelmon.pokemonFactory.create(this.decode());
		}

		return this.pokemon;
	}

	public void evolve(PokemonSpec to) {
		this.getPokemon().evolve(to);
		NBTTagCompound nbt = new NBTTagCompound();
		this.nbt = this.getPokemon().writeToNBT(nbt);
		this.json = GsonUtils.serialize(this.nbt);
	}

	private NBTTagCompound decode() {
		return nbt != null ? nbt : (nbt = GsonUtils.deserialize(json));
	}

	public void incrementGainedLvls() {
		++this.gainedLvls;
	}

	public int getCurrentLvl() {
		return this.startLvl + this.gainedLvls;
	}
}
