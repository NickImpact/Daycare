package com.nickimpact.daycare.sponge.ui;

import com.nickimpact.daycare.sponge.SpongeDaycarePlugin;
import com.nickimpact.daycare.sponge.implementation.SpongePen;
import com.nickimpact.daycare.sponge.implementation.SpongeRanch;
import com.nickimpact.impactor.api.building.Builder;
import org.spongepowered.api.entity.living.player.Player;

public interface PenUI {

	void open();

	void update();

	static PenUIBuilder builder() {
		return SpongeDaycarePlugin.getSpongeInstance().getService().getBuilderRegistry().createFor(PenUIBuilder.class);
	}

	interface PenUIBuilder extends Builder<PenUI> {

		PenUIBuilder ranch(SpongeRanch ranch);

		PenUIBuilder pen(SpongePen pen);

		PenUIBuilder viewer(Player viewer);

	}
}
