package com.nickimpact.daycare.api.pens;

import com.nickimpact.daycare.api.util.GsonUtils;
import net.minecraft.nbt.NBTTagCompound;

import java.time.LocalDateTime;

public abstract class DaycarePokemonWrapper<T> {

	protected String json;
	protected transient T delegate;

	private int gainedLvls;
	private LocalDateTime lastLevelTime;

	private transient boolean dirty;

	public DaycarePokemonWrapper(T pokemon) {
		this.json = GsonUtils.serialize(this.toNBT(pokemon));
		this.delegate = pokemon;
		this.dirty = true;
	}

	public abstract T getDelegate();

	public void updatePokemon() {
		this.json = GsonUtils.serialize(this.toNBT(this.getDelegate()));
		this.dirty = true;
	}

	public abstract NBTTagCompound toNBT(T pokemon);

	public int getGainedLevels() {
		return this.gainedLvls;
	}

	public void setGainedLevels(int levels) {
		this.gainedLvls = levels;
		this.dirty = true;
	}

	public void incrementGainedLevels() {
		++this.gainedLvls;
		this.dirty = true;
	}

	public LocalDateTime getLastLevelApplyTime() {
		return lastLevelTime;
	}

	public void setLastLevelApplyTime(LocalDateTime lastLevelTime) {
		this.lastLevelTime = lastLevelTime;
		this.dirty = true;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void clean() {
		this.dirty = false;
	}
}
