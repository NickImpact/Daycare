package com.nickimpact.daycare.api.events;

import com.nickimpact.daycare.api.pens.Pen;
import net.impactdev.impactor.api.event.ImpactorEvent;
import net.impactdev.impactor.api.event.annotations.Param;
import net.impactdev.impactor.api.event.type.Cancellable;
import net.impactdev.pixelmonbridge.ImpactDevPokemon;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.UUID;

public interface DaycareEvent extends ImpactorEvent, Cancellable {

	@Param(0)
	@NonNull
	UUID getOwner();

	@Param(1)
	@NonNull
	Pen<?, ?> getPen();

	interface AddPokemon<T> extends DaycareEvent, Generic<T> {

		@Param(2)
		@NonNull
		T getPokemon();

	}

	interface RemovePokemon<T> extends DaycareEvent, Generic<T> {

		@Param(2)
		@NonNull
		T getRemovedPokemon();

	}

	interface Breed<T> extends DaycareEvent, Generic<T> {

		@Param(2)
		T getMaleParent();

		@Param(3)
		T getFemaleParent();

		@Param(4)
		T getEgg();

	}

	interface CollectEgg<T> extends DaycareEvent, Generic<T> {

		@Param(2)
		T getEgg();

	}

	interface LevelUp<T> extends DaycareEvent, Generic<T> {

		@Param(2)
		T getPokemon();

		@Param(3)
		int toLevel();

		@Param(4)
		int getGainedLevels();

	}

	interface LearnMove<T> extends DaycareEvent, Generic<T> {

		@Param(2)
		@NonNull
		T getLearner();

		@Param(3)
		@NonNull
		String getAttackLearned();

		@Param(4)
		@Nullable
		String getForgottenMove();

	}

	interface Evolve<E> extends DaycareEvent, Generic<E> {

		@Param(2)
		E getPokemonEvolving();

		@Param(3)
		String getSpeciesEvolvedInto();

	}

}
