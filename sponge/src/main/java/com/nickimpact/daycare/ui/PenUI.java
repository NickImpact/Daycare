package com.nickimpact.daycare.ui;

import com.google.common.collect.Maps;
import com.nickimpact.daycare.SpongeDaycarePlugin;
import com.nickimpact.daycare.configuration.MsgConfigKeys;
import com.nickimpact.daycare.implementation.SpongePen;
import com.nickimpact.daycare.implementation.SpongeRanch;
import com.nickimpact.impactor.sponge.ui.SpongeUI;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class PenUI {

	private SpongeUI display;
	private Player viewer;
	private SpongeRanch ranch;
	private SpongePen pen;

	public PenUI(Player viewer, SpongeRanch ranch, SpongePen pen) {
		this.viewer = viewer;
		this.pen = pen;
		this.ranch = ranch;
		this.display = this.createUI();
	}

	public void open() {
		this.display.open(this.viewer);
	}

	private SpongeUI createUI() {
		Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
		tokens.put("pen_id", src -> Optional.of(Text.of(pen.getID())));

		return SpongeUI.builder()
				.title(SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsg(this.viewer, MsgConfigKeys.PEN_UI_TITLE, tokens, null))
				.dimension(InventoryDimension.of(9, 5))
				.build();
	}
}
