package com.nickimpact.daycare.sponge.observing;

import com.google.common.collect.Maps;
import com.nickimpact.daycare.sponge.ui.PenUI;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PenObservers {

	private static Map<UUID, PenUI> observers = Maps.newHashMap();

	public static void addObserver(UUID uuid, PenUI observer) {
		observers.put(uuid, observer);
	}

	public static Optional<PenUI> getObserver(UUID uuid) {
		return Optional.ofNullable(observers.get(uuid));
	}

	public static void removeObserver(UUID uuid) {
		observers.remove(uuid);
	}
}
