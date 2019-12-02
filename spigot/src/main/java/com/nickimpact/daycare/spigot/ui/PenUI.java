package com.nickimpact.daycare.spigot.ui;

import com.google.common.collect.Lists;
import com.nickimpact.daycare.api.breeding.BreedStage;
import com.nickimpact.daycare.spigot.implementation.SpigotDaycarePokemonWrapper;
import com.nickimpact.daycare.spigot.implementation.SpigotPen;
import com.nickimpact.daycare.spigot.implementation.SpigotRanch;
import com.nickimpact.daycare.spigot.observers.PenObservers;
import com.nickimpact.daycare.spigot.utils.ItemStackUtils;
import com.nickimpact.daycare.spigot.utils.LoreForging;
import com.nickimpact.daycare.spigot.utils.MessageUtils;
import com.nickimpact.impactor.api.building.Builder;
import com.nickimpact.impactor.spigot.ui.SpigotIcon;
import com.nickimpact.impactor.spigot.ui.SpigotLayout;
import com.nickimpact.impactor.spigot.ui.SpigotUI;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class PenUI {

	private Player viewer;
	private SpigotRanch ranch;
	private SpigotPen pen;

	private SpigotUI ui;

	public PenUI(PenUIBuilder builder) {
		this.viewer = builder.viewer;
		this.ranch = builder.ranch;
		this.pen = builder.pen;

		this.ui = this.createUI();
		this.ui.define(this.craftLayout());

		this.ui.attachCloseListener(e -> {
			PenObservers.removeObserver(viewer.getUniqueId());
		});

		PenObservers.addObserver(viewer.getUniqueId(), this);
	}

	public void open() {
		this.ui.open(viewer);
	}

	public void update() {
		if(this.pen.getAtPosition(1).isPresent()) {
			this.ui.setSlot(11, this.pokemonIconForSlot(1));
		}

		if(this.pen.getAtPosition(2).isPresent()) {
			this.ui.setSlot(13, this.pokemonIconForSlot(2));
		}

		if(this.pen.getEgg().isPresent()) {
			this.ui.setSlot(34, this.eggIcon(this.pen.getEgg().get()));
		} else {
			this.ui.clear(34);
		}

		List<SpigotIcon> stages = this.getStageIcons();
		for(int i = 28; i < 33; i++) {
			this.ui.setSlot(i, stages.get(i - 28));
		}
	}

	private SpigotUI createUI() {
		SpigotUI.SpigotUIBuilder sb = SpigotUI.builder();
		sb.title("&cDaycare &7\u00bb &3Pen " + pen.getID());
		sb.size(45);
		return sb.build();
	}

	private SpigotLayout craftLayout() {
		SpigotLayout.SpigotLayoutBuilder slb = SpigotLayout.builder();

		slb.dimension(9, 5).border().row(SpigotIcon.BORDER, 2).slots(SpigotIcon.BORDER, 18, 26);

		slb.slot(pokemonIconForSlot(1), 11);
		slb.slot(pokemonIconForSlot(2), 13);
		slb.slot(SpigotIcon.BORDER, 15);

		SpigotIcon settings = new SpigotIcon(ItemStackUtils.itemBuilder()
				.material(Material.matchMaterial("PIXELMON_DIAMOND_HAMMER"))
				.name("&eSettings")
				.build()
		);
		settings.addListener(clickable -> {
			new SettingsUI(viewer, ranch, pen).open();
		});
		slb.slot(settings, 16);

		if(this.pen.getEgg().isPresent()) {
			slb.slot(this.eggIcon(this.pen.getEgg().get()), 34);
		}

		slb.slot(SpigotIcon.BORDER, 33);

		List<SpigotIcon> statuses = this.getStageIcons();
		for(int i = 28; i < 33; i++) {
			slb.slot(statuses.get(i - 28), i);
		}

		return slb.build();
	}

	private SpigotIcon pokemonIconForSlot(int slot) {
		if(this.pen.getAtPosition(slot).isPresent()) {
			SpigotDaycarePokemonWrapper wrapper = this.pen.getAtPosition(slot).get();

			ItemStack display = CommonUIComponents.pokemonDisplay(this.pen.getAtPosition(slot).get().getDelegate());
			ItemMeta meta = display.getItemMeta();
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', String.format(
					"&3%s %s&7| &bLvl %d",
					wrapper.getDelegate().getSpecies().getPokemonName(),
					wrapper.getDelegate().isShiny() ? "&7(&6Shiny&7) " : "",
					wrapper.getDelegate().getLevel() + wrapper.getGainedLevels()
			)));

			LoreForging.craftLore(wrapper.getDelegate(), display, meta, wrapper.getGainedLevels());

			SpigotIcon icon = new SpigotIcon(display);
			icon.addListener(clickable -> {
				new RetrievalUI(this.viewer, this.pen.getAtPosition(slot).get(), this.ranch, this.pen, slot).open();
			});
			return icon;
		} else {
			ItemStack e = ItemStackUtils.itemBuilder()
					.material(Material.BARRIER)
					.name("&cEmpty &7(Click to add a Pokemon)")
					.build();
			SpigotIcon empty = new SpigotIcon(e);
			empty.addListener(clickable -> {
				new PartyUI(this.viewer, this.ranch, this.pen, slot).open();
			});

			return empty;
		}
	}

	private SpigotIcon eggIcon(SpigotDaycarePokemonWrapper egg) {
		ItemStack e = CommonUIComponents.pokemonDisplay(egg.getDelegate());
		ItemMeta meta = e.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', String.format(
				"&3%s Egg%s",
				egg.getDelegate().getSpecies().getPokemonName(),
				egg.getDelegate().isShiny() ? " &7(&6Shiny&7)" : ""
		)));

		LoreForging.craftLore(egg.getDelegate(), e, meta, -1);

		SpigotIcon icon = new SpigotIcon(e);
		icon.addListener(clickable -> {
			Pokemon eg = pen.claimEgg().getDelegate();
			PlayerPartyStorage party = Pixelmon.storageManager.getParty(this.viewer.getUniqueId());
			party.add(eg);

			this.viewer.sendMessage(MessageUtils.parse("&7You've collected an egg!", true, false));
			this.update();
		});

		return icon;
	}

	private String getForStage(BreedStage stage) {
		switch (stage) {
			case SETTLING:
			default:
				return "Settling";
			case SOCIALIZING:
				return "Socializing";
			case IN_LOVE:
				return "In Love";
			case OUT_ON_THE_TOWN:
				return "Out on the Town";
			case ONE_NIGHT_STAND:
				return "One Night Stand";
		}
	}

	private List<String> getForStageLore(BreedStage stage) {
		switch (stage) {
			case SETTLING:
			default:
				return Lists.newArrayList("&7Both pokemon are now settling in!");
			case SOCIALIZING:
				return Lists.newArrayList(
						"&7Both pokemon have started talking",
						"&7to each other!"
				);
			case IN_LOVE:
				return Lists.newArrayList(
						"&7Both pokemon have fallen in &dlove",
						"&7with each other!"
				);
			case OUT_ON_THE_TOWN:
				return Lists.newArrayList(
						"&7The Daycare officials report that",
						"&7your pokemon have gone out for",
						"&7a night on the town!"
				);
			case ONE_NIGHT_STAND:
				return Lists.newArrayList(
						"&7Both pokemon seem to be feeling",
						"&7a little romantic..."
				);
		}
	}

	private List<SpigotIcon> getStageIcons() {
		List<SpigotIcon> icons = Lists.newArrayList();

		ItemStack notReached = ItemStackUtils.itemBuilder()
				.material(Material.CONCRETE)
				.damage((short) 14)
				.name("&cNot yet achieved...")
				.build();
		SpigotIcon nr = new SpigotIcon(notReached);
		BreedStage stage = this.pen.getStage();
		if(stage != null) {
			int index = 28;
			for(BreedStage s : BreedStage.values()) {
				if(s.ordinal() >= stage.ordinal()) continue;

				ItemStack st = ItemStackUtils.itemBuilder()
						.material(Material.CONCRETE)
						.damage((short) 5)
						.name("&a" + this.getForStage(s))
						.lore(this.getForStageLore(s))
						.build();
				icons.add(new SpigotIcon(st));
				++index;
			}

			ItemStack current = ItemStackUtils.itemBuilder()
					.material(Material.CONCRETE)
					.damage((short) 4)
					.name("&e" + this.getForStage(stage))
					.lore(this.getForStageLore(stage))
					.build();
			icons.add(new SpigotIcon(current));
			index++;

			for(; index < 33; index++) {
				icons.add(nr);
			}
		} else {
			if(pen.getEgg().isPresent()) {
				for (BreedStage s : BreedStage.values()) {
					if (s == BreedStage.BRED) continue;

					ItemStack st = ItemStackUtils.itemBuilder()
							.material(Material.CONCRETE)
							.damage((short) 5)
							.name("&a" + this.getForStage(s))
							.lore(this.getForStageLore(s))
							.build();
					icons.add(new SpigotIcon(st));
				}
			} else {
				for(int i = 0; i < 5; i++) {
					icons.add(nr);
				}
			}
		}

		return icons;
	}

	static PenUIBuilder builder() {
		return new PenUIBuilder();
	}

	public static class PenUIBuilder implements Builder<PenUI> {

		private SpigotRanch ranch;
		private SpigotPen pen;
		private Player viewer;

		public PenUIBuilder ranch(SpigotRanch ranch) {
			this.ranch = ranch;
			return this;
		}

		public PenUIBuilder pen(SpigotPen pen) {
			this.pen = pen;
			return this;
		}

		public PenUIBuilder viewer(Player viewer) {
			this.viewer = viewer;
			return this;
		}

		@Override
		public PenUI build() {
			return new PenUI(this);
		}
	}
}
