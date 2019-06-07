package com.nickimpact.daycare.api.pens;

import java.util.UUID;

public interface PenUnlockModule<T> {

	T getRequirement();

	boolean canUnlock(UUID owner, int pen);

	boolean process(UUID owner, int pen);

}
