package com.nickimpact.daycare.ui;

import com.google.common.collect.Maps;
import com.nickimpact.daycare.SpongeDaycarePlugin;
import com.nickimpact.daycare.api.breeding.BreedStage;
import com.nickimpact.daycare.configuration.MsgConfigKeys;
import com.nickimpact.daycare.implementation.SpongeDaycarePokemonWrapper;
import com.nickimpact.daycare.implementation.SpongePen;
import com.nickimpact.daycare.implementation.SpongeRanch;
import com.nickimpact.impactor.api.configuration.ConfigKey;
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
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
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
		this.display.define(this.layout());
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

	private SpongeLayout layout() {
		SpongeIcon gray = new SpongeIcon(ItemStack.builder()
				.itemType(ItemTypes.STAINED_GLASS_PANE)
				.add(Keys.DISPLAY_NAME, Text.EMPTY)
				.add(Keys.DYE_COLOR, DyeColors.GRAY)
				.build());

		SpongeLayout.SpongeLayoutBuilder layout = SpongeLayout.builder()
				.dimension(9, 5)
				.border()
				.row(gray, 2)
				.slots(SpongeIcon.BORDER, 18, 26);

		layout.slot(pokemonIconForSlot(1), 11);
		layout.slot(pokemonIconForSlot(2), 15);

		if(this.pen.getEgg().isPresent()) {
			layout.slot(this.eggIcon(this.pen.getEgg().get()), 22);
		}

		layout.slot(gray, 33);

		BreedStage stage = this.pen.getStage();
		stage = BreedStage.OUT_ON_THE_TOWN;             // TEMP
		if(stage != null) {
			ItemStack notReached = ItemStack.builder()
					.itemType(ItemTypes.STAINED_GLASS_PANE)
					.add(Keys.DYE_COLOR, DyeColors.RED)
					.add(Keys.DISPLAY_NAME, SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsg(this.viewer, MsgConfigKeys.BREED_STAGES_NOT_REACHED, null, null))
					.build();
			SpongeIcon nr = new SpongeIcon(notReached);
			layout.slots(nr, stage.getSlots());

			int index = 28;
			for(BreedStage s : BreedStage.values()) {
				if(s.ordinal() > stage.ordinal()) continue;

				ItemStack st = ItemStack.builder()
						.itemType(ItemTypes.STAINED_GLASS_PANE)
						.add(Keys.DYE_COLOR, DyeColors.LIME)
						.add(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, s.name()))
						.add(Keys.ITEM_LORE, SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsgs(this.viewer, this.getForStage(s), null, null))
						.build();
				layout.slot(new SpongeIcon(st), index++);
			}
		}

		return layout.build();
	}

	private SpongeIcon pokemonIconForSlot(int slot) {
		if(this.pen.getAtPosition(slot).isPresent()) {

			return SpongeIcon.BORDER;
		} else {
			ItemStack e = ItemStack.builder()
					.itemType(ItemTypes.BARRIER)
					.add(Keys.DISPLAY_NAME, SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsg(this.viewer, MsgConfigKeys.PEN_EMPTY_SLOT, null, null))
					.build();
			SpongeIcon empty = new SpongeIcon(e);
			empty.addListener(clickable -> {

			});

			return empty;
		}
	}

	private SpongeIcon eggIcon(SpongeDaycarePokemonWrapper egg) {
		return SpongeIcon.BORDER;
	}

	private ConfigKey<List<String>> getForStage(BreedStage stage) {
		switch (stage) {
			case SETTLING:
			default:
				return MsgConfigKeys.BREED_STAGES_SETTLING;
			case SOCIALIZING:
				return MsgConfigKeys.BREED_STAGES_SOCIALIZING;
			case IN_LOVE:
				return MsgConfigKeys.BREED_STAGES_IN_LOVE;
			case OUT_ON_THE_TOWN:
				return MsgConfigKeys.BREED_STAGES_OUT_ON_THE_TOWN;
			case ONE_NIGHT_STAND:
				return MsgConfigKeys.BREED_STAGES_ONE_NIGHT_STAND;
		}
	}
}
