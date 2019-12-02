package com.nickimpact.daycare.spigot.ui;

import com.nickimpact.daycare.spigot.SpigotDaycarePlugin;
import com.nickimpact.daycare.spigot.implementation.SpigotPen;
import com.nickimpact.daycare.spigot.implementation.SpigotRanch;
import com.nickimpact.daycare.spigot.utils.ItemStackUtils;
import com.nickimpact.daycare.spigot.utils.LoreForging;
import com.nickimpact.daycare.spigot.utils.MessageUtils;
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

public class PartyUI {

	private SpigotUI display;
	private Player viewer;

	private SpigotRanch ranch;
	private SpigotPen pen;
	private int slot;

	private PlayerPartyStorage party;

	public PartyUI(Player viewer, SpigotRanch ranch, SpigotPen pen, int slot) {
		this.viewer = viewer;
		this.ranch = ranch;
		this.pen = pen;
		this.slot = slot;

		this.party = Pixelmon.storageManager.getParty(viewer.getUniqueId());

		this.display = this.createDisplay();
	}

	public void open() {
		this.display.open(this.viewer);
	}

	private SpigotUI createDisplay() {
		SpigotUI.SpigotUIBuilder sb = SpigotUI.builder();
		sb.title("&cDaycare &7\u00bb &3Party");
		sb.size(27);
		return sb.build().define(this.design());
	}

	private SpigotLayout design() {
		SpigotLayout.SpigotLayoutBuilder slb = SpigotLayout.builder();
		slb.rows(SpigotIcon.BORDER, 0, 2);
		slb.slots(SpigotIcon.BORDER, 9, 16);

		for(int i = 10; i < 16; i++) {
			Pokemon pokemon = this.party.get(i - 10);
			if(pokemon == null) continue;

			ItemStack display = CommonUIComponents.pokemonDisplay(pokemon);
			ItemMeta meta = display.getItemMeta();
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', String.format(
					"&3%s %s&7| &bLvl %d",
					pokemon.getSpecies().getPokemonName(),
					pokemon.isShiny() ? "&7(&6Shiny&7) " : "",
					pokemon.getLevel()
			)));

			LoreForging.craftLore(pokemon, display, meta, -1);

			SpigotIcon icon = new SpigotIcon(display);
			if(!pokemon.isEgg()) {
				icon.addListener(clickable -> {
					this.display.close(this.viewer);

					if(this.party.getTeam().size() != 1) {
						this.pen.addAtSlot(pokemon, slot);
						this.party.set(this.party.getPosition(pokemon), null);
						SpigotDaycarePlugin.getInstance().getService().getStorage().updateRanch(this.ranch);
						this.viewer.sendMessage(MessageUtils.parse(String.format("&7You've added your &e%s &7to the daycare!", pokemon.getSpecies().getPokemonName()), true, false));
					} else {
						this.viewer.sendMessage(MessageUtils.parse("You can't deposit your last non-egg party member!", true, false));
					}
				});
			}
			slb.slot(icon, i);
		}

		ItemStack pc = ItemStackUtils.itemBuilder()
				.material(Material.matchMaterial("PIXELMON_PC"))
				.name("&eOpen PC")
				.build();
		SpigotIcon icon = new SpigotIcon(pc);
		icon.addListener(clickable -> {
			new PcUI(this.viewer, this.ranch, this.pen, this.slot).open();
		});
		slb.slot(icon, 17);

		return slb.build();
	}


}
