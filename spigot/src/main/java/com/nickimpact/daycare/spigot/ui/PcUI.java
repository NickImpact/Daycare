package com.nickimpact.daycare.spigot.ui;

import com.google.common.collect.Lists;
import com.nickimpact.daycare.spigot.SpigotDaycarePlugin;
import com.nickimpact.daycare.spigot.implementation.SpigotPen;
import com.nickimpact.daycare.spigot.implementation.SpigotRanch;
import com.nickimpact.daycare.spigot.utils.ItemStackUtils;
import com.nickimpact.daycare.spigot.utils.LoreForging;
import com.nickimpact.daycare.spigot.utils.MessageUtils;
import com.nickimpact.impactor.api.gui.InventoryDimensions;
import com.nickimpact.impactor.spigot.ui.SpigotIcon;
import com.nickimpact.impactor.spigot.ui.SpigotLayout;
import com.nickimpact.impactor.spigot.ui.SpigotPage;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PCBox;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import lombok.AllArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class PcUI {

	private SpigotPage<PcPosition> page;
	private Player viewer;

	private SpigotRanch ranch;
	private SpigotPen pen;
	private int slot;

	private PCStorage pc;

	public PcUI(Player viewer, SpigotRanch ranch, SpigotPen pen, int slot) {
		this.viewer = viewer;
		this.ranch = ranch;
		this.pen = pen;
		this.slot = slot;

		this.pc = Pixelmon.storageManager.getPCForPlayer(viewer.getUniqueId());
		this.page = SpigotPage.builder()
				.viewer(viewer)
				.view(this.display())
				.title("&cDaycare &7\u00bb &3PC")
				.contentZone(new InventoryDimensions(6, 5))
				.previousPage(Material.matchMaterial("PIXELMON_TRADE_HOLDER_LEFT"), 51)
				.currentPage(Material.matchMaterial("PIXELMON_TRADE_MONITOR"), 52)
				.nextPage(Material.matchMaterial("PIXELMON_TRADE_HOLDER_RIGHT"), 53)
				.build();
		this.page.applier(wrapper -> {
			if(wrapper.pokemon != null) {
				ItemStack display = CommonUIComponents.pokemonDisplay(wrapper.pokemon);
				ItemMeta meta = display.getItemMeta();
				meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', String.format(
						"&3%s %s&7| &bLvl %d",
						wrapper.pokemon.getSpecies().getPokemonName(),
						wrapper.pokemon.isShiny() ? "&7(&6Shiny&7) " : "",
						wrapper.pokemon.getLevel()
				)));

				LoreForging.craftLore(wrapper.pokemon, display, meta, -1);

				SpigotIcon icon = new SpigotIcon(display);
				icon.addListener(clickable -> {
					if(!wrapper.pokemon.isEgg()) {
						this.page.close();
						this.pc.set(wrapper.box, wrapper.pos, null);
						this.pen.addAtSlot(wrapper.pokemon, this.slot);
						SpigotDaycarePlugin.getInstance().getService().getStorage().updateRanch(this.ranch);
						this.viewer.sendMessage(MessageUtils.parse(String.format("&7You've added your &e%s &7to the daycare!", wrapper.pokemon.getSpecies().getPokemonName()), true, false));
						PenUI.builder().viewer(this.viewer).ranch(this.ranch).pen(this.pen).build().open();
					}
				});

				return icon;
			} else {
				return new SpigotIcon(ItemStackUtils.itemBuilder().material(Material.AIR).build());
			}
		});
		this.page.define(this.getBoxContents());
	}

	public void open() {
		this.page.open();
	}

	private SpigotLayout display() {
		SpigotLayout.SpigotLayoutBuilder builder = SpigotLayout.builder();
		builder.slots(SpigotIcon.BORDER, 6, 7, 8, 15, 17, 24, 26, 33, 35, 42, 43, 44, 45, 46, 47, 48, 49, 50);

		ItemStack party = ItemStackUtils.itemBuilder()
				.material(Material.matchMaterial("PIXELMON_GS_BALL"))
				.name("&eOpen Party")
				.build();
		SpigotIcon icon = new SpigotIcon(party);
		icon.addListener(clickable -> {
			new PartyUI(this.viewer, this.ranch, this.pen, this.slot).open();
		});
		builder.slot(icon, 25);

		return builder.build();
	}

	private List<PcPosition> getBoxContents() {
		List<PcPosition> pokemon = Lists.newArrayList();
		int b = 0;
		for(PCBox box : this.pc.getBoxes()) {
			for(int i = 0; i < 30; i++) {
				pokemon.add(new PcPosition(box.get(i), b, i));
			}
			b++;
		}

		return pokemon;
	}

	@AllArgsConstructor
	private static class PcPosition {
		private Pokemon pokemon;
		private int box;
		private int pos;
	}

}
