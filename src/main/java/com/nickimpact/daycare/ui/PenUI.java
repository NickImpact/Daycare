package com.nickimpact.daycare.ui;

import com.nickimpact.daycare.api.gui.Icon;
import com.nickimpact.daycare.api.gui.InventoryBase;
import com.nickimpact.daycare.ranch.Pen;
import com.nickimpact.daycare.ranch.Pokemon;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

/**
 * (Some note will appear here)
 *
 * @author NickImpact (Nick DeGruccio)
 */
public class PenUI extends InventoryBase {

	/** The focus point for this UI */
	private Pen pen;

	public PenUI(Player player, Pen pen) {
		super(player);
		this.pen = pen;

		this.drawBorder();
		this.drawPokemon();
	}

	@Override
	public Text getTitle() {
		return Text.of(TextColors.RED, "Daycare ", TextColors.GRAY, "\u00BB ", TextColors.DARK_AQUA, "Pen");
	}

	@Override
	public int getSize() {
		return 5;
	}

	private void drawPokemon() {
		Optional<Pokemon> slot1 = this.pen.getAtPosition(1);
		Optional<Pokemon> slot2 = this.pen.getAtPosition(2);

		Icon s1 = slot1.map(pokemon -> StandardIcons.getPicture(11, pokemon.getPokemon()))
				.orElseGet(() -> new Icon(11, ItemStack.builder().itemType(ItemTypes.BARRIER).add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Empty")).build()));

		Icon s2 = slot2.map(pokemon -> StandardIcons.getPicture(15, pokemon.getPokemon()))
				.orElseGet(() -> new Icon(15, ItemStack.builder().itemType(ItemTypes.BARRIER).add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Empty")).build()));

		slot1.ifPresent(pokemon -> {
			s1.addListener(clickable -> {

			});
		});

		slot2.ifPresent(pokemon -> {
			s2.addListener(clickable -> {

			});
		});
	}

	private void drawBorder() {
		Text title = Text.EMPTY;
		DyeColor coloring = DyeColors.BLACK;
		if(pen.isFull()) {
			if(this.pen.canBreed()) {
				coloring = DyeColors.GREEN;
				title = Text.of(TextColors.GREEN, "Breeding...");
			} else {
				coloring = DyeColors.RED;
				title = Text.of(TextColors.RED, "Unable to breed...");
			}
		}

		this.drawBorder(this.getSize(), coloring, title);
	}
}
