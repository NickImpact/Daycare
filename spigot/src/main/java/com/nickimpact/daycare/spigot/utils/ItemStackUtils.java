package com.nickimpact.daycare.spigot.utils;

import com.nickimpact.impactor.api.building.Builder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class ItemStackUtils {

	public static ItemStackBuilder itemBuilder() {
		return new ItemStackBuilder();
	}

	public static class ItemStackBuilder implements Builder<ItemStack> {

		private ItemStack original;

		private Material material;
		private int amount = 1;
		private short damage = 0;

		private String name;
		private List<String> lore;

		public ItemStackBuilder fromItem(ItemStack item) {
			this.original = item;
			return this;
		}

		public ItemStackBuilder material(Material material) {
			this.material = material;
			return this;
		}

		public ItemStackBuilder amount(int amount) {
			this.amount = amount;
			return this;
		}

		public ItemStackBuilder damage(short damage) {
			this.damage = damage;
			return this;
		}

		public ItemStackBuilder name(String name) {
			this.name = ChatColor.translateAlternateColorCodes('&', name);
			return this;
		}

		public ItemStackBuilder lore(List<String> lore) {
			this.lore = lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).collect(Collectors.toList());
			return this;
		}

		@Override
		public ItemStack build() {
			if(this.original != null) {
				return buildStack(original);
			} else {
				ItemStack stack = new ItemStack(this.material, this.amount, this.damage);
				return buildStack(stack);
			}
		}

		@NotNull
		private ItemStack buildStack(ItemStack item) {
			ItemMeta meta = item.getItemMeta();
			if (this.name != null) {
				meta.setDisplayName(this.name);
			}

			if (this.lore != null) {
				meta.setLore(lore);
			}
			item.setItemMeta(meta);
			return item;
		}
	}
}
