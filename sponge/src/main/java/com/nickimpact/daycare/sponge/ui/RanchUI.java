package com.nickimpact.daycare.sponge.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.daycare.sponge.SpongeDaycarePlugin;
import com.nickimpact.daycare.api.pens.DaycarePokemonWrapper;
import com.nickimpact.daycare.sponge.configuration.MsgConfigKeys;
import com.nickimpact.daycare.sponge.implementation.SpongePen;
import com.nickimpact.daycare.sponge.implementation.SpongeRanch;
import com.nickimpact.daycare.sponge.utils.SpongeItemTypeUtil;
import com.nickimpact.daycare.sponge.utils.TextParser;
import net.impactdev.impactor.sponge.ui.SpongeIcon;
import net.impactdev.impactor.sponge.ui.SpongeLayout;
import net.impactdev.impactor.sponge.ui.SpongePage;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class RanchUI {

	private SpongePage<SpongePen> page;
	private Player viewer;

	private SpongeRanch ranch;

	public RanchUI(Player viewer) {
		this.viewer = viewer;
		this.ranch = this.getRanch(viewer);
		this.page = this.createPage();

		this.page.applier(pen -> this.createPenIcon(pen, pen.getID()));
		this.page.define(ranch.getPens());
	}

	public void open() {
		this.page.open();
	}

	private SpongeRanch getRanch(Player player) {
		return (SpongeRanch) SpongeDaycarePlugin.getSpongeInstance().getService().getRanchManager().getRanch(player.getUniqueId()).orElseThrow(() -> new RuntimeException("Unable to find " + player.getName() + "'s ranch"));
	}

	private SpongePage<SpongePen> createPage() {
		SpongePage.SpongePageBuilder spb = SpongePage.builder();
		spb.title(TextParser.parse(TextParser.read(MsgConfigKeys.RANCH_UI_TITLE)));
		spb.contentZone(InventoryDimension.of(7, 3));
		spb.offsets(1);
		spb.lastPage(SpongeItemTypeUtil.getOrDefaultItemTypeFromID("pixelmon:trade_holder_left"), 50);
		spb.currentPage(SpongeItemTypeUtil.getOrDefaultItemTypeFromID("pixelmon:trade_monitor"), 51);
		spb.nextPage(SpongeItemTypeUtil.getOrDefaultItemTypeFromID("pixelmon:trade_holder_right"), 52);
		spb.view(SpongeLayout.builder().dimension(9, 6)
				.rows(SpongeIcon.BORDER, 0, 4)
				.columns(SpongeIcon.BORDER, 0, 8)
				.slot(SpongeIcon.BORDER, 49)
				.slot(new SpongeIcon(ItemStack.builder().itemType(ItemTypes.BARRIER).add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Coming Soon...")).build()), 47)
				.build()
		);
		spb.viewer(this.viewer);
		return spb.build();
	}

	private SpongeIcon createPenIcon(SpongePen pen, int index) {
		List<Supplier<Object>> sources = Lists.newArrayList();
		sources.add(() -> index);
		sources.add(() -> SpongeDaycarePlugin.getSpongeInstance().getService().getActiveModule().getRequirement(index - 1));
		sources.add(() -> pen);

		if(pen.isUnlocked()) {
			List<Text> lore = Lists.newArrayList();
			lore.add(this.forSlot(pen, 1));
			lore.add(this.forSlot(pen, 2));

			if(pen.getEgg().isPresent()) {
				lore.add(Text.EMPTY);
				lore.add(TextParser.parse(TextParser.read(MsgConfigKeys.RANCH_UI_EGG_AVAILABLE), sources));
			}

			ItemStack display = ItemStack.builder()
					.itemType(SpongeItemTypeUtil.getOrDefaultItemTypeFromID("pixelmon:ranch"))
					.add(Keys.DISPLAY_NAME, TextParser.parse(TextParser.read(MsgConfigKeys.RANCH_UI_PEN_ID), sources))
					.add(Keys.ITEM_LORE, lore)
					.build();

			SpongeIcon icon = new SpongeIcon(display);
			icon.addListener(clickable -> {
				PenUI.builder().ranch(ranch).pen(pen).viewer(viewer).build().open();
			});
			return icon;
		} else {
			List<Text> lore = Lists.newArrayList();
			lore.addAll(TextParser.parse(TextParser.read(MsgConfigKeys.RANCH_UI_PEN_LOCKED), sources));
			lore.add(TextParser.parse(TextParser.read(MsgConfigKeys.RANCH_PRICE_TAG), sources));

			ItemStack display = ItemStack.builder()
					.itemType(SpongeItemTypeUtil.getOrDefaultItemTypeFromID("pixelmon:ranch"))
					.add(Keys.DISPLAY_NAME, TextParser.parse(TextParser.read(MsgConfigKeys.RANCH_UI_PEN_ID), sources))
					.add(Keys.ITEM_LORE, lore)
					.build();
			SpongeIcon icon = new SpongeIcon(display);
			icon.addListener(clickable -> {
				new ConfirmationUI(this.viewer, ranch, pen, index).open();
			});
			return icon;
		}
	}

	private Text forSlot(SpongePen pen, int slot) {
		List<Supplier<Object>> sources = Lists.newArrayList();
		sources.add(() -> slot);

		if (pen.getAtPosition(slot).isPresent()) {
			sources.add(() -> pen.getAtPosition(slot).orElse(null));
			sources.add(() -> ((DaycarePokemonWrapper) pen.getAtPosition(slot).get()).getDelegate());

			return TextParser.parse(TextParser.read(MsgConfigKeys.RANCH_UI_PEN_INFO), sources);
		} else {
			return TextParser.parse(TextParser.read(MsgConfigKeys.RANCH_UI_PEN_EMPTY), sources);
		}
	}
}
