package com.nickimpact.daycare.ui;

import com.nickimpact.daycare.api.gui.InventoryBase;
import com.nickimpact.daycare.ranch.Pen;
import com.nickimpact.daycare.ranch.Pokemon;
import com.nickimpact.daycare.ranch.Ranch;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

/**
 * (Some note will appear here)
 *
 * @author NickImpact (Nick DeGruccio)
 */
public class SelectionUI extends InventoryBase {

	/** The ranch being focused on */
	private Ranch ranch;

	/** The pen to return to, if the back button is selected */
	private Pen pen;

	/** The pokemon of focus in this UI */
	private Pokemon pokemon;

	public SelectionUI(Player player, Ranch ranch, Pen pen, Pokemon pokemon) {
		super(player);
		this.ranch = ranch;
		this.pen = pen;
		this.pokemon = pokemon;
	}

	@Override
	public Text getTitle() {
		return Text.of(TextColors.RED, "Daycare ", TextColors.GRAY, "\u00BB ", TextColors.DARK_AQUA, "Main Menu");
	}

	@Override
	public int getSize() {
		return 3;
	}
}
