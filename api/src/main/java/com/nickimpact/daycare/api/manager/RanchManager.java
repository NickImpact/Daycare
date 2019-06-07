package com.nickimpact.daycare.api.manager;

import com.google.common.collect.Lists;
import com.nickimpact.daycare.api.pens.Ranch;
import com.nickimpact.daycare.api.util.PluginInstance;

import java.util.List;
import java.util.UUID;

public class RanchManager {

	private List<Ranch> ranches = Lists.newArrayList();

	public List<Ranch> getLoadedRanches() {
		return this.ranches;
	}

	public void addRanch(Ranch ranch) {

	}

	public void readRanch(UUID player) {
		PluginInstance.getPlugin().getService().getStorage().getRanch(player).thenAccept(ranch -> ranches.add(ranch));
	}
}
