package com.nickimpact.daycare.generations.ui;

import com.pixelmongenerations.common.entity.pixelmon.EntityPixelmon;
import com.pixelmongenerations.core.config.PixelmonItems;
import com.pixelmongenerations.core.storage.NbtKeys;
import com.pixelmongenerations.core.util.helper.SpriteHelper;
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
			if (pokemon.isShiny()) {
				nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/shinypokemon/" + idValue + SpriteHelper.getSpriteExtra(pokemon.getName(), pokemon.getForm(), pokemon.getGender(), pokemon.getSpecialTextureIndex()));
			} else {
				nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/pokemon/" + idValue + SpriteHelper.getSpriteExtra(pokemon.getName(), pokemon.getForm(), pokemon.getGender(), pokemon.getSpecialTextureIndex()));
			}
		}
		nativeItem.setTagCompound(nbt);
		return (ItemStack) (Object) (nativeItem);
	}

}
