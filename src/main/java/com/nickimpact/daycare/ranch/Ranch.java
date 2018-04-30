package com.nickimpact.daycare.ranch;

import com.google.common.collect.Lists;
import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.api.events.BreedEvent;
import com.nickimpact.daycare.configuration.ConfigKeys;
import com.nickimpact.daycare.exceptions.AlreadyUnlockedException;
import com.nickimpact.daycare.stats.Statistics;
import lombok.Getter;
import lombok.Setter;
import org.mariuszgromada.math.mxparser.Expression;
import org.mariuszgromada.math.mxparser.Function;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.service.economy.account.UniqueAccount;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.Instant;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The physical area where pokemon may be deposited to gain levels and potentially breed eggs
 *
 * @author NickImpact
 */
@Getter
public class Ranch {

	private UUID ownerUUID;
	private List<Pen> pens;
	@Setter private Statistics stats;
	private Settings settings;

	public Ranch(Player player) {
		this(player.getUniqueId());
	}

	public Ranch(UUID ownerUUID) {
		this.ownerUUID = ownerUUID;
		this.pens = Lists.newArrayList();
		this.stats = new Statistics();
		this.settings = new Settings();

		for(int i = 0; i < DaycarePlugin.getInstance().getConfig().get(ConfigKeys.NUM_PENS) && i < 14; i++) {
			this.pens.add(new Pen());
		}

		if(DaycarePlugin.getInstance().getConfig().get(ConfigKeys.FIRST_PEN_UNLOCKED)) {
			this.pens.get(0).unlock();
			this.pens.get(0).setDateUnlocked(Date.from(Instant.now()));
			this.pens.get(0).setPrice(new BigDecimal(-1));
		}
	}

	/**
	 * Attempts to unlock a pen for breeding, requiring that the user has the right amount of money
	 * to afford the pen.
	 *
	 * @param id The ID of the pen to open
	 * @return True if the unlock was successful, false otherwise
	 */
	public boolean unlock(int id) throws AlreadyUnlockedException {
		Pen pen = this.pens.get(id);
		if(pen.isUnlocked()) {
			throw new AlreadyUnlockedException();
		}

		// Check here if the user can afford the pen
		Optional<UniqueAccount> account = DaycarePlugin.getInstance().getEconomy().getOrCreateAccount(this.ownerUUID);
		if(account.isPresent()) {
			Function function = new Function("P(b, i, p) = " + DaycarePlugin.getInstance().getConfig().get(ConfigKeys.PEN_PRICE_EQUATION));
			Expression expression = new Expression(String.format(
					"P(%.2f, %.2f, %d)",
					DaycarePlugin.getInstance().getConfig().get(ConfigKeys.BASE_PEN_PRICE),
					DaycarePlugin.getInstance().getConfig().get(ConfigKeys.INCREMENT_PEN_PRICE),
					id
			), function);
			BigDecimal price = new BigDecimal(expression.calculate());

			if(account.get().getBalance(DaycarePlugin.getInstance().getEconomy().getDefaultCurrency()).compareTo(price) < 0) {
				// Cannot afford
				return false;
			}

			account.get().withdraw(DaycarePlugin.getInstance().getEconomy().getDefaultCurrency(), price, Sponge.getCauseStackManager().getCurrentCause());
			this.pens.get(id).unlock();
			pen.setPrice(price);
			return true;
		}

		return false;
	}

	public boolean addToPen(Pokemon pokemon, int id) {
		Pen pen = this.pens.get(id);
		if(pen.isFull()) {
			return false;
		}

		if(!pen.getAtPosition(1).isPresent()) {
			pen.setSlot1(pokemon);
		} else {
			pen.setSlot2(pokemon);
		}

		// Update the date so that the timer can start evaluating breeding conditions
		if(pen.isFull() && pen.canBreed()) {
			pen.setReadyPoint(Date.from(Instant.now()));
		}

		DaycarePlugin.getInstance().getStorage().updateRanch(this);
		return true;
	}

	/**
	 * For each pen in this ranch, attempt to breed the parent pokemon, if there are enough. As always, first check
	 * to ensure that the pen itself is unlocked, and that breeding is deemed acceptable for the current iteration.
	 */
	public boolean attemptBreeding() {
		boolean bred = false;
		int id = 0;
		for(Pen pen : this.pens) {
			if(pen.isUnlocked() && pen.willBreed()) {
				Pokemon offspring = pen.breed();
				BreedEvent event = new BreedEvent(this.ownerUUID, id, pen.getAtPosition(1).get(), pen.getAtPosition(2).get(), offspring);
				Sponge.getEventManager().post(event);

				if(!event.isCancelled()) {
					pen.setEgg(offspring);
					stats.incrementStat(Statistics.Stats.EGGS_PRODUCED);
					bred = true;
				}
			}
			id++;
		}
		if(bred) {
			DaycarePlugin.getInstance().getStorage().updateRanch(this);
		}

		return bred;
	}

	public Pen getPen(int id) {
		return this.pens.get(id);
	}

	@Setter
	public class Settings {
		private boolean canLevel;
		private boolean canLearnMoves;
		private boolean canEvolve;

		public boolean canLevel() {
			return this.canLevel;
		}

		public boolean canLearnMoves() {
			return this.canLearnMoves;
		}

		public boolean canEvolve() {
			return this.canEvolve;
		}
	}
}
