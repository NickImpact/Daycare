package com.nickimpact.daycare.storage;


import com.nickimpact.daycare.ranch.Pen;
import com.nickimpact.daycare.ranch.Ranch;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Storage {

    String getName();

    /**
     * This method is to initialize our storage provider, loading anything necessary
     * about the provider.
     */
    void init();

    /**
     * This method is to ensure we properly close our connection when the plugin is either
     * no longer in use or in the middle of server shutdown.
     */
    void shutdown();

	CompletableFuture<Void> addRanch(Ranch ranch);

	CompletableFuture<Void> updateRanch(Ranch ranch);

	CompletableFuture<Void> deleteRanch(UUID uuid);

	CompletableFuture<Ranch> getRanch(UUID uuid);

	CompletableFuture<List<Ranch>> getAllRanches();

    /**
     * This method is meant to clean out the daycare, along with logs if the passed variable
     * is <code>true</code>.
     *
     * @return <code>true</code> on successful purge, <code>false</code> otherwise
     */
    CompletableFuture<Void> purge(boolean logs);

    /**
     * Attempts to save all data awaiting an update to the storage provider. However, for flatfile,
     * we will need to ensure we have all data present at time of save to ensure all is saved
     * properly.
     *
     * @return <code>true</code> on successful save, <code>false</code> otherwise
     */
    CompletableFuture<Void> save();
}
