package com.nickimpact.daycare.common.storage;

import com.nickimpact.daycare.api.IDaycarePlugin;
import com.nickimpact.daycare.api.pens.DaycareNPC;
import com.nickimpact.daycare.api.pens.Ranch;
import com.nickimpact.daycare.api.storage.IDaycareStorage;
import com.nickimpact.daycare.api.util.ThrowingRunnable;
import com.nickimpact.daycare.common.storage.implementation.StorageImplementation;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@RequiredArgsConstructor
public class DaycareStorage implements IDaycareStorage {

	private final IDaycarePlugin plugin;
	private final StorageImplementation implementation;

	/**
	 * Attempts to initialize the storage implementation
	 */
	public void init() {
		try {
			this.implementation.init();
		} catch (Exception e) {
			// Log the failure
			e.printStackTrace();
		}
	}

	/**
	 * Attempts to shutdown the storage implementation
	 */
	public void shutdown() {
		try {
			this.implementation.shutdown();
		} catch (Exception e) {
			// Log the failure
			e.printStackTrace();
		}
	}

	/**
	 * Represents any properties which might be set against a storage
	 * implementation.
	 *
	 * @return A mapping of flags to values representing storage implementation
	 * properties
	 */
	public Map<String, String> getMeta() {
		return this.implementation.getMeta();
	}

	private <T> CompletableFuture<T> makeFuture(Callable<T> supplier) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return supplier.call();
			} catch (Exception e) {
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				}
				throw new CompletionException(e);
			}
		});
	}

	private CompletableFuture<Void> makeFuture(ThrowingRunnable runnable) {
		return CompletableFuture.runAsync(() -> {
			try {
				runnable.run();
			} catch (Exception e) {
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				}
				throw new CompletionException(e);
			}
		});
	}

	@Override
	public CompletableFuture<Boolean> addRanch(Ranch ranch) {
		return this.makeFuture(() -> this.implementation.addRanch(ranch));
	}

	@Override
	public CompletableFuture<Boolean> updateRanch(Ranch ranch) {
		return this.makeFuture(() -> this.implementation.updateRanch(ranch));
	}

	@Override
	public CompletableFuture<Boolean> deleteRanch(Ranch ranch) {
		return this.makeFuture(() -> this.implementation.deleteRanch(ranch));
	}

	@Override
	public CompletableFuture<Ranch> getRanch(UUID player) {
		return this.makeFuture(() -> this.implementation.getRanch(player));
	}

	@Override
	public CompletableFuture<Boolean> addNPC(DaycareNPC npc) {
		return this.makeFuture(() -> this.implementation.addNPC(npc));
	}

	@Override
	public CompletableFuture<Boolean> deleteNPC(DaycareNPC npc) {
		return this.makeFuture(() -> this.implementation.deleteNPC(npc));
	}

	@Override
	public CompletableFuture<List<DaycareNPC>> getAllNPCs() {
		return this.makeFuture(this.implementation::getNPCs);

	}
}
