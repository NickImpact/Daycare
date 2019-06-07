package com.nickimpact.daycare.api.pens;


import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public abstract class Pen<T extends DaycarePokemonWrapper<?>, E> {

	private UUID identifier;

	protected T slot1;
	protected T slot2;

	protected T egg;

	private boolean unlocked;
	private LocalDateTime dateUnlocked = LocalDateTime.now();

	private Settings settings = new Settings();

	private transient boolean dirty;

	// Time tracking settings
	private int time;

	public Pen() {
		this.identifier = UUID.randomUUID();
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

	public boolean isDirty() {
		return dirty;
	}

	public void clean() {
		this.dirty = false;
	}

	public LocalDateTime getDateUnlocked() {
		return dateUnlocked;
	}
}
