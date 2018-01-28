package com.nickimpact.daycare.storage;

import com.google.common.base.Throwables;
import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.ranch.Ranch;
import com.nickimpact.daycare.storage.dao.AbstractDao;
import com.nickimpact.daycare.storage.wrappings.PhasedStorage;
import com.nickimpact.daycare.utils.MessageUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.spongepowered.api.Sponge;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AbstractStorage implements Storage {

	public static Storage create(DaycarePlugin plugin, AbstractDao backing) {
		return PhasedStorage.of(new AbstractStorage(plugin, backing));
	}

	private final DaycarePlugin plugin;
	private final AbstractDao dao;

	private <T> CompletableFuture<T> makeFuture(Callable<T> supplier) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return supplier.call();
			} catch (Exception e) {
				Throwables.propagateIfPossible(e);
				throw new CompletionException(e);
			}
		}, DaycarePlugin.getInstance().getAsyncExecutorService());
	}

	private CompletableFuture<Void> makeFuture(ThrowingRunnable runnable) {
		return CompletableFuture.runAsync(() -> {
			try {
				runnable.run();
			} catch (Exception e) {
				Throwables.propagateIfPossible(e);
				throw new CompletionException(e);
			}
		}, DaycarePlugin.getInstance().getAsyncExecutorService());
	}

	private interface ThrowingRunnable {
		void run() throws Exception;
	}

	@Override
	public String getName() {
		return dao.getName();
	}

	@Override
	public void init() {
		try {
			dao.init();
		} catch (Exception e) {
			MessageUtils.genAndSendErrorMessage(
					"Storage Init Error",
					"Failed to load storage dao",
					"Error report is as follows: "
			);
			e.printStackTrace();
		}
	}

	@Override
	public void shutdown() {
		try {
			dao.shutdown();
		} catch (Exception e) {
			MessageUtils.genAndSendErrorMessage(
					"Storage Shutdown Error",
					"Failed to close storage dao",
					"Error report is as follows: "
			);
			e.printStackTrace();
		}
	}

	@Override
	public CompletableFuture<Void> addRanch(Ranch ranch) {
		return makeFuture(() -> dao.addRanch(ranch));
	}

	@Override
	public CompletableFuture<Void> updateRanch(Ranch ranch) {
		return makeFuture(() -> dao.updateRanch(ranch));
	}

	@Override
	public CompletableFuture<Void> deleteRanch(UUID uuid) {
		return makeFuture(() -> dao.deleteRanch(uuid));
	}

	@Override
	public CompletableFuture<Ranch> getRanch(UUID uuid) {
		return makeFuture(() -> dao.getRanch(uuid));
	}

	@Override
	public CompletableFuture<List<Ranch>> getAllRanches() {
		return makeFuture(dao::getAllRanches);
	}

	@Override
	public CompletableFuture<Void> purge(boolean logs) {
		return makeFuture(() -> dao.purge(logs));
	}

	@Override
	public CompletableFuture<Void> save() {
		return makeFuture(dao::save);
	}
}
