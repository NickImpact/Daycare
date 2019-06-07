package com.nickimpact.daycare.implementation;

import com.nickimpact.daycare.api.pens.DaycarePokemonWrapper;
import com.nickimpact.daycare.api.util.GsonUtils;
import com.nickimpact.impactor.api.json.JsonTyping;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import net.minecraft.nbt.NBTTagCompound;

@JsonTyping("daycare_sponge_pokemon_wrapper")
public class SpongeDaycarePokemonWrapper extends DaycarePokemonWrapper<Pokemon> {

	public SpongeDaycarePokemonWrapper(Pokemon pokemon) {
		super(pokemon);
	}

	@Override
	public Pokemon getDelegate() {
		return this.delegate != null ? this.delegate : Pixelmon.pokemonFactory.create(GsonUtils.deserialize(this.json));
	}

	@Override
	public NBTTagCompound toNBT(Pokemon pokemon) {
		return pokemon.writeToNBT(new NBTTagCompound());
	}
}
