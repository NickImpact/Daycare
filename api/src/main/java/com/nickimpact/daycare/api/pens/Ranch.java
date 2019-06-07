package com.nickimpact.daycare.api.pens;

import com.google.common.collect.Lists;
import com.nickimpact.daycare.api.configuration.ConfigKeys;
import com.nickimpact.daycare.api.util.PluginInstance;

import java.util.List;
import java.util.UUID;

public abstract class Ranch<T extends Pen<?, ?>> {

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
			this.pens.add(this.newPen());
		}
	}

	public abstract T newPen();

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
		if(module.canUnlock(this.owner, index)) {
			if(module.process(this.owner, index)) {
				pen.unlock();
				return true;
			}
		}

		return false;
	}

	public void shutdown() {

	}

	public static RanchBuilder builder() {
		return new RanchBuilder();
	}

	public static class RanchBuilder {

	}

}
