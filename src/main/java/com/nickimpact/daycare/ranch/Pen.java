package com.nickimpact.daycare.ranch;

import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.api.breeding.BreedStyle;
import com.pixelmonmod.pixelmon.entities.pixelmon.Entity10CanBreed;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * An individual area within the Daycare ranch for the player. The pen can hold only 2 pokemon, and one egg.
 *
 * @author NickImpact
 */
@Setter
public class Pen {

	private Pokemon slot1;
	private Pokemon slot2;
	private Pokemon egg;

	/** Whether the ranch owner owns this pen */
	@Getter private boolean unlocked = false;
	@Getter private Date dateUnlocked;
	@Getter private BigDecimal price;
	@Getter private int numEggsProduced;

	/** The running task of the breeding cycle for this pen */
	@Getter private BreedStyle.Instance instance;

	void unlock() {
		this.unlocked = true;
		this.dateUnlocked = Date.from(Instant.now());
	}

	public boolean isEmpty() {
		return this.slot1 == null && this.slot2 == null;
	}

	public boolean isFull() {
		return this.slot1 != null && this.slot2 != null;
	}

	public boolean canBreed() {
		return Entity10CanBreed.canBreed(slot1.getPokemon(), slot2.getPokemon()) && !this.getEgg().isPresent();
	}

	public void initialize(UUID owner) {
		if(this.instance == null) {
			this.instance = DaycarePlugin.getInstance().getBreedStyle().createInstance(owner, this, slot1, slot2);
		} else {
			if(this.instance.getClass().isInstance(DaycarePlugin.getInstance().getBreedStyle())) {
				this.instance.supply(owner, this, slot1, slot2).register();
			} else {
				this.instance = DaycarePlugin.getInstance().getBreedStyle().createInstance(owner, this, slot1, slot2);
			}
		}
	}

	public void halt() {
		if(this.instance != null) {
			this.instance.getRunner().cancel();
		}
	}

	public Optional<Pokemon> getAtPosition(int pos) {
		switch (pos) {
			case 1:
				return Optional.ofNullable(this.slot1);
			default:
				return Optional.ofNullable(this.slot2);
		}
	}

	public Optional<Pokemon> getEgg() {
		return Optional.ofNullable(this.egg);
	}
}
