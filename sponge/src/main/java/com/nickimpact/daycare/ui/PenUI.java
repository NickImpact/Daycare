package com.nickimpact.daycare.ui;

import com.nickimpact.daycare.SpongeDaycarePlugin;
import com.nickimpact.daycare.implementation.SpongePen;
import com.nickimpact.daycare.implementation.SpongeRanch;
import com.nickimpact.impactor.api.building.Builder;
import org.spongepowered.api.entity.living.player.Player;

public interface PenUI {

	void open();

	static PenUIBuilder builder() {
		return SpongeDaycarePlugin.getSpongeInstance().getService().getBuilderRegistry().createFor(PenUIBuilder.class);
	}

	interface PenUIBuilder extends Builder<PenUI> {

		PenUIBuilder ranch(SpongeRanch ranch);

		PenUIBuilder pen(SpongePen pen);

		PenUIBuilder viewer(Player viewer);

	}
}
