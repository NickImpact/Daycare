package com.nickimpact.daycare.reforged.implementation;

import com.nickimpact.daycare.api.breeding.BreedStage;
import com.nickimpact.daycare.api.pens.DaycarePokemonWrapper;
import com.nickimpact.daycare.api.pens.Settings;
import com.nickimpact.daycare.implementation.SpongePen;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.util.helpers.BreedLogic;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class ReforgedPen extends SpongePen<ReforgedDaycarePokemonWrapper, Pokemon> {

	public ReforgedPen(int id) {
		super(id);
	}

	ReforgedPen(ReforgedPenBuilder builder) {
		super(builder.uuid, builder.id, builder.slot1, builder.slot2, builder.egg, builder.unlocked, builder.dateUnlocked, builder.settings, builder.stage);
	}

	@Override
	public void addAtSlot(Pokemon pokemon, int slot) {
		super.addAtSlot(pokemon, slot);
		if(slot == 1) {
			this.slot1 = new ReforgedDaycarePokemonWrapper(pokemon);
		} else {
			this.slot2 = new ReforgedDaycarePokemonWrapper(pokemon);
		}

		if(this.isFull() && this.canBreed()) {
			this.stage = BreedStage.SETTLING;
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
	public Optional<ReforgedDaycarePokemonWrapper> createEgg() {
		if(this.canBreed()) {
			return Optional.of(new ReforgedDaycarePokemonWrapper(BreedLogic.makeEgg(this.getAtPosition(1).get().getDelegate(), this.getAtPosition(2).get().getDelegate())));
		}

		return Optional.empty();
	}

	@Override
	public void setEgg(ReforgedDaycarePokemonWrapper wrapper) {
		super.setEgg(wrapper);
		this.egg = wrapper;
	}

	public static class ReforgedPenBuilder implements PenBuilder {

		private UUID uuid;
		private int id;

		private ReforgedDaycarePokemonWrapper slot1;
		private ReforgedDaycarePokemonWrapper slot2;
		private ReforgedDaycarePokemonWrapper egg;

		private boolean unlocked;
		private LocalDateTime dateUnlocked;

		private Settings settings = new Settings();

		private BreedStage stage;

		@Override
		public ReforgedPenBuilder identifier(UUID uuid) {
			this.uuid = uuid;
			return this;
		}

		@Override
		public ReforgedPenBuilder id(int id) {
			this.id = id;
			return this;
		}

		@Override
		public ReforgedPenBuilder unlocked(boolean flag) {
			this.unlocked = flag;
			return this;
		}

		@Override
		public ReforgedPenBuilder dateUnlocked(LocalDateTime time) {
			this.dateUnlocked = time;
			return this;
		}

		@Override
		public ReforgedPenBuilder slot1(DaycarePokemonWrapper wrapper) {
			this.slot1 = (ReforgedDaycarePokemonWrapper) wrapper;
			return this;
		}

		@Override
		public ReforgedPenBuilder slot2(DaycarePokemonWrapper wrapper) {
			this.slot2 = (ReforgedDaycarePokemonWrapper) wrapper;
			return this;
		}

		@Override
		public ReforgedPenBuilder egg(DaycarePokemonWrapper wrapper) {
			this.egg = (ReforgedDaycarePokemonWrapper) wrapper;
			return this;
		}

		@Override
		public ReforgedPenBuilder stage(BreedStage stage) {
			this.stage = stage;
			return this;
		}

		@Override
		public ReforgedPenBuilder settings(Settings settings) {
			this.settings = settings;
			return this;
		}

		@Override
		public ReforgedPen build() {
			if(uuid == null) {
				uuid = UUID.randomUUID();
			}
			return new ReforgedPen(this);
		}

	}
}
