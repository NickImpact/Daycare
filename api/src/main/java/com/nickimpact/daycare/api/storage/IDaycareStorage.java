package com.nickimpact.daycare.api.storage;

import com.nickimpact.daycare.api.pens.DaycareNPC;
import com.nickimpact.daycare.api.pens.Ranch;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IDaycareStorage {

	void init();

	void shutdown();

	Map<String, String> getMeta();

	CompletableFuture<Boolean> addRanch(Ranch ranch);

	CompletableFuture<Boolean> updateRanch(Ranch ranch);

	CompletableFuture<Boolean> deleteRanch(Ranch ranch);

	CompletableFuture<Ranch> getRanch(UUID player);

	CompletableFuture<Boolean> addNPC(DaycareNPC npc);

	CompletableFuture<Boolean> deleteNPC(DaycareNPC npc);

	CompletableFuture<List<DaycareNPC>> getAllNPCs();

}
