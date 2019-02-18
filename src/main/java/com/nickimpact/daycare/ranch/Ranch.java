package com.nickimpact.daycare.ranch;

import com.google.common.collect.Lists;
import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.api.breeding.BreedStyle;
import com.nickimpact.daycare.api.events.DaycareEvent;
import com.nickimpact.daycare.api.pens.PenUnlockModule;
import com.nickimpact.daycare.configuration.ConfigKeys;
import com.nickimpact.daycare.exceptions.AlreadyUnlockedException;
import com.nickimpact.daycare.impl.EconomicPenModule;
import com.nickimpact.daycare.stats.Statistics;
import lombok.Getter;
import lombok.Setter;
import org.mariuszgromada.math.mxparser.Expression;
import org.mariuszgromada.math.mxparser.Function;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.economy.account.UniqueAccount;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.Instant;
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
	@Getter private static PenUnlockModule unlocker;

	@Setter private transient BreedStyle style;

	public static void registerUnlocker() {
		String chosen = DaycarePlugin.getInstance().getConfig().get(ConfigKeys.RANCH_UNLOCK_MODULE).toLowerCase();
		if(DaycarePlugin.getInstance().getService().getUnlockModules().containsKey(chosen)) {
			unlocker = DaycarePlugin.getInstance().getService().getUnlockModules().get(chosen);
		} else {
			unlocker = DaycarePlugin.getInstance().getService().getUnlockModules().get("economic");
		}
	}

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

		if(unlocker.canUnlock(this.ownerUUID, id)) {
			if(unlocker.process(this.ownerUUID, id)) {
				pen.unlock();
				return true;
			}
		}

		return false;
	}

	public void addToPen(Pokemon pokemon, int id) {
		Pen pen = this.pens.get(id);

		if(!pen.getAtPosition(1).isPresent()) {
			pen.setSlot1(pokemon);
		} else {
			pen.setSlot2(pokemon);
		}

		// Update the date so that the timer can start evaluating breeding conditions
		if(pen.isFull()) {
			pen.initialize(ownerUUID);
		}

		DaycarePlugin.getInstance().getStorage().updateRanch(this);
	}

	public Pen getPen(int id) {
		return this.pens.get(id);
	}

	public void shutdown() {
		this.pens.forEach(Pen::halt);
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
