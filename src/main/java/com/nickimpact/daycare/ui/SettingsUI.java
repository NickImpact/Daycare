package com.nickimpact.daycare.ui;

import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.configuration.MsgConfigKeys;
import com.nickimpact.daycare.ranch.Ranch;
import com.nickimpact.daycare.utils.MessageUtils;
import com.nickimpact.impactor.gui.v2.Displayable;
import com.nickimpact.impactor.gui.v2.Icon;
import com.nickimpact.impactor.gui.v2.Layout;
import com.nickimpact.impactor.gui.v2.UI;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class SettingsUI implements Displayable {

	private Ranch.Settings settings;

	private UI display;

	private final Icon ON = Icon.from(ItemStack.builder().itemType(ItemTypes.STAINED_GLASS_PANE).add(Keys.DISPLAY_NAME, Text.EMPTY).add(Keys.DYE_COLOR, DyeColors.GREEN).build());
	private final Icon OFF = Icon.from(ItemStack.builder().itemType(ItemTypes.STAINED_GLASS_PANE).add(Keys.DISPLAY_NAME, Text.EMPTY).add(Keys.DYE_COLOR, DyeColors.RED).build());
	private final Icon MOVES = Icon.from(ItemStack.builder().itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:tm1").get()).build());
	private final Icon LEVEL = Icon.from(ItemStack.builder().itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:exp_share").get()).build());
	private final Icon EVOLVE = Icon.from(ItemStack.builder().itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:shiny_stone").get()).build());

	public SettingsUI(Player player, Ranch ranch) {
		this.settings = ranch.getSettings();

		MOVES.getDisplay().offer(Keys.DISPLAY_NAME, MessageUtils.fetchMsg(player, MsgConfigKeys.SETINGS_MOVES_TITLE));
		MOVES.getDisplay().offer(Keys.ITEM_LORE, MessageUtils.fetchMsgs(player, MsgConfigKeys.SETTINGS_MOVES_LORE));
		LEVEL.getDisplay().offer(Keys.DISPLAY_NAME, MessageUtils.fetchMsg(player, MsgConfigKeys.SETINGS_LEVEL_TITLE));
		LEVEL.getDisplay().offer(Keys.ITEM_LORE, MessageUtils.fetchMsgs(player, MsgConfigKeys.SETTINGS_LEVEL_LORE));
		EVOLVE.getDisplay().offer(Keys.DISPLAY_NAME, MessageUtils.fetchMsg(player, MsgConfigKeys.SETINGS_EVOLVE_TITLE));
		EVOLVE.getDisplay().offer(Keys.ITEM_LORE, MessageUtils.fetchMsgs(player, MsgConfigKeys.SETTINGS_EVOLVE_LORE));

		this.display = UI.builder()
				.title(MessageUtils.fetchMsg(player, MsgConfigKeys.SETTINGS_UI_TITLE))
				.dimension(InventoryDimension.of(9, 6))
				.build(DaycarePlugin.getInstance())
				.define(setupDisplay(player));
	}

	@Override
	public UI getDisplay() {
		return this.display;
	}

	private Layout setupDisplay(Player player) {
		Layout.Builder builder = Layout.builder()
				.dimension(InventoryDimension.of(9, 6))
				.border()
				.slot(LEVEL, 11)
				.slot(MOVES, 15)
				.column(Icon.BORDER, 4)
				.slot(EVOLVE, 40)
				.hollowSquare(Icon.BORDER, 37)
				.hollowSquare(Icon.BORDER, 43);

		Icon back = Icon.from(
				ItemStack.builder()
						.itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:eject_button").orElse(ItemTypes.BARRIER))
						.add(Keys.DISPLAY_NAME, MessageUtils.fetchMsg(player, MsgConfigKeys.ITEM_BACK))
						.build()
		);
		back.addListener(clickable -> {
			this.close(player);
			new RanchUI(player).open(player);
		});
		builder = builder.slot(back, 37);
		Icon confirm = Icon.from(ItemStack.builder().itemType(ItemTypes.DYE).add(Keys.DYE_COLOR, DyeColors.LIME).add(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "Confirm Selection")).build());
		confirm.addListener(clickable -> {
			this.settings.setCanLevel(this.display.getSlot(10) == ON);
			this.settings.setCanLearnMoves(this.display.getSlot(14) == ON);
			this.settings.setCanEvolve(this.display.getSlot(39) == ON);
			player.sendMessages(MessageUtils.fetchMsgs(player, MsgConfigKeys.SETTINGS_APPLY));
			this.close(player);
			new RanchUI(player).open(player);
		});
		builder = builder.slot(confirm, 43);

		if(settings.canLevel()) {
			builder = builder.hollowSquare(ON, 11);
		} else {
			builder = builder.hollowSquare(OFF, 11);
		}

		if(settings.canLearnMoves()) {
			builder = builder.hollowSquare(ON, 15);
		} else {
			builder = builder.hollowSquare(OFF, 15);
		}

		if(settings.canEvolve()) {
			builder = builder.hollowSquare(ON, 40);
		} else {
			builder = builder.hollowSquare(OFF, 40);
		}

		final Layout.Builder copy = builder;
		LEVEL.addListener(clickable -> {
			Layout.Builder copy2 = copy;
			if(this.display.getSlot(10) == OFF) {
				this.display.define(copy.hollowSquare(ON, 11).build());
			} else {
				copy2 = copy2.hollowSquare(OFF, 11);
				if(this.display.getSlot(14) == ON) {
					copy2 = copy2.hollowSquare(OFF, 15);
				}
				if(this.display.getSlot(39) == ON) {
					copy2 = copy2.hollowSquare(OFF, 40);
				}

				this.display.define(copy2.build());
			}
		});
		MOVES.addListener(clickable -> {
			if(this.display.getSlot(14) == OFF && this.display.getSlot(10) == ON) {
				this.display.define(copy.hollowSquare(ON, 15).build());
			} else {
				this.display.define(copy.hollowSquare(OFF, 15).build());
			}
		});
		EVOLVE.addListener(clickable -> {
			if(this.display.getSlot(39) == OFF && this.display.getSlot(10) == ON) {
				this.display.define(copy.hollowSquare(ON, 40).build());
			} else {
				this.display.define(copy.hollowSquare(OFF, 40).build());
			}
		});

		return builder.build();
	}
}
