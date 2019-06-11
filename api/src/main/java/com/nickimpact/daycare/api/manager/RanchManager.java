package com.nickimpact.daycare.api.manager;

import com.google.common.collect.Lists;
import com.nickimpact.daycare.api.pens.Ranch;
import com.nickimpact.daycare.api.util.PluginInstance;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class RanchManager {

	private List<Ranch> ranches = Lists.newArrayList();

	public List<Ranch> getLoadedRanches() {
		return this.ranches;
	}

	public void addRanch(Ranch ranch) {
		this.ranches.add(ranch);
	}

	public boolean readRanch(UUID player) {
		AtomicBoolean result = new AtomicBoolean(false);
		PluginInstance.getPlugin().getService().getStorage().getRanch(player).thenAccept(ranch -> {
			ranches.add(ranch);
			result.set(true);
		});

		return result.get();
	}

	public Optional<Ranch> getRanch(UUID uuid) {
		return ranches.stream().filter(ranch -> ranch.getOwnerUUID().equals(uuid)).findAny();
	}
}
