package com.nickimpact.daycare.spigot.ui;

import com.google.common.collect.Lists;
import com.nickimpact.daycare.spigot.SpigotDaycarePlugin;
import com.nickimpact.daycare.spigot.implementation.SpigotDaycarePokemonWrapper;
import com.nickimpact.daycare.spigot.implementation.SpigotPen;
import com.nickimpact.daycare.spigot.implementation.SpigotRanch;
import com.nickimpact.daycare.spigot.utils.ItemStackUtils;
import com.nickimpact.daycare.spigot.utils.LoreForging;
import com.nickimpact.impactor.api.gui.InventoryDimensions;
import com.nickimpact.impactor.spigot.ui.SpigotIcon;
import com.nickimpact.impactor.spigot.ui.SpigotLayout;
import com.nickimpact.impactor.spigot.ui.SpigotPage;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class RanchUI {

	private SpigotPage<SpigotPen> page;
	private Player viewer;

	private SpigotRanch ranch;

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

	private SpigotRanch getRanch(Player player) {
		return (SpigotRanch) SpigotDaycarePlugin.getInstance().getService().getRanchManager().getRanch(player.getUniqueId()).orElseThrow(() -> new RuntimeException("Unable to find " + player.getName() + "'s ranch"));
	}

	private SpigotPage<SpigotPen> createPage() {
		SpigotPage.SpigotPageBuilder spb = SpigotPage.builder();
		spb.title(ChatColor.translateAlternateColorCodes('&', "&cDaycare &7\u00bb &3Ranches"));
		spb.contentZone(new InventoryDimensions(7, 3));
		spb.offsets(1);
		spb.lastPage(Material.matchMaterial("PIXELMON_TRADE_HOLDER_LEFT"), 50);
		spb.currentPage(Material.matchMaterial("PIXELMON_TRADE_MONITOR"), 51);
		spb.nextPage(Material.matchMaterial("PIXELMON_TRADE_HOLDER_RIGHT"), 52);
		spb.view(SpigotLayout.builder().dimension(9, 6)
				.rows(SpigotIcon.BORDER, 0, 4)
				.columns(SpigotIcon.BORDER, 0, 8)
				.slot(SpigotIcon.BORDER, 49)
				.slot(new SpigotIcon(ItemStackUtils.itemBuilder().material(Material.BARRIER).name("&cComing Soon...").build()), 47)
				.build()
		);
		spb.viewer(this.viewer);
		return spb.build();
	}

	private SpigotIcon createPenIcon(SpigotPen pen, int index) {
		if(pen.isUnlocked()) {
			List<String> lore = Lists.newArrayList();
			lore.add(this.forSlot(pen, 1));
			lore.add(this.forSlot(pen, 2));

			if(pen.getEgg().isPresent()) {
				lore.add("");
				lore.add(ChatColor.translateAlternateColorCodes('&', "&e\u2730 &aEgg Available &e\u2730"));
			}

			ItemStack display = ItemStackUtils.itemBuilder()
					.material(Material.matchMaterial("PIXELMON_RANCH"))
					.name(ChatColor.translateAlternateColorCodes('&', String.format("&ePen %d", index)))
					.lore(lore)
					.build();

			SpigotIcon icon = new SpigotIcon(display);
			icon.addListener(clickable -> {
				PenUI.builder().ranch(ranch).pen(pen).viewer(viewer).build().open();
			});
			return icon;
		} else {
			List<String> lore = Lists.newArrayList();
			lore.add(ChatColor.translateAlternateColorCodes('&', "&cCurrently locked..."));
			lore.add("");
//			lore.add(ChatColor.translateAlternateColorCodes('&', String.format("&7Price: &e%s", SpigotDaycarePlugin.getInstance().getService().getActiveModule().getRequirement(index - 1))));
			// TODO - Implement logic to reference unlock description

			ItemStack display = ItemStackUtils.itemBuilder()
					.material(Material.matchMaterial("PIXELMON_RANCH"))
					.name(ChatColor.translateAlternateColorCodes('&', String.format("&ePen %d", index)))
					.lore(lore)
					.build();
			return new SpigotIcon(display);
		}
	}

	private String forSlot(SpigotPen pen, int slot) {
		if (pen.getAtPosition(slot).isPresent()) {
			SpigotDaycarePokemonWrapper wrapper = pen.getAtPosition(slot).get();
			return ChatColor.translateAlternateColorCodes(
					'&', String.format(
							"&7Slot %d&7: &e%s %s &7(&aLvl %d&7)",
							slot,
							wrapper.getDelegate().getSpecies().getPokemonName(),
							LoreForging.genderSymbol(wrapper.getDelegate().getGender()),
							wrapper.getDelegate().getLevel() + wrapper.getGainedLevels()
					));
		} else {
			return ChatColor.translateAlternateColorCodes('&', String.format("&7Slot %d&7: &cEmpty", slot));
		}
	}
}
