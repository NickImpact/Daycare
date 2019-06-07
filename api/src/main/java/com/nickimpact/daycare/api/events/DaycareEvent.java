package com.nickimpact.daycare.api.events;

import com.nickimpact.daycare.api.pens.Pen;
import java.util.Optional;
import java.util.UUID;

public interface DaycareEvent extends Cancelable {

	UUID getOwner();

	Pen getPen();

	interface AddPokemon<T> extends DaycareEvent {

		T getPokemon();

	}

	interface RemovePokemon<T> extends DaycareEvent {

		T getRemovedPokemon();

	}

	interface Breed<T> extends DaycareEvent, Updatable<T> {

		T getMaleParent();

		T getFemaleParent();

		T getEgg();

	}

	interface CollectEgg<T> extends DaycareEvent {

		T getEgg();

	}

	interface LevelUp<T> extends DaycareEvent {

		T getPokemon();

		int toLevel();

		int getGainedLevels();

	}

	interface LearnMove<T, E> extends DaycareEvent {

		T getLearner();

		E getAttackLearned();

		Optional<E> getForgottenMove();

	}

	interface Evolve<T, E> extends DaycareEvent {

		T getPokemonEvolving();

		E getSpeciesEvolvedInto();

	}

	interface Updatable<T> {

		void setTarget(T pokemon);

	}
}
