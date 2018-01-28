package com.nickimpact.daycare.ui;

import com.nickimpact.daycare.api.gui.InventoryBase;
import com.nickimpact.daycare.ranch.Pen;
import com.nickimpact.daycare.ranch.Pokemon;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

/**
 * (Some note will appear here)
 *
 * @author NickImpact (Nick DeGruccio)
 */
public class SelectionUI extends InventoryBase {

	/** The pen to return to, if the back button is selected */
	private Pen pen;

	/** The pokemon of focus in this UI */
	private Pokemon pokemon;

	public SelectionUI(Player player, Pen pen, Pokemon pokemon) {
		super(player);
	}

	@Override
	public Text getTitle() {
		return null;
	}

	@Override
	public int getSize() {
		return 0;
	}
}
