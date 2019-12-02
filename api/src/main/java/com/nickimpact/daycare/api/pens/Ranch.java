package com.nickimpact.daycare.api.pens;

import com.google.common.collect.Lists;
import com.nickimpact.daycare.api.configuration.ConfigKeys;
import com.nickimpact.daycare.api.util.PluginInstance;
import com.nickimpact.impactor.api.building.Builder;

import java.util.List;
import java.util.UUID;

public abstract class Ranch<T extends Pen> {

	private UUID identifier;

	private UUID owner;
	private List<T> pens;

	private Statistics stats;

	public Ranch(UUID uuid) {
		this.identifier = UUID.randomUUID();
		this.owner = uuid;
		this.pens = Lists.newArrayList();
		this.stats = new Statistics();

		for(int i = 0; i < PluginInstance.getPlugin().getConfiguration().get(ConfigKeys.NUM_PENS); i++) {
			this.pens.add(this.newPen(i + 1));
		}

		for(int i = 0; i < 2; i++) {
			this.pens.get(i).unlock();
		}
	}

	protected Ranch(UUID identifier, UUID owner, List<T> pens, Statistics stats) {
		this.identifier = identifier;
		this.owner = owner;
		this.pens = pens;
		this.stats = stats;
	}

	public abstract T newPen(int id);

	public UUID getIdentifier() {
		return this.identifier;
	}

	public UUID getOwnerUUID() {
		return this.owner;
	}

	public List<T> getPens() {
		return this.pens;
	}

	public Pen getPen(int index) {
		return this.pens.get(Math.min(Math.max(0, index), this.pens.size()));
	}

	public Statistics getStats() {
		return this.stats;
	}

	public boolean unlock(int index) {
		Pen pen = this.pens.get(index);
		if(pen.isUnlocked()) {
			return false;
		}

		PenUnlockModule module = PluginInstance.getPlugin().getService().getActiveModule();
		if(module.canPay(this.owner, index)) {
			if(module.pay(this.owner, index)) {
				pen.unlock();
				return true;
			}
		}

		return false;
	}

	public void shutdown() {

	}

	public static RanchBuilder builder() {
		return PluginInstance.getPlugin().getService().getBuilderRegistry().createFor(RanchBuilder.class);
	}

	public interface RanchBuilder extends Builder<Ranch> {

		RanchBuilder identifier(UUID identifier);

		RanchBuilder owner(UUID owner);

		RanchBuilder pens(List<Pen> pens);

		RanchBuilder stats(Statistics stats);

	}

}
