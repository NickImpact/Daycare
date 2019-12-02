package com.nickimpact.daycare.spigot.implementation;

import com.nickimpact.daycare.spigot.SpigotDaycarePlugin;
import com.nickimpact.daycare.api.breeding.BreedStage;
import com.nickimpact.daycare.api.pens.DaycarePokemonWrapper;
import com.nickimpact.daycare.api.pens.Pen;
import com.nickimpact.daycare.api.pens.Settings;
import com.nickimpact.daycare.spigot.utils.Breeding;
import com.pixelmonmod.pixelmon.api.pokemon.EnumInitializeCategory;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Moveset;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.forms.IEnumForm;
import com.pixelmonmod.pixelmon.util.helpers.BreedLogic;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
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
            return Optional.of(new SpigotDaycarePokemonWrapper(this.makeEgg(this.getAtPosition(1).get().getDelegate(), this.getAtPosition(2).get().getDelegate())));
        }

        return Optional.empty();
    }

    private Pokemon makeEgg(Pokemon p1, Pokemon p2) {
        if(p1.isPokemon(EnumSpecies.Ditto) && p2.isPokemon(EnumSpecies.Ditto) && PixelmonConfig.allowDittoDittoBreeding) {
            EnumSpecies species = EnumSpecies.randomPoke(PixelmonConfig.allowRandomBreedingEggsToBeLegendary);
            Pokemon pokemon = PokemonSpec.from(species.name, "lvl:1").create().makeEgg();
            pokemon.initialize(EnumInitializeCategory.SPECIES, EnumInitializeCategory.INTRINSIC);

            List<Integer> abilities = Breeding.getValidAbilitySlots(pokemon.getBaseStats().abilities);
            int slot = abilities.get(new Random().nextInt(abilities.size()));

            pokemon.setAbility(pokemon.getBaseStats().abilities[slot]);
            pokemon.getIVs().CopyIVs(BreedLogic.getIVsForEgg(p1, p2));
            pokemon.setNature(BreedLogic.getNatureForEgg(p1, p2));
            pokemon.setGrowth(BreedLogic.getEggGrowth(p1, p2));
            pokemon.setShiny(Breeding.shouldBeShiny(this.getOwnerFromContainingRanch(), p1, p2));

            return pokemon;
        } else {
            EnumSpecies species = Breeding.getPokemonInEggName(p1, p2);
            IEnumForm form = BreedLogic.getPokemonInEggForm(species, p1, p2);
            Pokemon pokemon = PokemonSpec.from(species.name, "f:" + form.getForm()).create().makeEgg();
            pokemon.getIVs().CopyIVs(BreedLogic.getIVsForEgg(p1, p2));
            pokemon.setNature(BreedLogic.getNatureForEgg(p1, p2));
            pokemon.setCaughtBall(BreedLogic.getMotherPokeball(p1, p2));
            pokemon.setGrowth(BreedLogic.getEggGrowth(p1, p2));
            pokemon.setAbility(pokemon.getBaseStats().abilities[BreedLogic.getEggAbilitySlot(pokemon, p1, p2)]);
            pokemon.setShiny(Breeding.shouldBeShiny(this.getOwnerFromContainingRanch(), p1, p2));

            Moveset moveset = BreedLogic.getEggMoveset(pokemon, species, p1, p2);
            Moveset thisMoveset = pokemon.getMoveset();
            thisMoveset.set(0, moveset.get(0));
            thisMoveset.set(1, moveset.get(1));
            thisMoveset.set(2, moveset.get(2));
            thisMoveset.set(3, moveset.get(3));

            return pokemon;
        }
    }

    private UUID getOwnerFromContainingRanch() {
        return SpigotDaycarePlugin.getInstance().getService().getRanchManager().getLoadedRanches().stream().filter(ranch -> ((List<SpigotPen>)ranch.getPens()).stream().anyMatch(pen -> pen.getIdentifier().equals(this.getIdentifier()))).findAny().get().getOwnerUUID();
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
