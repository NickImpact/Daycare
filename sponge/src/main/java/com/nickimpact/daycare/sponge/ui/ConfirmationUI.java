package com.nickimpact.daycare.sponge.ui;

import com.google.common.collect.Lists;
import com.nickimpact.daycare.sponge.SpongeDaycarePlugin;
import com.nickimpact.daycare.api.pens.PenUnlockModule;
import com.nickimpact.daycare.sponge.configuration.MsgConfigKeys;
import com.nickimpact.daycare.sponge.implementation.SpongePen;
import com.nickimpact.daycare.sponge.implementation.SpongeRanch;
import com.nickimpact.daycare.sponge.ui.common.CommonUIComponents;
import com.nickimpact.daycare.sponge.utils.SpongeItemTypeUtil;
import com.nickimpact.daycare.sponge.utils.TextParser;
import net.impactdev.impactor.sponge.ui.SpongeIcon;
import net.impactdev.impactor.sponge.ui.SpongeLayout;
import net.impactdev.impactor.sponge.ui.SpongeUI;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;

import java.util.List;
import java.util.function.Supplier;

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
		sb.title(TextParser.parse(TextParser.read(MsgConfigKeys.CONFIRM_UI_TITLE)));
		sb.dimension(InventoryDimension.of(9, 6));
		return sb.build().define(this.layout());
	}

	private SpongeLayout layout() {
		PenUnlockModule module = SpongeDaycarePlugin.getSpongeInstance().getService().getActiveModule();
		List<Supplier<Object>> sources = Lists.newArrayList();
		sources.add(() -> index);
		sources.add(() -> module.getRequirement(index - 1));

		return CommonUIComponents.confirmBase(
				this.drawPen(),
				new CommonUIComponents.CommonConfirmComponent(
						TextParser.parse(TextParser.read(MsgConfigKeys.CONFIRM_PEN_BUTTON), sources),
						(player, event) -> {
							this.display.close(player);
							if(ranch.unlock(this.index - 1)) {
								SpongeDaycarePlugin.getSpongeInstance().getService().getStorage().updateRanch(this.ranch);
								this.viewer.sendMessage(TextParser.parse(TextParser.read(MsgConfigKeys.UNLOCK_PEN), sources));
							} else {
								player.sendMessages(TextParser.parse(TextParser.read(MsgConfigKeys.RANCH_UI_PEN_INSUFFICIENT_FUNDS), sources));
							}
						}
				),
				(player, event) -> new RanchUI(player).open()
		);
	}

	private SpongeIcon drawPen() {
		List<Supplier<Object>> sources = Lists.newArrayList();
		sources.add(() -> index);
		sources.add(() -> SpongeDaycarePlugin.getSpongeInstance().getService().getActiveModule().getRequirement(index - 1));

		List<String> lore = Lists.newArrayList();
		lore.addAll(SpongeDaycarePlugin.getSpongeInstance().getMsgConfig().get(MsgConfigKeys.CONFIRM_PEN_DETAILS));

		return new SpongeIcon(ItemStack.builder()
				.itemType(SpongeItemTypeUtil.getOrDefaultItemTypeFromID("pixelmon:ranch"))
				.add(Keys.DISPLAY_NAME, TextParser.parse(TextParser.read(MsgConfigKeys.RANCH_UI_PEN_ID), sources))
				.add(Keys.ITEM_LORE, TextParser.parse(lore, sources))
				.build()
		);
	}
}
