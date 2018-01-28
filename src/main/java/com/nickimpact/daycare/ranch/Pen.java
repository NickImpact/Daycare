package com.nickimpact.daycare.ranch;

import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.configuration.ConfigKeys;
import com.pixelmonmod.pixelmon.entities.pixelmon.Entity10CanBreed;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.Random;

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
	@Getter private Date readyPoint;

	/** Whether the ranch owner owns this pen */
	@Getter private boolean unlocked = false;

	private static final long MAX_WAIT = DaycarePlugin.getInstance().getConfig().get(ConfigKeys.MAX_BREEDING_WAIT_TIME);
	private static final Random rng = new Random();

	public void unlock() {
		this.unlocked = true;
	}

	public boolean isEmpty() {
		return this.slot1 == null && this.slot2 == null;
	}

	public boolean isFull() {
		return this.slot1 != null && this.slot2 != null;
	}

	public boolean canBreed() {
		return Entity10CanBreed.canBreed(slot1.getPokemon(), slot2.getPokemon()) && egg == null;
	}

	/**
	 * Determines whether two parent pokemon will produce an offspring on the next iteration check.
	 *
	 * @return True if two parent pokemon will breed, false otherwise
	 */
	public boolean willBreed() {
		if(!this.isFull() || !this.canBreed()){
			return false;
		}

		Date wait = Date.from(Instant.now().plusSeconds(5));
		if(wait.compareTo(Date.from(Instant.now().plusSeconds(MAX_WAIT))) < 0) {
			int rngVal = rng.nextInt(100001); // Between 0 - 100000
			double result = (double) rngVal / 1000000.0; // Between 0.0 - 1.0
			double chance = DaycarePlugin.getInstance().getConfig().get(ConfigKeys.EGG_CHANCE) / 100;

			return result < chance;
		}

		return false;
	}

	public String breedChance() {
		if(slot1 == null && slot2 == null) {
			return "No pokemon in this pen...";
		}

		if(slot1 == null || slot2 == null) {
			return "No available partner";
		}

		if(!this.canBreed()) {
			return "Incompatible partners";
		}

		return String.format("%s and %s love each other!", slot1.getPokemon().getName(), slot2.getPokemon().getName());
	}

	public Pokemon breed() {
		EntityPixelmon offspring = new EntityPixelmon(slot1.getPokemon().world);
		offspring.makeEntityIntoEgg(slot1.getPokemon(), slot2.getPokemon());

		return new Pokemon(offspring);
	}

	public Optional<Pokemon> getAtPosition(int pos) {
		switch (pos) {
			case 1:
				return Optional.ofNullable(this.slot1);
			default:
				return Optional.ofNullable(this.slot2);
		}
	}
}
