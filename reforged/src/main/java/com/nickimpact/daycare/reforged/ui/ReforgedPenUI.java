package com.nickimpact.daycare.reforged.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;
import com.nickimpact.daycare.api.breeding.BreedStage;
import com.nickimpact.daycare.api.events.DaycareEvent;
import com.nickimpact.daycare.reforged.implementation.ReforgedDaycarePokemonWrapper;
import com.nickimpact.daycare.reforged.implementation.ReforgedPen;
import com.nickimpact.daycare.sponge.SpongeDaycarePlugin;
import com.nickimpact.daycare.sponge.configuration.MsgConfigKeys;
import com.nickimpact.daycare.sponge.implementation.SpongePen;
import com.nickimpact.daycare.sponge.implementation.SpongeRanch;
import com.nickimpact.daycare.sponge.observing.PenObservers;
import com.nickimpact.daycare.sponge.ui.PenUI;
import com.nickimpact.daycare.sponge.ui.SettingsUI;
import com.nickimpact.daycare.sponge.ui.common.CommonUIComponents;
import com.nickimpact.daycare.sponge.utils.SpongeItemTypeUtil;
import com.nickimpact.daycare.sponge.utils.TextParser;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.impactor.sponge.ui.SpongeIcon;
import net.impactdev.impactor.sponge.ui.SpongeLayout;
import net.impactdev.impactor.sponge.ui.SpongeUI;
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
import java.util.function.Supplier;

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
		List<Supplier<Object>> sources = Lists.newArrayList();
		sources.add(() -> pen.getID());

		return SpongeUI.builder()
				.title(TextParser.parse(TextParser.read(MsgConfigKeys.PEN_UI_TITLE), sources))
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
				.add(Keys.DISPLAY_NAME, TextParser.parse(TextParser.read(MsgConfigKeys.RANCH_UI_SETTINGS)))
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
			List<Supplier<Object>> sources = Lists.newArrayList();
			sources.add(() -> this.pen.getAtPosition(slot).get().getDelegate());
			sources.add(() -> this.pen.getAtPosition(slot).get());

			ItemStack display = CommonUIComponents.pokemonDisplay(this.pen.getAtPosition(slot).get().getDelegate());
			display.offer(Keys.DISPLAY_NAME, TextParser.parse(TextParser.read(MsgConfigKeys.POKEMON_TITLE_PEN), sources));
			display.offer(Keys.ITEM_LORE, TextParser.parse(TextParser.read(MsgConfigKeys.POKEMON_LORE_PEN), sources));
			SpongeIcon icon = new SpongeIcon(display);
			icon.addListener(clickable -> {
				new RetrievalUI(this.viewer, this.pen.getAtPosition(slot).get(), this.ranch, this.pen, slot).open();
			});
			return icon;
		} else {
			ItemStack e = ItemStack.builder()
					.itemType(ItemTypes.BARRIER)
					.add(Keys.DISPLAY_NAME, TextParser.parse(TextParser.read(MsgConfigKeys.PEN_EMPTY_SLOT)))
					.build();
			SpongeIcon empty = new SpongeIcon(e);
			empty.addListener(clickable -> {
				new PartyUI(this.viewer, this.ranch, this.pen, slot).open();
			});

			return empty;
		}
	}

	private SpongeIcon eggIcon(ReforgedDaycarePokemonWrapper egg) {
		List<Supplier<Object>> sources = Lists.newArrayList();
		sources.add(egg::getDelegate);

		ItemStack e = CommonUIComponents.pokemonDisplay(egg.getDelegate());
		e.offer(Keys.DISPLAY_NAME, TextParser.parse(TextParser.read(MsgConfigKeys.POKEMON_EGG_TITLE_PEN), sources));
		e.offer(Keys.ITEM_LORE, TextParser.parse(TextParser.read(MsgConfigKeys.POKEMON_LORE_SELECT), sources));
		SpongeIcon icon = new SpongeIcon(e);
		icon.addListener(clickable -> {
			Impactor.getInstance().getEventBus().post(
					DaycareEvent.CollectEgg.class,
					new TypeToken<ReforgedDaycarePokemonWrapper>(){},
					this.viewer.getUniqueId(),
					pen,
					pen.getEgg().get()
			);
			Pokemon eg = pen.claimEgg().getDelegate();
			PlayerPartyStorage party = Pixelmon.storageManager.getParty(this.viewer.getUniqueId());
			party.add(eg);

			this.viewer.sendMessages(TextParser.parse(TextParser.read(MsgConfigKeys.PEN_EGG_CLAIM), sources));
			this.update();
		});

		return icon;
	}

	private ConfigKey<String> getForStageName(BreedStage stage) {
		switch (stage) {
			case SETTLING:
			default:
				return MsgConfigKeys.SETTLING;
			case SOCIALIZING:
				return MsgConfigKeys.SOCIALIZING;
			case IN_LOVE:
				return MsgConfigKeys.IN_LOVE;
			case OUT_ON_THE_TOWN:
				return MsgConfigKeys.NIGHT_OUT;
			case ONE_NIGHT_STAND:
				return MsgConfigKeys.NIGHT_STAND;
		}
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
				.add(Keys.DISPLAY_NAME, TextParser.parse(TextParser.read(MsgConfigKeys.BREED_STAGES_NOT_REACHED)))
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
						.add(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, TextParser.parse(TextParser.read(this.getForStageName(s)))))
						.add(Keys.ITEM_LORE, TextParser.parse(TextParser.read(this.getForStage(s))))
						.build();
				icons.add(new SpongeIcon(st));
				++index;
			}

			ItemStack current = ItemStack.builder()
					.itemType(ItemTypes.CONCRETE)
					.add(Keys.DYE_COLOR, DyeColors.YELLOW)
					.add(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, TextParser.parse(TextParser.read(this.getForStageName(stage)))))
					.add(Keys.ITEM_LORE, TextParser.parse(TextParser.read(this.getForStage(stage))))
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
							.add(Keys.ITEM_LORE, TextParser.parse(TextParser.read(this.getForStage(s))))
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
		public PenUIBuilder from(PenUI penUI) {
			return null;
		}

		@Override
		public PenUI build() {
			return new ReforgedPenUI(viewer, ranch, pen);
		}
	}
}
