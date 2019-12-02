package com.nickimpact.daycare.spigot.ui;

import com.google.common.collect.Lists;
import com.nickimpact.daycare.spigot.SpigotDaycarePlugin;
import com.nickimpact.daycare.spigot.implementation.SpigotPen;
import com.nickimpact.daycare.spigot.implementation.SpigotRanch;
import com.nickimpact.daycare.spigot.utils.ItemStackUtils;
import com.nickimpact.daycare.spigot.utils.MessageUtils;
import com.nickimpact.impactor.spigot.ui.SpigotIcon;
import com.nickimpact.impactor.spigot.ui.SpigotLayout;
import com.nickimpact.impactor.spigot.ui.SpigotUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.stream.Collectors;

public class SettingsUI {

	private SpigotUI display;
	private Player viewer;
	private SpigotRanch ranch;
	private SpigotPen pen;

	private static final SpigotIcon ON;
	private static final SpigotIcon OFF;

	private static final SpigotIcon MOVES;
	private static final SpigotIcon LEVEL;
	private static final SpigotIcon EVOLVE;

	static {
		ItemStack on = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 5);
		ItemStack off = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
		ItemMeta onmeta = on.getItemMeta();
		onmeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c"));
		on.setItemMeta(onmeta);
		ON = new SpigotIcon(on);

		ItemMeta offmeta = off.getItemMeta();
		offmeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c"));
		off.setItemMeta(offmeta);
		OFF = new SpigotIcon(off);

		ItemStack moves = new ItemStack(Material.matchMaterial("PIXELMON_TM1"));
		ItemMeta moveMeta = moves.getItemMeta();
		moveMeta.setDisplayName(MessageUtils.parse("&eLearn Moves?", false, false));
		moveMeta.setLore(Lists.newArrayList(
				"&7Should pokemon learn moves",
				"&7as they level up in the",
				"&7daycare?",
				"",
				"&a&lNOTE:",
				"&7This setting depends on",
				"&7the leveling setting to",
				"&7be enabled!"
		).stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).collect(Collectors.toList()));
		moves.setItemMeta(moveMeta);
		MOVES = new SpigotIcon(moves);

		ItemStack level = new ItemStack(Material.matchMaterial("PIXELMON_EXP_SHARE"));
		ItemMeta levelMeta = moves.getItemMeta();
		levelMeta.setDisplayName(MessageUtils.parse("&eAllow Leveling?", false, false));
		levelMeta.setLore(Lists.newArrayList(
				"&7Should pokemon be able",
				"&7to level up as they spend",
				"&7time in the daycare?"
		).stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).collect(Collectors.toList()));
		level.setItemMeta(levelMeta);
		LEVEL = new SpigotIcon(level);

		ItemStack evolve = new ItemStack(Material.matchMaterial("PIXELMON_SHINY_STONE"));
		ItemMeta meta = evolve.getItemMeta();
		meta.setDisplayName(MessageUtils.parse("&eAllow Pokemon to Evolve?", false, false));
		meta.setLore(Lists.newArrayList(
				"&7Should pokemon be able",
				"&7to evolve from leveling up",
				"&7in the daycare?",
				"",
				"&a&lNOTE:",
				"&7This setting depends on",
				"&7the leveling setting to",
				"&7be enabled!"
		).stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).collect(Collectors.toList()));
		evolve.setItemMeta(meta);
		EVOLVE = new SpigotIcon(evolve);
	}

	public SettingsUI(Player viewer, SpigotRanch ranch, SpigotPen pen) {
		this.ranch = ranch;
		this.pen = pen;
		this.viewer = viewer;

		this.display = this.createDisplay();
		this.display.define(this.layout());
	}

	public void open() {
		this.display.open(this.viewer);
	}

	private SpigotUI createDisplay() {
		return SpigotUI.builder().size(54).title("&cDaycare &7\u00bb &3Settings").build();
	}

	private SpigotLayout layout() {
		SpigotLayout.SpigotLayoutBuilder slb = SpigotLayout.builder();
		slb.border();
		slb.slot(LEVEL, 11).slot(MOVES, 15);
		slb.column(SpigotIcon.BORDER, 4).slot(EVOLVE, 40);
		slb.hollowSquare(SpigotIcon.BORDER, 37).hollowSquare(SpigotIcon.BORDER, 43);

		SpigotIcon back = new SpigotIcon(ItemStackUtils.itemBuilder()
				.material(Material.matchMaterial("PIXELMON_EJECT_BUTTON"))
				.name("&c\u2190 Go Back \u2190")
				.build());

		back.addListener(clickable -> {
			PenUI.builder().ranch(ranch).pen(pen).viewer(viewer).build().open();
		});
		slb.slot(back, 37);

		SpigotIcon confirm = new SpigotIcon(ItemStackUtils.itemBuilder()
				.material(Material.INK_SACK)
				.damage((short) 10)
				.name("&aConfirm Action")
				.build()
		);
		confirm.addListener(clickable -> {
			this.pen.getSettings().setCanLevel(this.display.getIcon(10).get() == ON);
			this.pen.getSettings().setCanLearnMoves(this.display.getIcon(14).get() == ON);
			this.pen.getSettings().setCanEvolve(this.display.getIcon(39).get() == ON);
			this.viewer.sendMessage(MessageUtils.parse("&7Settings Applied!", true, false));
			SpigotDaycarePlugin.getInstance().getService().getStorage().updateRanch(this.ranch);
			PenUI.builder().ranch(ranch).pen(pen).viewer(viewer).build().open();
		});
		slb.slot(confirm, 43);

		if(this.pen.getSettings().canLevel()) {
			slb.hollowSquare(ON, 11);
		} else {
			slb.hollowSquare(OFF, 11);
		}

		if(this.pen.getSettings().canLearnMoves()) {
			slb.hollowSquare(ON, 15);
		} else {
			slb.hollowSquare(OFF, 15);
		}

		if(this.pen.getSettings().canEvolve()) {
			slb.hollowSquare(ON, 40);
		} else {
			slb.hollowSquare(OFF, 40);
		}

		final SpigotLayout.SpigotLayoutBuilder copy = slb;
		LEVEL.addListener(clickable -> {
			if(this.display.getIcon(10).get() == OFF) {
				this.display.define(copy.hollowSquare(ON, 11).build());
			} else {
				copy.hollowSquare(OFF, 11);
				if(this.display.getIcon(14).get() == ON) {
					copy.hollowSquare(OFF, 15);
				}

				if(this.display.getIcon(39).get() == ON) {
					copy.hollowSquare(OFF, 40);
				}
				this.display.define(copy.build());
			}
		});
		MOVES.addListener(clickable -> {
			if(this.display.getIcon(14).get() == OFF && this.display.getIcon(10).get() == ON) {
				copy.hollowSquare(ON, 15);
			} else {
				copy.hollowSquare(OFF, 15);
			}
			this.display.define(copy.build());
		});

		EVOLVE.addListener(clickable -> {
			if(this.display.getIcon(39).get() == OFF && this.display.getIcon(10).get() == ON) {
				copy.hollowSquare(ON, 40);
			} else {
				copy.hollowSquare(OFF, 40);
			}
			this.display.define(copy.build());
		});

		return slb.build();
	}
}
