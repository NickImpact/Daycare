package com.nickimpact.daycare.implementation;

import com.nickimpact.daycare.api.breeding.BreedStage;
import com.nickimpact.daycare.api.pens.DaycarePokemonWrapper;
import com.nickimpact.daycare.api.pens.Pen;
import com.nickimpact.daycare.api.pens.Settings;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.util.helpers.BreedLogic;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class SpigotPen extends Pen<SpigotDaycarePokemonWrapper, Pokemon> {

    public SpigotPen(int id) {
        super(id);
    }

    public SpigotPen(SpigotPenBuilder builder) {
        super(builder.uuid, builder.id, builder.slot1, builder.slot2, builder.egg, builder.unlocked, builder.dateUnlocked, builder.settings, builder.stage);
    }

    @Override
    public void addAtSlot(Pokemon pokemon, int slot) {
        super.addAtSlot(pokemon, slot);
        if(slot == 1) {
            this.slot1 = new SpigotDaycarePokemonWrapper(pokemon);
        } else {
            this.slot2 = new SpigotDaycarePokemonWrapper(pokemon);
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
    public Optional<SpigotDaycarePokemonWrapper> createEgg() {
        if(this.canBreed()) {
            return Optional.of(new SpigotDaycarePokemonWrapper(BreedLogic.makeEgg(this.getAtPosition(1).get().getDelegate(), this.getAtPosition(2).get().getDelegate())));
        }

        return Optional.empty();
    }

    @Override
    public void setEgg(SpigotDaycarePokemonWrapper wrapper) {
        super.setEgg(wrapper);
        this.egg = wrapper;
    }

    public static class SpigotPenBuilder implements PenBuilder {

        private UUID uuid;
        private int id;

        private SpigotDaycarePokemonWrapper slot1;
        private SpigotDaycarePokemonWrapper slot2;
        private SpigotDaycarePokemonWrapper egg;

        private boolean unlocked;
        private LocalDateTime dateUnlocked;

        private Settings settings = new Settings();

        private BreedStage stage;

        @Override
        public SpigotPenBuilder identifier(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        @Override
        public SpigotPenBuilder id(int id) {
            this.id = id;
            return this;
        }

        @Override
        public SpigotPenBuilder unlocked(boolean flag) {
            this.unlocked = flag;
            return this;
        }

        @Override
        public SpigotPenBuilder dateUnlocked(LocalDateTime time) {
            this.dateUnlocked = time;
            return this;
        }

        @Override
        public SpigotPenBuilder slot1(DaycarePokemonWrapper wrapper) {
            this.slot1 = (SpigotDaycarePokemonWrapper) wrapper;
            return this;
        }

        @Override
        public SpigotPenBuilder slot2(DaycarePokemonWrapper wrapper) {
            this.slot2 = (SpigotDaycarePokemonWrapper) wrapper;
            return this;
        }

        @Override
        public SpigotPenBuilder egg(DaycarePokemonWrapper wrapper) {
            this.egg = (SpigotDaycarePokemonWrapper) wrapper;
            return this;
        }

        @Override
        public SpigotPenBuilder stage(BreedStage stage) {
            this.stage = stage;
            return this;
        }

        @Override
        public SpigotPenBuilder settings(Settings settings) {
            this.settings = settings;
            return this;
        }

        @Override
        public SpigotPen build() {
            if(uuid == null) {
                uuid = UUID.randomUUID();
            }
            return new SpigotPen(this);
        }
    }
}
