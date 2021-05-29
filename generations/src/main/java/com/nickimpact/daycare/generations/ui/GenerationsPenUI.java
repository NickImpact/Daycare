package com.nickimpact.daycare.generations.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.daycare.sponge.SpongeDaycarePlugin;
import com.nickimpact.daycare.api.breeding.BreedStage;
import com.nickimpact.daycare.sponge.configuration.MsgConfigKeys;
import com.nickimpact.daycare.generations.implementation.GenerationsDaycarePokemonWrapper;
import com.nickimpact.daycare.generations.implementation.GenerationsPen;
import com.nickimpact.daycare.sponge.implementation.SpongePen;
import com.nickimpact.daycare.sponge.implementation.SpongeRanch;
import com.nickimpact.daycare.sponge.observing.PenObservers;
import com.nickimpact.daycare.sponge.ui.PenUI;
import com.nickimpact.daycare.sponge.ui.SettingsUI;
import com.nickimpact.daycare.sponge.utils.SpongeItemTypeUtil;
import com.nickimpact.impactor.api.configuration.ConfigKey;
import com.nickimpact.impactor.sponge.ui.SpongeIcon;
import com.nickimpact.impactor.sponge.ui.SpongeLayout;
import com.nickimpact.impactor.sponge.ui.SpongeUI;
import com.pixelmongenerations.common.entity.pixelmon.EntityPixelmon;
import com.pixelmongenerations.core.storage.PixelmonStorage;
import com.pixelmongenerations.core.storage.PlayerStorage;
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

public class GenerationsPenUI implements PenUI {
	private SpongeUI display;
	private Player viewer;
	private SpongeRanch ranch;
	private GenerationsPen pen;

	public GenerationsPenUI(Player viewer, SpongeRanch ranch, GenerationsPen pen) {
		this.viewer = viewer;
		this.pen = pen;
		this.ranch = ranch;
		this.display = this.createUI();
		this.display.attachCloseListener(e -> {
			PenObservers.removeObserver(viewer.getUniqueId());
		});
		this.display.define(this.layout());

		PenObservers.addObserver(viewer.getUniqueId(), this);
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
		layout.slot(pokemonIconForSlot(2), 13);
		layout.slot(SpongeIcon.BORDER, 15);

		ItemStack settings = ItemStack.builder()
				.itemType(SpongeItemTypeUtil.getOrDefaultItemTypeFromID("pixelmon:diamond_hammer"))
				.add(Keys.DISPLAY_NAME, SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsg(this.viewer, MsgConfigKeys.RANCH_UI_SETTINGS, null, null))
				.build();
		SpongeIcon si = new SpongeIcon(settings);
		si.addListener(clickable -> {
			new SettingsUI(this.viewer, this.ranch, this.pen).open();
		});
		layout.slot(si, 16);

		if(this.pen.getEgg().isPresent()) {
			layout.slot(this.eggIcon(this.pen.getEgg().get()), 34);
		}

		layout.slot(gray, 33);

		List<SpongeIcon> statuses = this.getStageIcons();
		for(int i = 28; i < 33; i++) {
			layout.slot(statuses.get(i - 28), i);
		}

		return layout.build();
	}

	private SpongeIcon pokemonIconForSlot(int slot) {
		if(this.pen.getAtPosition(slot).isPresent()) {
			TextParsingUtils parser = SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils();
			Map<String, Object> variables = Maps.newHashMap();
			variables.put("poke", this.pen.getAtPosition(slot).get().getDelegate());
			variables.put("wrapper", this.pen.getAtPosition(slot).get());
			ItemStack display = GenerationsIcons.pokemonDisplay(this.pen.getAtPosition(slot).get().getDelegate());
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

	private SpongeIcon eggIcon(GenerationsDaycarePokemonWrapper egg) {
		Map<String, Object> variables = Maps.newHashMap();
		variables.put("poke", egg.getDelegate());

		ItemStack e = GenerationsIcons.pokemonDisplay(egg.getDelegate());
		e.offer(Keys.DISPLAY_NAME, SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsg(this.viewer, MsgConfigKeys.POKEMON_EGG_TITLE_PEN, null, variables));
		e.offer(Keys.ITEM_LORE, SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsgs(this.viewer, MsgConfigKeys.POKEMON_LORE_SELECT, null, variables));
		SpongeIcon icon = new SpongeIcon(e);
		icon.addListener(clickable -> {
			EntityPixelmon eg = pen.claimEgg().getDelegate();
			PlayerStorage party = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID(this.viewer.getUniqueId()).get();
			party.addToParty(eg);

			this.viewer.sendMessages(SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsgs(this.viewer, MsgConfigKeys.PEN_EGG_CLAIM, null, variables));
			this.update();
		});

		return icon;
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

	private List<SpongeIcon> getStageIcons() {
		List<SpongeIcon> icons = Lists.newArrayList();

		ItemStack notReached = ItemStack.builder()
				.itemType(ItemTypes.CONCRETE)
				.add(Keys.DYE_COLOR, DyeColors.RED)
				.add(Keys.DISPLAY_NAME, SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsg(this.viewer, MsgConfigKeys.BREED_STAGES_NOT_REACHED, null, null))
				.build();
		SpongeIcon nr = new SpongeIcon(notReached);
		BreedStage stage = this.pen.getStage();
		if(stage != null) {
			int index = 28;
			for(BreedStage s : BreedStage.values()) {
				if(s.ordinal() >= stage.ordinal()) continue;

				ItemStack st = ItemStack.builder()
						.itemType(ItemTypes.CONCRETE)
						.add(Keys.DYE_COLOR, DyeColors.LIME)
						.add(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, s.name()))
						.add(Keys.ITEM_LORE, SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsgs(this.viewer, this.getForStage(s), null, null))
						.build();
				icons.add(new SpongeIcon(st));
				++index;
			}

			ItemStack current = ItemStack.builder()
					.itemType(ItemTypes.CONCRETE)
					.add(Keys.DYE_COLOR, DyeColors.YELLOW)
					.add(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, stage.name()))
					.add(Keys.ITEM_LORE, SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsgs(this.viewer, this.getForStage(stage), null, null))
					.build();
			icons.add(new SpongeIcon(current));
			index++;

			for(; index < 33; index++) {
				icons.add(nr);
			}
		} else {
			if(pen.getEgg().isPresent()) {
				for (BreedStage s : BreedStage.values()) {
					if (s == BreedStage.BRED) continue;

					ItemStack st = ItemStack.builder()
							.itemType(ItemTypes.CONCRETE)
							.add(Keys.DYE_COLOR, DyeColors.LIME)
							.add(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, s.name()))
							.add(Keys.ITEM_LORE, SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsgs(this.viewer, this.getForStage(s), null, null))
							.build();
					icons.add(new SpongeIcon(st));
				}
			} else {
				for(int i = 0; i < 5; i++) {
					icons.add(nr);
				}
			}
		}

		return icons;
	}

	public void update() {
		if(this.pen.getEgg().isPresent()) {
			this.display.setSlot(34, this.eggIcon(this.pen.getEgg().get()));
		} else {
			this.display.setSlot(34, new SpongeIcon(ItemStack.builder().itemType(ItemTypes.AIR).build()));
		}

		List<SpongeIcon> statuses = this.getStageIcons();
		for(int i = 28; i < 33; i++) {
			this.display.setSlot(i, statuses.get(i - 28));
		}
	}

	public static class GenerationsPenUIBuilder implements PenUIBuilder {

		private SpongeRanch ranch;
		private GenerationsPen pen;
		private Player viewer;

		@Override
		public PenUIBuilder ranch(SpongeRanch ranch) {
			this.ranch = ranch;
			return this;
		}

		@Override
		public PenUIBuilder pen(SpongePen pen) {
			this.pen = (GenerationsPen) pen;
			return this;
		}

		@Override
		public PenUIBuilder viewer(Player viewer) {
			this.viewer = viewer;
			return this;
		}

		@Override
		public PenUI build() {
			return new GenerationsPenUI(viewer, ranch, pen);
		}
	}
}
