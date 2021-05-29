package com.nickimpact.daycare.api.pens;


import com.google.gson.reflect.TypeToken;
import com.nickimpact.daycare.api.breeding.BreedStage;
import com.nickimpact.daycare.api.configuration.ConfigKeys;
import com.nickimpact.daycare.api.events.DaycareEvent;
import com.nickimpact.daycare.api.util.PluginInstance;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.utilities.Builder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public abstract class Pen<T extends DaycarePokemonWrapper<?>, E> {

	private UUID identifier;
	private int id;

	protected T slot1;
	protected T slot2;

	protected T egg;
	protected BreedStage stage;

	private boolean unlocked;
	private LocalDateTime dateUnlocked = LocalDateTime.now();

	private Settings settings = new Settings();

	private transient boolean dirty;

	// Time tracking settings
	private long time;
	private double chance;

	public Pen(int id) {
		this.identifier = UUID.randomUUID();
		this.id = id;
	}

	public Pen(UUID identifier, int id, T slot1, T slot2, T egg, boolean unlocked, LocalDateTime dateUnlocked, Settings settings, BreedStage stage) {
		this.identifier = identifier;
		this.id = id;
		this.slot1 = slot1;
		this.slot2 = slot2;
		this.egg = egg;
		this.unlocked = unlocked;
		this.dateUnlocked = dateUnlocked;
		this.settings = settings;
		this.stage = stage;
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
		this.stage = null;
		return wrapper;
	}

	public Optional<T> getEgg() {
		return Optional.ofNullable(egg);
	}

	public T claimEgg() {
		T egg = this.egg;
		this.egg = null;
		return egg;
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

	public long getSecondsElapsedSinceLastEgg() {
		return this.time;
	}

	public void incrementTimeElapsed() {
		++time;
		this.dirty = true;
	}

	public void pushTimeToIncrement() {
		long increment = PluginInstance.getPlugin().getConfiguration().get(ConfigKeys.BREEDING_MAX_WAIT) / 5;
		this.time = increment * (this.stage == null ? 0 : this.stage.ordinal());
	}

	public void resetEggTimer() {
		this.time = 0;
	}

	public double getChance() {
		return this.chance;
	}

	public void incrementEggChance() {
		this.chance += PluginInstance.getPlugin().getConfiguration().get(ConfigKeys.BREEDING_STAGE_ADVANCE_INCREMENT);
		this.dirty = true;
	}

	public void resetEggChance() {
		this.chance = 0;
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

	public boolean advanceBreeding() {
		if(this.stage == null) {
			this.stage = BreedStage.SETTLING;
			return false;
		}

		this.stage = BreedStage.values()[this.stage.ordinal() + 1];
		if(this.stage == BreedStage.BRED) {
			this.stage = null;
			return true;
		}

		return false;
	}

	public static PenBuilder builder() {
		return Impactor.getInstance().getRegistry().createBuilder(PenBuilder.class);
	}

	public interface PenBuilder extends Builder<Pen, PenBuilder> {

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
