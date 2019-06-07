package com.nickimpact.daycare.implementation;

import com.nickimpact.daycare.api.pens.Pen;
import com.nickimpact.impactor.api.json.JsonTyping;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.util.helpers.BreedLogic;

import java.util.Optional;

@JsonTyping("daycare_sponge_pen")
public class SpongePen extends Pen<SpongeDaycarePokemonWrapper, Pokemon> {

	@Override
	public void addAtSlot(Pokemon pokemon, int slot) {
		super.addAtSlot(pokemon, slot);
		if(slot == 1) {
			this.slot1 = new SpongeDaycarePokemonWrapper(pokemon);
		}
	}

	@Override
	public boolean canBreed() {
		if(this.isFull()) {
			return BreedLogic.canBreed(this.getAtPosition(1).get().getDelegate(), this.getAtPosition(2).get().getDelegate());
		}
		return false;
	}

	@Override
	public Optional<SpongeDaycarePokemonWrapper> createEgg() {
		if(this.canBreed()) {
			return Optional.of(new SpongeDaycarePokemonWrapper(BreedLogic.makeEgg(this.getAtPosition(1).get().getDelegate(), this.getAtPosition(2).get().getDelegate())));
		}

		return Optional.empty();
	}

	@Override
	public void setEgg(SpongeDaycarePokemonWrapper wrapper) {
		super.setEgg(wrapper);
		this.egg = wrapper;
	}
}
