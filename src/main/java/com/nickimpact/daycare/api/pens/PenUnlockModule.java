package com.nickimpact.daycare.api.pens;

import org.spongepowered.api.text.Text;

import java.util.UUID;

public interface PenUnlockModule {

	Text getTranslatedRequirement(int id);

	boolean canUnlock(UUID uuid, int id);

	boolean process(UUID uuid, int id);
}
