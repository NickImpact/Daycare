package com.nickimpact.daycare.ui;

import com.nickimpact.daycare.implementation.SpongePen;
import com.nickimpact.impactor.sponge.ui.SpongeUI;
import org.spongepowered.api.entity.living.player.Player;

public class PenUI {

	private SpongeUI display;
	private Player viewer;
	private SpongePen pen;

	public PenUI(Player viewer, SpongePen pen) {
		this.viewer = viewer;
		this.pen = pen;
	}

	public void open() {
		this.display.open(this.viewer);
	}
}
