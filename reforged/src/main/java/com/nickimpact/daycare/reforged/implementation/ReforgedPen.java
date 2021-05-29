package com.nickimpact.daycare.reforged.implementation;

import com.google.gson.reflect.TypeToken;
import com.nickimpact.daycare.api.breeding.BreedStage;
import com.nickimpact.daycare.api.events.DaycareEvent;
import com.nickimpact.daycare.api.pens.DaycarePokemonWrapper;
import com.nickimpact.daycare.api.pens.Settings;
import com.nickimpact.daycare.reforged.DaycareReforged;
import com.nickimpact.daycare.reforged.pokemon.Breeding;
import com.nickimpact.daycare.reforged.utils.Flags;
import com.nickimpact.daycare.api.pens.Pen;
import com.nickimpact.daycare.sponge.implementation.SpongePen;
import com.pixelmonmod.pixelmon.RandomHelper;
import com.pixelmonmod.pixelmon.api.pokemon.EnumInitializeCategory;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Moveset;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.forms.IEnumForm;
import com.pixelmonmod.pixelmon.util.helpers.BreedLogic;
import net.impactdev.impactor.api.Impactor;

import java.time.LocalDateTime;
import java.util.List;
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
			if(!Flags.UNBREEDABLE.matches(this.getAtPosition(1).get().getDelegate()) && !Flags.UNBREEDABLE.matches(this.getAtPosition(2).get().getDelegate())) {
				return BreedLogic.canBreed(this.getAtPosition(1).get().getDelegate(), this.getAtPosition(2).get().getDelegate());
			}
		}
		return false;
	}

	@Override
	public Optional<ReforgedDaycarePokemonWrapper> createEgg() {
		if(this.canBreed()) {
			return Optional.of(new ReforgedDaycarePokemonWrapper(this.makeEgg(this.getAtPosition(1).get().getDelegate(), this.getAtPosition(2).get().getDelegate())));
		}

		return Optional.empty();
	}

	@Override
	public void setEgg(ReforgedDaycarePokemonWrapper wrapper) {
		super.setEgg(wrapper);
		this.egg = wrapper;
	}

	private Pokemon makeEgg(Pokemon p1, Pokemon p2) {
		if(p1.isPokemon(EnumSpecies.Ditto) && p2.isPokemon(EnumSpecies.Ditto) && PixelmonConfig.allowDittoDittoBreeding) {
			EnumSpecies species = EnumSpecies.randomPoke(PixelmonConfig.allowRandomBreedingEggsToBeLegendary);
			Pokemon pokemon = PokemonSpec.from(species.name, "lvl:1").create().makeEgg();
			pokemon.initialize(EnumInitializeCategory.SPECIES, EnumInitializeCategory.INTRINSIC);

			int slot;
			if (RandomHelper.getRandomChance(1.0F / PixelmonConfig.getHiddenAbilityRate(0))) {
				slot = 2;
			} else {
				slot = RandomHelper.getRandomNumberBetween(0, pokemon.getBaseStats().abilities[1] == null ? 0 : 1);
			}

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
		return DaycareReforged.getSpongeInstance().getService().getRanchManager().getLoadedRanches().stream().filter(ranch -> ((List<ReforgedPen>)ranch.getPens()).stream().anyMatch(pen -> pen.getIdentifier().equals(this.getIdentifier()))).findAny().get().getOwnerUUID();
	}

	public static class ReforgedPenBuilder implements Pen.PenBuilder {

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
		public PenBuilder from(Pen pen) {
			return null;
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
