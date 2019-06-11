package com.nickimpact.daycare.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.daycare.SpongeDaycarePlugin;
import com.nickimpact.daycare.api.pens.PenUnlockModule;
import com.nickimpact.daycare.configuration.MsgConfigKeys;
import com.nickimpact.daycare.implementation.SpongePen;
import com.nickimpact.daycare.implementation.SpongeRanch;
import com.nickimpact.daycare.utils.SpongeItemTypeUtil;
import com.nickimpact.impactor.sponge.ui.SpongeIcon;
import com.nickimpact.impactor.sponge.ui.SpongeLayout;
import com.nickimpact.impactor.sponge.ui.SpongeUI;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class ConfirmationUI {

	private SpongeUI display;
	private Player viewer;
	private SpongeRanch ranch;
	private SpongePen pen;
	private int index;

	public ConfirmationUI(Player viewer, SpongeRanch ranch, SpongePen pen, int index) {
		this.viewer = viewer;
		this.ranch = ranch;
		this.pen = pen;
		this.index = index;
		this.display = this.createUI();
	}

	public void open() {
		this.display.open(this.viewer);
	}

	private SpongeUI createUI() {
		SpongeUI.SpongeUIBuilder sb = SpongeUI.builder();
		sb.title(SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsg(this.viewer, MsgConfigKeys.CONFIRM_UI_TITLE, null, null));
		sb.dimension(InventoryDimension.of(9, 6));
		return sb.build().define(this.layout());
	}

	private SpongeLayout layout() {
		SpongeLayout.SpongeLayoutBuilder slb = SpongeLayout.builder();
		slb.border();
		slb.slot(this.drawPen(), 13);

		PenUnlockModule module = SpongeDaycarePlugin.getSpongeInstance().getService().getActiveModule();
		Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
		tokens.put("daycare_price", src -> Optional.of(Text.of(module.getRequirement(this.index - 1))));
		tokens.put("pen", src -> Optional.of(Text.of(this.index)));

		ItemStack confirm = ItemStack.builder()
				.itemType(ItemTypes.STAINED_GLASS_PANE)
				.add(Keys.DYE_COLOR, DyeColors.LIME)
				.add(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize(SpongeDaycarePlugin.getSpongeInstance().getMsgConfig().get(MsgConfigKeys.CONFIRM)))
				.add(Keys.ITEM_LORE, SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsgs(this.viewer, MsgConfigKeys.CONFIRM_PEN_BUTTON, tokens, null))
				.build();
		SpongeIcon cIcon = new SpongeIcon(confirm);
		cIcon.addListener(clickable -> {
			this.display.close(clickable.getPlayer());
			if(ranch.unlock(this.index - 1)) {
				SpongeDaycarePlugin.getSpongeInstance().getService().getStorage().updateRanch(this.ranch);
				this.viewer.sendMessage(SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsg(this.viewer, MsgConfigKeys.UNLOCK_PEN, tokens, null));
			}
		});
		slb.slots(cIcon, 29, 30, 38, 39);

		ItemStack cancel = ItemStack.builder()
				.itemType(ItemTypes.STAINED_GLASS_PANE)
				.add(Keys.DYE_COLOR, DyeColors.RED)
				.add(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize(SpongeDaycarePlugin.getSpongeInstance().getMsgConfig().get(MsgConfigKeys.CANCEL)))
				.build();
		SpongeIcon c = new SpongeIcon(cancel);
		c.addListener(clickable -> {
			new RanchUI(this.viewer).open();
		});
		slb.slots(c, 32, 33, 41, 42);

		return slb.build();
	}

	private SpongeIcon drawPen() {
		Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
		tokens.put("pen_id", src -> Optional.of(Text.of(index)));
		tokens.put("daycare_price", src -> Optional.of(Text.of(SpongeDaycarePlugin.getSpongeInstance().getService().getActiveModule().getRequirement(index - 1))));

		List<String> lore = Lists.newArrayList();
		lore.addAll(SpongeDaycarePlugin.getSpongeInstance().getMsgConfig().get(MsgConfigKeys.CONFIRM_PEN_DETAILS));

		return new SpongeIcon(ItemStack.builder()
				.itemType(SpongeItemTypeUtil.getOrDefaultItemTypeFromID("pixelmon:ranch"))
				.add(Keys.DISPLAY_NAME, Text.of(SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsg(
						this.viewer, MsgConfigKeys.RANCH_UI_PEN_ID, tokens, null
				)))
				.add(Keys.ITEM_LORE, SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().parse(lore, this.viewer, tokens, null))
				.build()
		);
	}
}
