package com.nickimpact.daycare.implementation;

import com.nickimpact.daycare.api.pens.Ranch;
import com.nickimpact.impactor.api.json.JsonTyping;

import java.util.UUID;

@JsonTyping("daycare_sponge_ranch")
public class SpongeRanch extends Ranch<SpongePen> {

	public SpongeRanch(UUID uuid) {
		super(uuid);
	}

	@Override
	public SpongePen newPen() {
		return new SpongePen();
	}
}
