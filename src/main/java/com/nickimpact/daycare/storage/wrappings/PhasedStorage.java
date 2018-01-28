package com.nickimpact.daycare.storage.wrappings;

import com.nickimpact.daycare.ranch.Ranch;
import com.nickimpact.daycare.storage.Storage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PhasedStorage implements Storage {

	public static PhasedStorage of(Storage storage) {
		return new PhasedStorage(storage);
	}

	private final Storage delegate;

	private final Phaser phaser = new Phaser();

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
	public void init() {
		delegate.init();
	}

	@Override
	public void shutdown() {
		try {
			phaser.awaitAdvanceInterruptibly(phaser.getPhase(), 10, TimeUnit.SECONDS);
		} catch (InterruptedException | TimeoutException e) {
			e.printStackTrace();
		}

		delegate.shutdown();
	}

	@Override
	public CompletableFuture<Void> addRanch(Ranch ranch) {
		phaser.register();
		try {
			return delegate.addRanch(ranch);
		} finally {
			phaser.arriveAndDeregister();
		}
	}

	@Override
	public CompletableFuture<Void> updateRanch(Ranch ranch) {
		phaser.register();
		try {
			return delegate.updateRanch(ranch);
		} finally {
			phaser.arriveAndDeregister();
		}
	}

	@Override
	public CompletableFuture<Void> deleteRanch(UUID uuid) {
		phaser.register();
		try {
			return delegate.deleteRanch(uuid);
		} finally {
			phaser.arriveAndDeregister();
		}
	}

	@Override
	public CompletableFuture<Ranch> getRanch(UUID uuid) {
		phaser.register();
		try {
			return delegate.getRanch(uuid);
		} finally {
			phaser.arriveAndDeregister();
		}
	}

	@Override
	public CompletableFuture<List<Ranch>> getAllRanches() {
		phaser.register();
		try {
			return delegate.getAllRanches();
		} finally {
			phaser.arriveAndDeregister();
		}
	}

	@Override
	public CompletableFuture<Void> purge(boolean logs) {
		phaser.register();
		try {
			return delegate.purge(logs);
		} finally {
			phaser.arriveAndDeregister();
		}
	}

	@Override
	public CompletableFuture<Void> save() {
		phaser.register();
		try {
			return delegate.save();
		} finally {
			phaser.arriveAndDeregister();
		}
	}
}
