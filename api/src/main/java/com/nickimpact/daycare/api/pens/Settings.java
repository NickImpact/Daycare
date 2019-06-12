package com.nickimpact.daycare.api.pens;

import lombok.Setter;

@Setter
public class Settings {
	private boolean canLevel;
	private boolean canLearnMoves;
	private boolean canEvolve;

	private transient boolean dirty;

	public boolean canLevel() {
		return this.canLevel;
	}

	public void toggleCanLevel() {
		this.canLevel = !this.canLevel;
		this.dirty = true;
	}

	public boolean canLearnMoves() {
		return this.canLearnMoves;
	}

	public void toggleCanLearnMoves() {
		this.canLearnMoves = !this.canLearnMoves;
		this.dirty = true;
	}

	public boolean canEvolve() {
		return this.canEvolve;
	}

	public void toggleCanEvolve() {
		this.canEvolve = !this.canEvolve;
		this.dirty = true;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void clean() {
		this.dirty = false;
	}
}
