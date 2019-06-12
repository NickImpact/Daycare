package com.nickimpact.daycare.implementation;

import com.nickimpact.daycare.api.breeding.BreedStage;
import com.nickimpact.daycare.api.pens.DaycarePokemonWrapper;
import com.nickimpact.daycare.api.pens.Pen;
import com.nickimpact.daycare.api.pens.Settings;
import com.nickimpact.impactor.api.json.JsonTyping;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.util.helpers.BreedLogic;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@JsonTyping("daycare_sponge_pen")
public class SpongePen extends Pen<SpongeDaycarePokemonWrapper, Pokemon> {

	SpongePen(int id) {
		super(id);
	}

	SpongePen(SpongePenBuilder builder) {
		super(builder.uuid, builder.id, builder.slot1, builder.slot2, builder.egg, builder.unlocked, builder.dateUnlocked, builder.settings, builder.stage);
	}

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

	public static class SpongePenBuilder implements PenBuilder {

		private UUID uuid;
		private int id;

		private SpongeDaycarePokemonWrapper slot1;
		private SpongeDaycarePokemonWrapper slot2;
		private SpongeDaycarePokemonWrapper egg;

		private boolean unlocked;
		private LocalDateTime dateUnlocked;

		private Settings settings;

		private BreedStage stage;

		@Override
		public PenBuilder identifier(UUID uuid) {
			this.uuid = uuid;
			return this;
		}

		@Override
		public PenBuilder id(int id) {
			this.id = id;
			return this;
		}

		@Override
		public PenBuilder unlocked(boolean flag) {
			this.unlocked = flag;
			return this;
		}

		@Override
		public PenBuilder dateUnlocked(LocalDateTime time) {
			this.dateUnlocked = time;
			return this;
		}

		@Override
		public PenBuilder slot1(DaycarePokemonWrapper wrapper) {
			this.slot1 = (SpongeDaycarePokemonWrapper) wrapper;
			return this;
		}

		@Override
		public PenBuilder slot2(DaycarePokemonWrapper wrapper) {
			this.slot2 = (SpongeDaycarePokemonWrapper) wrapper;
			return this;
		}

		@Override
		public PenBuilder egg(DaycarePokemonWrapper wrapper) {
			this.egg = (SpongeDaycarePokemonWrapper) wrapper;
			return this;
		}

		@Override
		public PenBuilder stage(BreedStage stage) {
			this.stage = stage;
			return this;
		}

		@Override
		public PenBuilder settings(Settings settings) {
			this.settings = settings;
			return this;
		}

		@Override
		public SpongePen build() {
			return new SpongePen(this);
		}

	}
}
