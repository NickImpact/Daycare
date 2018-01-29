package com.nickimpact.daycare.ui;

import com.nickimpact.daycare.api.gui.Icon;
import com.nickimpact.daycare.api.gui.InventoryBase;
import com.nickimpact.daycare.ranch.Pen;
import com.nickimpact.daycare.ranch.Pokemon;
import com.nickimpact.daycare.ranch.Ranch;
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

	/** The ranch being focused on */
	private Ranch ranch;

	/** The focus point for this UI */
	private Pen pen;

	public PenUI(Player player, Ranch ranch, Pen pen) {
		super(player);
		this.ranch = ranch;
		this.pen = pen;

		this.drawBorder();
		this.drawPokemon();
		this.drawEgg();
	}

	@Override
	public Text getTitle() {
		return Text.of(TextColors.RED, "Daycare ", TextColors.GRAY, "\u00BB ", TextColors.DARK_AQUA, "Pen");
	}

	@Override
	public int getSize() {
		return 6;
	}

	private void drawPokemon() {
		Optional<Pokemon> slot1 = this.pen.getAtPosition(1);
		Optional<Pokemon> slot2 = this.pen.getAtPosition(2);

		Icon s1 = slot1.map(pokemon -> StandardIcons.getPicture(player, 11, pokemon.getPokemon()))
				.orElseGet(() -> new Icon(11, ItemStack.builder().itemType(ItemTypes.BARRIER).add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Empty")).build()));

		Icon s2 = slot2.map(pokemon -> StandardIcons.getPicture(player, 15, pokemon.getPokemon()))
				.orElseGet(() -> new Icon(15, ItemStack.builder().itemType(ItemTypes.BARRIER).add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Empty")).build()));

		slot1.ifPresent(pokemon -> {
			s1.addListener(clickable -> {
				clickable.getPlayer().closeInventory();
				clickable.getPlayer().openInventory(new SelectionUI(player, ranch, pen, pokemon).getInventory());
			});
		});

		slot2.ifPresent(pokemon -> {
			s2.addListener(clickable -> {
				clickable.getPlayer().closeInventory();
				clickable.getPlayer().openInventory(new SelectionUI(player, ranch, pen, pokemon).getInventory());
			});
		});

		this.addIcon(s1);
		this.addIcon(s2);
	}

	private void drawEgg() {
		Optional<Pokemon> optEgg = this.pen.getEgg();

		Icon icon = optEgg.map(egg -> StandardIcons.getPicture(player, 22, egg.getPokemon()))
				.orElseGet(() -> new Icon(22, ItemStack.builder().itemType(ItemTypes.BARRIER).add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "No Egg Available...")).build()));

		optEgg.ifPresent(egg -> {
			icon.addListener(clickable -> {

			});
		});

		this.addIcon(icon);
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

		ItemStack pane = ItemStack.builder().itemType(ItemTypes.STAINED_GLASS_PANE).add(Keys.DISPLAY_NAME, Text.EMPTY).add(Keys.DYE_COLOR, DyeColors.BLACK).build();
		for(int i = 28; i < 35; i++) {
			this.addIcon(new Icon(i, pane));
		}
	}
}
