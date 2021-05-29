package com.nickimpact.daycare.sponge.ui;

import com.nickimpact.daycare.sponge.SpongeDaycarePlugin;
import com.nickimpact.daycare.sponge.implementation.SpongePen;
import com.nickimpact.daycare.sponge.implementation.SpongeRanch;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.utilities.Builder;
import org.spongepowered.api.entity.living.player.Player;

public interface PenUI {

	void open();

	void update();

	static PenUIBuilder builder() {
		return Impactor.getInstance().getRegistry().createBuilder(PenUIBuilder.class);
	}

	interface PenUIBuilder extends Builder<PenUI, PenUIBuilder> {

		PenUIBuilder ranch(SpongeRanch ranch);

		PenUIBuilder pen(SpongePen pen);

		PenUIBuilder viewer(Player viewer);

	}
}
