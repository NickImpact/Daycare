package com.nickimpact.daycare.generations.ui;

import com.pixelmonmod.pixelmon.config.PixelmonItems;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.util.helpers.SpriteHelper;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.item.inventory.ItemStack;

public class GenerationsIcons {

	public static ItemStack pokemonDisplay(EntityPixelmon pokemon) {
		net.minecraft.item.ItemStack nativeItem = new net.minecraft.item.ItemStack(PixelmonItems.itemPixelmonSprite);
		NBTTagCompound nbt = new NBTTagCompound();
		String idValue = String.format("%03d", pokemon.baseStats.nationalPokedexNumber);
		if (pokemon.isEgg) {
			switch(pokemon.getSpecies()) {
				case Manaphy:
					nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/eggs/manaphy1");
					break;
				case Togepi:
					nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/eggs/togepi1");
					break;
				default:
					nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/eggs/egg1");
					break;
			}
		} else {
			if (pokemon.getIsShiny()) {
				nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/shinypokemon/" + idValue + SpriteHelper.getSpriteExtra(pokemon.getName(), pokemon.getForm(), pokemon.getGender(), pokemon.getSpecialTextureIndex()));
			} else {
				nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/pokemon/" + idValue + SpriteHelper.getSpriteExtra(pokemon.getName(), pokemon.getForm(), pokemon.getGender(), pokemon.getSpecialTextureIndex()));
			}
		}
		nativeItem.setTagCompound(nbt);
		return (ItemStack) (Object) (nativeItem);
	}

}
