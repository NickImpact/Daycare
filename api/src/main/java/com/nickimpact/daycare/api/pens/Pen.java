package com.nickimpact.daycare.api.pens;


import com.nickimpact.daycare.api.breeding.BreedStage;
import com.nickimpact.daycare.api.util.PluginInstance;
import com.nickimpact.impactor.api.building.Builder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public abstract class Pen<T extends DaycarePokemonWrapper<?>, E> {

	private UUID identifier;
	private int id;

	protected T slot1;
	protected T slot2;

	protected T egg;
	private BreedStage stage;

	private boolean unlocked;
	private LocalDateTime dateUnlocked = LocalDateTime.now();

	private Settings settings = new Settings();

	private transient boolean dirty;

	// Time tracking settings
	private int time;

	public Pen(int id) {
		this.identifier = UUID.randomUUID();
		this.id = id;
	}

	protected Pen(UUID identifier, int id, T slot1, T slot2, T egg, boolean unlocked, LocalDateTime dateUnlocked, Settings settings) {
		this.identifier = identifier;
		this.id = id;
		this.slot1 = slot1;
		this.slot2 = slot2;
		this.egg = egg;
		this.unlocked = unlocked;
		this.dateUnlocked = dateUnlocked;
		this.settings = settings;
	}

	public Optional<T> getAtPosition(int slot) {
		return Optional.ofNullable(slot == 1 ? slot1 : slot2);
	}

	public void addAtSlot(E pokemon, int slot) {
		this.dirty = true;
	}

	public T takeFromSlot(int slot) {
		T wrapper;
		if(slot == 1) {
			wrapper = slot1;
			slot1 = null;
		} else {
			wrapper = slot2;
			slot2 = null;
		}

		this.dirty = true;
		return wrapper;
	}

	public Optional<T> getEgg() {
		return Optional.ofNullable(egg);
	}

	public boolean isEmpty() {
		return !getAtPosition(1).isPresent() && !getAtPosition(2).isPresent();
	}

	public boolean isFull() {
		return getAtPosition(1).isPresent() && getAtPosition(2).isPresent();
	}

	public abstract boolean canBreed();

	public abstract Optional<T> createEgg();

	public void setEgg(T wrapper) {
		this.dirty = true;
	}

	public boolean isUnlocked() {
		return this.unlocked;
	}

	public void unlock() {
		this.unlocked = true;
		this.dateUnlocked = LocalDateTime.now();
		this.dirty = true;
	}

	public Settings getSettings() {
		return this.settings;
	}

	public int getSecondsElapsedSinceLastEgg() {
		return this.time;
	}

	public void incrementTimeElapsed() {
		++time;
		this.dirty = true;
	}

	public UUID getIdentifier() {
		return identifier;
	}

	public int getID() {
		return this.id;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void clean() {
		this.dirty = false;
	}

	public LocalDateTime getDateUnlocked() {
		return dateUnlocked;
	}

	public BreedStage getStage() {
		return this.stage;
	}

	public void advanceBreeding() {
		this.stage = BreedStage.values()[this.stage.ordinal() + 1];
		if(this.stage == BreedStage.BRED) {
			this.stage = BreedStage.SETTLING;
		}
	}

	public static PenBuilder builder() {
		return PluginInstance.getPlugin().getService().getBuilderRegistry().createFor(PenBuilder.class);
	}

	public interface PenBuilder extends Builder<Pen> {

		PenBuilder identifier(UUID uuid);

		PenBuilder id(int id);

		PenBuilder unlocked(boolean flag);

		PenBuilder dateUnlocked(LocalDateTime time);

		PenBuilder slot1(DaycarePokemonWrapper wrapper);

		PenBuilder slot2(DaycarePokemonWrapper wrapper);

		PenBuilder egg(DaycarePokemonWrapper wrapper);

		PenBuilder stage(BreedStage stage);

		PenBuilder settings(Settings settings);

	}
}
