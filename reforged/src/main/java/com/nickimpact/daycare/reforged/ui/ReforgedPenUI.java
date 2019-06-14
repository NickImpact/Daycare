package com.nickimpact.daycare.reforged.ui;

import com.google.common.collect.Maps;
import com.nickimpact.daycare.SpongeDaycarePlugin;
import com.nickimpact.daycare.api.breeding.BreedStage;
import com.nickimpact.daycare.configuration.MsgConfigKeys;
import com.nickimpact.daycare.implementation.SpongePen;
import com.nickimpact.daycare.implementation.SpongeRanch;
import com.nickimpact.daycare.reforged.implementation.ReforgedDaycarePokemonWrapper;
import com.nickimpact.daycare.reforged.implementation.ReforgedPen;
import com.nickimpact.daycare.text.TextParsingUtils;
import com.nickimpact.daycare.ui.PenUI;
import com.nickimpact.daycare.ui.common.CommonUIComponents;
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

public class ReforgedPenUI implements PenUI {

	private SpongeUI display;
	private Player viewer;
	private SpongeRanch ranch;
	private ReforgedPen pen;

	public ReforgedPenUI(Player viewer, SpongeRanch ranch, ReforgedPen pen) {
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

		ItemStack notReached = ItemStack.builder()
				.itemType(ItemTypes.CONCRETE)
				.add(Keys.DYE_COLOR, DyeColors.RED)
				.add(Keys.DISPLAY_NAME, SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsg(this.viewer, MsgConfigKeys.BREED_STAGES_NOT_REACHED, null, null))
				.build();
		SpongeIcon nr = new SpongeIcon(notReached);
		BreedStage stage = this.pen.getStage();
		if(stage != null) {
			layout.slots(nr, stage.getSlots());

			int index = 28;
			for(BreedStage s : BreedStage.values()) {
				if(s.ordinal() >= stage.ordinal()) continue;

				ItemStack st = ItemStack.builder()
						.itemType(ItemTypes.CONCRETE)
						.add(Keys.DYE_COLOR, DyeColors.LIME)
						.add(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, s.name()))
						.add(Keys.ITEM_LORE, SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsgs(this.viewer, this.getForStage(s), null, null))
						.build();
				layout.slot(new SpongeIcon(st), index++);
			}

			ItemStack current = ItemStack.builder()
					.itemType(ItemTypes.CONCRETE)
					.add(Keys.DYE_COLOR, DyeColors.YELLOW)
					.add(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, stage.name()))
					.add(Keys.ITEM_LORE, SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsgs(this.viewer, this.getForStage(stage), null, null))
					.build();
			layout.slot(new SpongeIcon(current), index);
		} else {
			if(pen.getEgg().isPresent()) {
				int index = 28;
				for (BreedStage s : BreedStage.values()) {
					if (s == BreedStage.BRED) continue;

					ItemStack st = ItemStack.builder()
							.itemType(ItemTypes.CONCRETE)
							.add(Keys.DYE_COLOR, DyeColors.LIME)
							.add(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, s.name()))
							.add(Keys.ITEM_LORE, SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsgs(this.viewer, this.getForStage(s), null, null))
							.build();
					layout.slot(new SpongeIcon(st), index++);
				}
			} else {
				layout.slots(nr, 28, 29, 30, 31, 32);
			}
		}

		return layout.build();
	}

	private SpongeIcon pokemonIconForSlot(int slot) {
		if(this.pen.getAtPosition(slot).isPresent()) {
			TextParsingUtils parser = SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils();
			Map<String, Object> variables = Maps.newHashMap();
			variables.put("poke", this.pen.getAtPosition(slot).get().getDelegate());
			variables.put("wrapper", this.pen.getAtPosition(slot).get());
			ItemStack display = CommonUIComponents.pokemonDisplay(this.pen.getAtPosition(slot).get().getDelegate());
			display.offer(Keys.DISPLAY_NAME, parser.fetchAndParseMsg(this.viewer, MsgConfigKeys.POKEMON_TITLE_PEN, null, variables));
			display.offer(Keys.ITEM_LORE, parser.fetchAndParseMsgs(this.viewer, MsgConfigKeys.POKEMON_LORE_PEN, null, variables));
			SpongeIcon icon = new SpongeIcon(display);
			icon.addListener(clickable -> {
				new RetrievalUI(this.viewer, this.pen.getAtPosition(slot).get(), this.ranch, this.pen, slot).open();
			});
			return icon;
		} else {
			ItemStack e = ItemStack.builder()
					.itemType(ItemTypes.BARRIER)
					.add(Keys.DISPLAY_NAME, SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsg(this.viewer, MsgConfigKeys.PEN_EMPTY_SLOT, null, null))
					.build();
			SpongeIcon empty = new SpongeIcon(e);
			empty.addListener(clickable -> {
				new PartyUI(this.viewer, this.ranch, this.pen, slot).open();
			});

			return empty;
		}
	}

	private SpongeIcon eggIcon(ReforgedDaycarePokemonWrapper egg) {
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

	public static class ReforgedPenUIBuilder implements PenUIBuilder {

		private SpongeRanch ranch;
		private ReforgedPen pen;
		private Player viewer;

		@Override
		public PenUIBuilder ranch(SpongeRanch ranch) {
			this.ranch = ranch;
			return this;
		}

		@Override
		public PenUIBuilder pen(SpongePen pen) {
			this.pen = (ReforgedPen) pen;
			return this;
		}

		@Override
		public PenUIBuilder viewer(Player viewer) {
			this.viewer = viewer;
			return this;
		}

		@Override
		public PenUI build() {
			return new ReforgedPenUI(viewer, ranch, pen);
		}
	}
}
