package com.nickimpact.daycare.api.events;


import com.nickimpact.daycare.ranch.Pen;
import com.nickimpact.daycare.ranch.Pokemon;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import lombok.*;

import java.util.UUID;

/**
 * Superclass event representing actions happening within the Daycare service.
 * Subclasses: {@link AddPokemon}, {@link Breed}, {@link CollectEgg}, {@link LearnMove}, {@link Evolve}
 *
 * @author NickImpact (Nick DeGruccio)
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class DaycareEvent extends CancellableEvent {

	private final UUID owner;
	private final Pen pen;

	@Getter
	public static class AddPokemon extends DaycareEvent implements Updatable {

		private Pokemon pokemon;

		public AddPokemon(UUID owner, Pen pen, Pokemon pokemon) {
			super(owner, pen);
			this.pokemon = pokemon;
		}

		@Override
		public void setTarget(Pokemon pokemon) {
			this.pokemon = pokemon;
		}
	}

	@Getter
	public static class Breed extends DaycareEvent implements Updatable {

		private Pokemon parent1;
		private Pokemon parent2;
		private Pokemon offspring;

		public Breed(UUID owner, Pen pen, Pokemon p1, Pokemon p2, Pokemon offspring) {
			super(owner, pen);

			this.parent1 = p1;
			this.parent2 = p2;
			this.offspring = offspring;
		}

		@Override
		public void setTarget(Pokemon pokemon) {
			this.offspring = pokemon;
			this.offspring.getPokemon().makeEgg();
		}
	}

	@Getter
	public static class CollectEgg extends DaycareEvent {

		private Pokemon egg;

		public CollectEgg(UUID owner, Pen pen, Pokemon pokemon) {
			super(owner, pen);
			this.egg = pokemon;
		}
	}

	@Getter
	public static class LearnMove extends DaycareEvent {

		private Pokemon pokemon;
		private Attack move;

		public LearnMove(UUID owner, Pen pen, Pokemon pokemon, Attack attack) {
			super(owner, pen);
			this.pokemon = pokemon;
			this.move = attack;
		}
	}

	@Getter
	public static class Evolve extends DaycareEvent {

		private Pokemon pokemon;
		private EnumSpecies evolution;

		public Evolve(UUID owner, Pen pen, Pokemon pokemon, EnumSpecies evolution) {
			super(owner, pen);
			this.pokemon = pokemon;
			this.evolution = evolution;
		}
	}

	/**
	 * Represents an event which supports the style of changing the target. This will at least allow us
	 * to have some events which don't need to follow the update logic, whilst allowing others to share
	 * a common library.
	 */
	private interface Updatable {
		/**
		 * Allows an implementing listener to update the target result of an event.
		 *
		 * @param pixelmon The pixelmon to set as the target.
		 */
		default void setTarget(com.pixelmonmod.pixelmon.api.pokemon.Pokemon pixelmon) {
			this.setTarget(new Pokemon(pixelmon));
		}

		/**
		 * Allows an implementing listener to update the target result of an event.
		 *
		 * @param pokemon The pokemon to set as the target.
		 */
		void setTarget(Pokemon pokemon);
	}
}
