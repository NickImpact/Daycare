package com.nickimpact.daycare.common.storage.implementation;

import com.nickimpact.daycare.api.pens.DaycareNPC;
import com.nickimpact.daycare.api.pens.Ranch;
import com.nickimpact.impactor.api.plugin.ImpactorPlugin;

import java.util.*;

public interface StorageImplementation {

	ImpactorPlugin getPlugin();

	String getName();

	void init() throws Exception;

	void shutdown() throws Exception;

	default Map<String, String> getMeta() {
		return Collections.emptyMap();
	}

	boolean addRanch(Ranch ranch) throws Exception;

	boolean updateRanch(Ranch ranch) throws Exception;

	boolean deleteRanch(Ranch ranch) throws Exception;

	Optional<Ranch> getRanch(UUID player) throws Exception;

	boolean addNPC(DaycareNPC npc) throws Exception;

	boolean deleteNPC(DaycareNPC npc) throws Exception;

	List<DaycareNPC> getNPCs() throws Exception;

}
