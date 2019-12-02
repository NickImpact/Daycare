package com.nickimpact.daycare.spigot.utils;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.IVStore;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Collectors;

public class LoreForging {

	public static void craftLore(Pokemon pokemon, ItemStack e, ItemMeta meta, int gained) {
		List<String> lore = Lists.newArrayList();
		if(gained != -1) {
			lore.add("&7Gained Levels: &e" + gained);
			lore.add("");
		}

		if(pokemon.getNickname() != null && !pokemon.getNickname().isEmpty()) {
			lore.add("&7Nickname: &e" + pokemon.getNickname());
		}
		lore.add("&7Ability: &e" + pokemon.getAbility().getTranslatedName().getFormattedText());
		lore.add("&7Gender: &e" + pokemon.getGender().getLocalizedName());
		lore.add("&7Nature: &e" + pokemon.getNature().getLocalizedName());
		lore.add("&7Size: &e" + pokemon.getGrowth().getLocalizedName());

		if(!pokemon.getHeldItem().equals(net.minecraft.item.ItemStack.EMPTY)) {
			lore.add("&7Holding: &e" + pokemon.getHeldItem().getDisplayName());
		}

		lore.add("");
		lore.add(String.format("&7IVs: &e%d&7/&e186 &7(&a%s%%&7)", (int) totalIVs(pokemon.getIVs()), new DecimalFormat("#0.##").format(totalIVs(pokemon.getIVs()) / 186.0 * 100.0)));

		IVStore ivs = pokemon.getIVs();
		lore.add(String.format("&7IVs: &e%d&7/&e%d&7/&e%d&7/&e%d&7/&e%d&7/&e%d&7", ivs.hp, ivs.attack, ivs.defence, ivs.specialAttack, ivs.specialDefence, ivs.speed));
		meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).collect(Collectors.toList()));
		e.setItemMeta(meta);
	}

	private static double totalIVs(IVStore ivs) {
		return ivs.hp + ivs.attack + ivs.defence + ivs.specialAttack + ivs.specialDefence + ivs.speed;
	}

	public static String genderSymbol(Gender gender) {
		switch(gender) {
			case Male:
				return ChatColor.AQUA + "\u2642";
			case Female:
				return ChatColor.LIGHT_PURPLE + "\u2640";
			default:
				return "";
		}
	}
}
