package com.nickimpact.daycare.ui.storage;

import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
class Selection {
	private final EntityPixelmon pokemon;
	private final int slot;
}
