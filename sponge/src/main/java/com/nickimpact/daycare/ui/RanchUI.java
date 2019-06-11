package com.nickimpact.daycare.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.daycare.SpongeDaycarePlugin;
import com.nickimpact.daycare.api.pens.PenUnlockModule;
import com.nickimpact.daycare.configuration.MsgConfigKeys;
import com.nickimpact.daycare.implementation.SpongePen;
import com.nickimpact.daycare.implementation.SpongeRanch;
import com.nickimpact.daycare.text.TextParsingUtils;
import com.nickimpact.daycare.utils.SpongeItemTypeUtil;
import com.nickimpact.impactor.sponge.ui.SpongeIcon;
import com.nickimpact.impactor.sponge.ui.SpongeLayout;
import com.nickimpact.impactor.sponge.ui.SpongePage;
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
		TextParsingUtils parser = SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils();

		SpongePage.SpongePageBuilder spb = SpongePage.builder();
		spb.title(Text.of(parser.parse(SpongeDaycarePlugin.getSpongeInstance().getMsgConfig().get(MsgConfigKeys.RANCH_UI_TITLE), this.viewer, null, null)));
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
		TextParsingUtils parser = SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils();

		Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
		tokens.put("pen_id", src -> Optional.of(Text.of(index)));
		tokens.put("daycare_price", src -> Optional.of(Text.of(SpongeDaycarePlugin.getSpongeInstance().getService().getActiveModule().getRequirement(index - 1))));

		Map<String, Object> variables = Maps.newHashMap();
		variables.put("pen", pen);

		if(pen.isUnlocked()) {
			List<Text> lore = Lists.newArrayList();
			lore.add(this.forSlot(pen, parser, 1));
			lore.add(this.forSlot(pen, parser, 2));

			if(pen.getEgg().isPresent()) {
				lore.add(Text.EMPTY);
				lore.add(parser.fetchAndParseMsg(viewer, MsgConfigKeys.RANCH_UI_EGG_AVAILABLE, tokens, variables));
			}

			ItemStack display = ItemStack.builder()
					.itemType(SpongeItemTypeUtil.getOrDefaultItemTypeFromID("pixelmon:ranch"))
					.add(Keys.DISPLAY_NAME, parser.fetchAndParseMsg(viewer, MsgConfigKeys.RANCH_UI_PEN_ID, tokens, variables))
					.add(Keys.ITEM_LORE, lore)
					.build();

			SpongeIcon icon = new SpongeIcon(display);
			icon.addListener(clickable -> {
				new PenUI(this.viewer, pen).open();
			});
			return icon;
		} else {
			PenUnlockModule module = SpongeDaycarePlugin.getSpongeInstance().getService().getActiveModule();

			List<Text> lore = Lists.newArrayList();
			lore.addAll(parser.fetchAndParseMsgs(this.viewer, MsgConfigKeys.RANCH_UI_PEN_LOCKED, tokens, variables));
			lore.add(Text.of(parser.fetchAndParseMsg(this.viewer, MsgConfigKeys.RANCH_PRICE_TAG, tokens, variables)));

			ItemStack display = ItemStack.builder()
					.itemType(SpongeItemTypeUtil.getOrDefaultItemTypeFromID("pixelmon:ranch"))
					.add(Keys.DISPLAY_NAME, parser.fetchAndParseMsg(viewer, MsgConfigKeys.RANCH_UI_PEN_ID, tokens, variables))
					.add(Keys.ITEM_LORE, lore)
					.build();
			SpongeIcon icon = new SpongeIcon(display);
			icon.addListener(clickable -> {
				new ConfirmationUI(this.viewer, ranch, pen, index).open();
			});
			return icon;
		}
	}

	private Text forSlot(SpongePen pen, TextParsingUtils parser, int slot) {
		Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
		tokens.put("slot_id", src -> Optional.of(Text.of(slot)));

		if (pen.getAtPosition(slot).isPresent()) {
			Map<String, Object> variables = Maps.newHashMap();
			variables.put("wrapper", pen.getAtPosition(slot).orElse(null));
			variables.put("pokemon", pen.getAtPosition(slot).get().getDelegate());

			return parser.fetchAndParseMsg(this.viewer, MsgConfigKeys.RANCH_UI_PEN_INFO, tokens, variables);
		} else {
			return parser.fetchAndParseMsg(this.viewer, MsgConfigKeys.RANCH_UI_PEN_EMPTY, tokens, null);
		}
	}
}
