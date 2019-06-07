package com.nickimpact.daycare.events;

import com.nickimpact.daycare.api.events.DaycareEvent;
import com.nickimpact.daycare.api.pens.DaycarePokemonWrapper;
import com.nickimpact.daycare.api.pens.Pen;
import com.nickimpact.daycare.implementation.SpongeDaycarePokemonWrapper;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.attacks.AttackBase;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;

import java.util.Optional;
import java.util.UUID;

public class DaycareEventImpl implements DaycareEvent, Event, Cancellable {

	private UUID owner;
	private Pen pen;

	private boolean cancelled;

	public DaycareEventImpl(UUID uuid, Pen pen) {
		this.owner = uuid;
		this.pen = pen;
	}

	@Override
	public UUID getOwner() {
		return this.owner;
	}

	@Override
	public Pen getPen() {
		return this.pen;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public void setCancelled(boolean flag) {
		this.cancelled = flag;
	}

	@Override
	public Cause getCause() {
		return Sponge.getCauseStackManager().getCurrentCause();
	}

	public static class AddPokemon extends DaycareEventImpl implements DaycareEvent.AddPokemon<Pokemon> {

		private Pokemon pokemon;

		public AddPokemon(UUID uuid, Pen pen, Pokemon pokemon) {
			super(uuid, pen);
			this.pokemon = pokemon;
		}

		@Override
		public Pokemon getPokemon() {
			return pokemon;
		}
	}

	public static class RemovePokemon extends DaycareEventImpl implements DaycareEvent.RemovePokemon<Pokemon> {

		private Pokemon pokemon;

		public RemovePokemon(UUID uuid, Pen pen, Pokemon pokemon) {
			super(uuid, pen);
			this.pokemon = pokemon;
		}

		@Override
		public Pokemon getRemovedPokemon() {
			return this.pokemon;
		}

	}

	public static class Breed extends DaycareEventImpl implements DaycareEvent.Breed<Pokemon> {

		private Pokemon male;
		private Pokemon female;
		private Pokemon egg;

		public Breed(UUID uuid, Pen pen, Pokemon male, Pokemon female, Pokemon egg) {
			super(uuid, pen);
			this.male = male;
			this.female = female;
			this.egg = egg;
		}

		@Override
		public Pokemon getMaleParent() {
			return this.male;
		}

		@Override
		public Pokemon getFemaleParent() {
			return this.female;
		}

		@Override
		public Pokemon getEgg() {
			return this.egg;
		}

		@Override
		public void setTarget(Pokemon pokemon) {
			this.egg = pokemon.isEgg() ? pokemon : pokemon.makeEgg();
		}
	}

	public static class CollectEgg extends DaycareEventImpl implements DaycareEvent.CollectEgg<Pokemon> {

		private Pokemon egg;

		public CollectEgg(UUID uuid, Pen pen, Pokemon egg) {
			super(uuid, pen);
			this.egg = egg;
		}

		public Pokemon getEgg() {
			return this.egg;
		}

	}

	public static class LevelUp extends DaycareEventImpl implements DaycareEvent.LevelUp<Pokemon> {

		private Pokemon pokemon;
		private int level;
		private int gained;

		public LevelUp(UUID uuid, Pen pen, SpongeDaycarePokemonWrapper wrapper, int level) {
			super(uuid, pen);
			this.pokemon = wrapper.getDelegate();
			this.level = level;
			this.gained = wrapper.getGainedLevels();
		}

		@Override
		public Pokemon getPokemon() {
			return this.pokemon;
		}

		@Override
		public int toLevel() {
			return this.level;
		}

		@Override
		public int getGainedLevels() {
			return this.gained;
		}
	}

	public static class LearnMove extends DaycareEventImpl implements DaycareEvent.LearnMove<Pokemon, AttackBase> {

		private Pokemon learner;
		private AttackBase learned;
		private AttackBase forgot;

		public LearnMove(UUID uuid, Pen pen, Pokemon learner, AttackBase learned) {
			this(uuid, pen, learner, learned, null);
		}

		public LearnMove(UUID uuid, Pen pen, Pokemon learner, AttackBase learned, AttackBase forgot) {
			super(uuid, pen);
			this.learner = learner;
			this.learned = learned;
			this.forgot = forgot;
		}


		@Override
		public Pokemon getLearner() {
			return this.learner;
		}

		@Override
		public AttackBase getAttackLearned() {
			return this.learned;
		}

		@Override
		public Optional<AttackBase> getForgottenMove() {
			return Optional.ofNullable(this.forgot);
		}

	}

	public static class Evolve extends DaycareEventImpl implements DaycareEvent.Evolve<Pokemon, EnumSpecies> {

		private Pokemon evolving;
		private EnumSpecies to;

		public Evolve(UUID uuid, Pen pen, Pokemon evolving, EnumSpecies to) {
			super(uuid, pen);
			this.evolving = evolving;
			this.to = to;
		}

		@Override
		public Pokemon getPokemonEvolving() {
			return this.evolving;
		}

		@Override
		public EnumSpecies getSpeciesEvolvedInto() {
			return this.to;
		}
	}
}
