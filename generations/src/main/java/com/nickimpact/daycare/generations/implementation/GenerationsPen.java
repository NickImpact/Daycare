package com.nickimpact.daycare.generations.implementation;

import com.nickimpact.daycare.api.breeding.BreedStage;
import com.nickimpact.daycare.api.pens.DaycarePokemonWrapper;
import com.nickimpact.daycare.api.pens.Pen;
import com.nickimpact.daycare.api.pens.Settings;
import com.nickimpact.daycare.sponge.implementation.SpongePen;
import com.pixelmongenerations.common.entity.pixelmon.Entity10CanBreed;
import com.pixelmongenerations.common.entity.pixelmon.EntityPixelmon;
import com.pixelmongenerations.core.config.PixelmonEntityList;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class GenerationsPen extends SpongePen<GenerationsDaycarePokemonWrapper, EntityPixelmon> {

	public GenerationsPen(int id) {
		super(id);
	}

	protected GenerationsPen(GenerationsPenBuilder builder) {
		super(builder.uuid, builder.id, builder.slot1, builder.slot2, builder.egg, builder.unlocked, builder.dateUnlocked, builder.settings, builder.stage);
	}

	@Override
	public void addAtSlot(EntityPixelmon pokemon, int slot) {
		super.addAtSlot(pokemon, slot);
		if(slot == 1) {
			this.slot1 = new GenerationsDaycarePokemonWrapper(pokemon);
		} else {
			this.slot2 = new GenerationsDaycarePokemonWrapper(pokemon);
		}

		if(this.isFull() && this.canBreed()) {
			this.stage = BreedStage.SETTLING;
		}
	}

	@Override
	public boolean canBreed() {
		if(this.isFull()) {
			return Entity10CanBreed.canBreed(this.getAtPosition(1).get().getDelegate(), this.getAtPosition(2).get().getDelegate());
		}
		return false;
	}

	@Override
	public Optional<GenerationsDaycarePokemonWrapper> createEgg() {
		if(this.canBreed()) {
			EntityPixelmon base = (EntityPixelmon) PixelmonEntityList.createEntityByName("Bidoof", (World) Sponge.getServer().getWorlds().iterator().next());
			base.makeEntityIntoEgg(this.getAtPosition(1).get().getDelegate(), this.getAtPosition(2).get().getDelegate());
			return Optional.of(new GenerationsDaycarePokemonWrapper(base));
		}

		return Optional.empty();
	}

	@Override
	public void setEgg(GenerationsDaycarePokemonWrapper wrapper) {
		super.setEgg(wrapper);
		this.egg = wrapper;
	}

	public static class GenerationsPenBuilder implements Pen.PenBuilder {

		private UUID uuid;
		private int id;

		private GenerationsDaycarePokemonWrapper slot1;
		private GenerationsDaycarePokemonWrapper slot2;
		private GenerationsDaycarePokemonWrapper egg;

		private boolean unlocked;
		private LocalDateTime dateUnlocked;

		private Settings settings = new Settings();

		private BreedStage stage;

		@Override
		public GenerationsPenBuilder identifier(UUID uuid) {
			this.uuid = uuid;
			return this;
		}

		@Override
		public GenerationsPenBuilder id(int id) {
			this.id = id;
			return this;
		}

		@Override
		public GenerationsPenBuilder unlocked(boolean flag) {
			this.unlocked = flag;
			return this;
		}

		@Override
		public GenerationsPenBuilder dateUnlocked(LocalDateTime time) {
			this.dateUnlocked = time;
			return this;
		}

		@Override
		public GenerationsPenBuilder slot1(DaycarePokemonWrapper wrapper) {
			this.slot1 = (GenerationsDaycarePokemonWrapper) wrapper;
			return this;
		}

		@Override
		public GenerationsPenBuilder slot2(DaycarePokemonWrapper wrapper) {
			this.slot2 = (GenerationsDaycarePokemonWrapper) wrapper;
			return this;
		}

		@Override
		public GenerationsPenBuilder egg(DaycarePokemonWrapper wrapper) {
			this.egg = (GenerationsDaycarePokemonWrapper) wrapper;
			return this;
		}

		@Override
		public GenerationsPenBuilder stage(BreedStage stage) {
			this.stage = stage;
			return this;
		}

		@Override
		public GenerationsPenBuilder settings(Settings settings) {
			this.settings = settings;
			return this;
		}

		@Override
		public GenerationsPen build() {
			if(uuid == null) {
				uuid = UUID.randomUUID();
			}
			return new GenerationsPen(this);
		}
	}
}
