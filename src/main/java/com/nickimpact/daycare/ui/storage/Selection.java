package com.nickimpact.daycare.ui.storage;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
class Selection {
	private final Pokemon pokemon;
	private final int slot;
}
