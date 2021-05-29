package com.nickimpact.daycare.sponge.ui;

import com.nickimpact.daycare.sponge.SpongeDaycarePlugin;
import com.nickimpact.daycare.sponge.configuration.MsgConfigKeys;
import com.nickimpact.daycare.sponge.implementation.SpongePen;
import com.nickimpact.daycare.sponge.implementation.SpongeRanch;
import com.nickimpact.daycare.sponge.utils.SpongeItemTypeUtil;
import com.nickimpact.daycare.sponge.utils.TextParser;
import net.impactdev.impactor.sponge.ui.SpongeIcon;
import net.impactdev.impactor.sponge.ui.SpongeLayout;
import net.impactdev.impactor.sponge.ui.SpongeUI;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;

public class SettingsUI {

	private SpongeUI display;
	private Player viewer;
	private SpongeRanch ranch;
	private SpongePen pen;

	private static final SpongeIcon ON = new SpongeIcon(ItemStack.builder().itemType(ItemTypes.STAINED_GLASS_PANE)
			.add(Keys.DISPLAY_NAME, Text.EMPTY)
			.add(Keys.DYE_COLOR, DyeColors.GREEN)
			.build()
	);
	private static final SpongeIcon OFF = new SpongeIcon(ItemStack.builder().itemType(ItemTypes.STAINED_GLASS_PANE)
			.add(Keys.DISPLAY_NAME, Text.EMPTY)
			.add(Keys.DYE_COLOR, DyeColors.RED)
			.build()
	);

	private final SpongeIcon MOVES = new SpongeIcon(ItemStack.builder()
			.itemType(SpongeItemTypeUtil.getOrDefaultItemTypeFromID("pixelmon:tm1"))
			.add(Keys.DISPLAY_NAME, TextParser.parse(TextParser.read(MsgConfigKeys.SETTINGS_MOVES_TITLE)))
			.add(Keys.ITEM_LORE, TextParser.parse(TextParser.read(MsgConfigKeys.SETTINGS_MOVES_LORE)))
			.build()
	);
	private final SpongeIcon LEVEL = new SpongeIcon(ItemStack.builder()
			.itemType(SpongeItemTypeUtil.getOrDefaultItemTypeFromID("pixelmon:exp_share"))
			.add(Keys.DISPLAY_NAME, TextParser.parse(TextParser.read(MsgConfigKeys.SETTINGS_LEVEL_TITLE)))
			.add(Keys.ITEM_LORE, TextParser.parse(TextParser.read(MsgConfigKeys.SETTINGS_LEVEL_LORE)))
			.build()
	);
	private final SpongeIcon EVOLVE = new SpongeIcon(ItemStack.builder()
			.itemType(SpongeItemTypeUtil.getOrDefaultItemTypeFromID("pixelmon:shiny_stone"))
			.add(Keys.DISPLAY_NAME, TextParser.parse(TextParser.read(MsgConfigKeys.SETTINGS_EVOLVE_TITLE)))
			.add(Keys.ITEM_LORE, TextParser.parse(TextParser.read(MsgConfigKeys.SETTINGS_EVOLVE_LORE)))
			.build()
	);

	public SettingsUI(Player viewer, SpongeRanch ranch, SpongePen pen) {
		this.ranch = ranch;
		this.pen = pen;
		this.viewer = viewer;

		this.display = this.createDisplay();
		this.display.define(this.layout());
	}

	public void open() {
		this.display.open(this.viewer);
	}

	private SpongeUI createDisplay() {
		return SpongeUI.builder().dimension(InventoryDimension.of(9, 6)).title(TextParser.parse(TextParser.read(MsgConfigKeys.SETTINGS_UI_TITLE))).build();
	}

	private SpongeLayout layout() {
		SpongeLayout.SpongeLayoutBuilder slb = SpongeLayout.builder();
		slb.border();
		slb.slot(LEVEL, 11).slot(MOVES, 15);
		slb.column(SpongeIcon.BORDER, 4).slot(EVOLVE, 40);
		slb.hollowSquare(SpongeIcon.BORDER, 37).hollowSquare(SpongeIcon.BORDER, 43);

		SpongeIcon back = new SpongeIcon(ItemStack.builder()
				.itemType(SpongeItemTypeUtil.getOrDefaultItemTypeFromID("pixelmon:eject_button"))
				.add(Keys.DISPLAY_NAME, TextParser.parse(TextParser.read(MsgConfigKeys.ITEM_BACK)))
				.build());
		back.addListener(clickable -> {
			PenUI.builder().ranch(ranch).pen(pen).viewer(viewer).build().open();
		});
		slb.slot(back, 37);

		SpongeIcon confirm = new SpongeIcon(ItemStack.builder().itemType(ItemTypes.DYE).add(Keys.DYE_COLOR, DyeColors.LIME).add(Keys.DISPLAY_NAME, TextParser.parse(TextParser.read(MsgConfigKeys.CONFIRM))).build());
		confirm.addListener(clickable -> {
			this.pen.getSettings().setCanLevel(this.display.getIcon(10).get() == ON);
			this.pen.getSettings().setCanLearnMoves(this.display.getIcon(14).get() == ON);
			this.pen.getSettings().setCanEvolve(this.display.getIcon(39).get() == ON);
			this.viewer.sendMessages(TextParser.parse(TextParser.read(MsgConfigKeys.SETTINGS_APPLY)));
			SpongeDaycarePlugin.getSpongeInstance().getService().getStorage().updateRanch(this.ranch);
			PenUI.builder().ranch(ranch).pen(pen).viewer(viewer).build().open();
		});
		slb.slot(confirm, 43);

		if(this.pen.getSettings().canLevel()) {
			slb.hollowSquare(ON, 11);
		} else {
			slb.hollowSquare(OFF, 11);
		}

		if(this.pen.getSettings().canLearnMoves()) {
			slb.hollowSquare(ON, 15);
		} else {
			slb.hollowSquare(OFF, 15);
		}

		if(this.pen.getSettings().canEvolve()) {
			slb.hollowSquare(ON, 40);
		} else {
			slb.hollowSquare(OFF, 40);
		}

		final SpongeLayout.SpongeLayoutBuilder copy = slb;
		LEVEL.addListener(clickable -> {
			if(this.display.getIcon(10).get() == OFF) {
				this.display.define(copy.hollowSquare(ON, 11).build());
			} else {
				copy.hollowSquare(OFF, 11);
				if(this.display.getIcon(14).get() == ON) {
					copy.hollowSquare(OFF, 15);
				}

				if(this.display.getIcon(39).get() == ON) {
					copy.hollowSquare(OFF, 40);
				}
				this.display.define(copy.build());
			}
		});
		MOVES.addListener(clickable -> {
			if(this.display.getIcon(14).get() == OFF && this.display.getIcon(10).get() == ON) {
				copy.hollowSquare(ON, 15);
			} else {
				copy.hollowSquare(OFF, 15);
			}
			this.display.define(copy.build());
		});

		EVOLVE.addListener(clickable -> {
			if(this.display.getIcon(39).get() == OFF && this.display.getIcon(10).get() == ON) {
				copy.hollowSquare(ON, 40);
			} else {
				copy.hollowSquare(OFF, 40);
			}
			this.display.define(copy.build());
		});

		return slb.build();
	}
}
