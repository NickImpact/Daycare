package com.nickimpact.daycare.implementation;

import com.nickimpact.daycare.SpongeDaycarePlugin;
import com.nickimpact.daycare.api.breeding.BreedStage;
import com.nickimpact.daycare.api.pens.DaycarePokemonWrapper;
import com.nickimpact.daycare.api.pens.Pen;
import com.nickimpact.daycare.api.pens.Settings;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class SpongePen<T extends DaycarePokemonWrapper<?>, E> extends Pen<T, E> {

	public SpongePen(int id) {
		super(id);
	}

	protected SpongePen(UUID identifier, int id, T slot1, T slot2, T egg, boolean unlocked, LocalDateTime dateUnlocked, Settings settings, BreedStage stage) {
		super(identifier, id, slot1, slot2, egg, unlocked, dateUnlocked, settings, stage);
	}

	public static PenBuilder builder() {
		return SpongeDaycarePlugin.getSpongeInstance().getService().getBuilderRegistry().createFor(PenBuilder.class);
	}
}
