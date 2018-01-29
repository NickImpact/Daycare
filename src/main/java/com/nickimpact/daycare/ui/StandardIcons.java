package com.nickimpact.daycare.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.api.gui.Icon;
import com.nickimpact.daycare.configuration.MsgConfigKeys;
import com.pixelmonmod.pixelmon.config.PixelmonItems;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.util.helpers.SpriteHelper;
import io.github.nucleuspowered.nucleus.api.exceptions.NucleusException;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.List;
import java.util.Map;

/**
 * (Some note will appear here)
 *
 * @author NickImpact (Nick DeGruccio)
 */
public class StandardIcons {

	public static Icon getPicture(Player player, int pos, EntityPixelmon pokemon) {
		net.minecraft.item.ItemStack item = new net.minecraft.item.ItemStack(PixelmonItems.itemPixelmonSprite);
		NBTTagCompound nbt = new NBTTagCompound();
		String idValue = String.format("%03d", pokemon.baseStats.nationalPokedexNumber);
		if (pokemon.isEgg) {
			switch (pokemon.getSpecies()) {
				case Manaphy:
				case Togepi:
					nbt.setString(NbtKeys.SPRITE_NAME,
							String.format("pixelmon:sprites/eggs/%s1", pokemon.getSpecies().name.toLowerCase()));
					break;
				default:
					nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/eggs/egg1");
					break;
			}
		} else if (pokemon.getIsShiny()) {
			nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/shinypokemon/" + idValue + SpriteHelper.getSpriteExtra(pokemon.getSpecies().name, pokemon.getForm()));
		} else {
			nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/pokemon/" + idValue + SpriteHelper.getSpriteExtra(pokemon.getSpecies().name, pokemon.getForm()));
		}

		item.setTagCompound(nbt);
		ItemStack is = ItemStackUtil.fromNative(item);
		applyInfo(player, is, pokemon);

		return new Icon(pos, is);
	}

	private static void applyInfo(Player player, ItemStack item, EntityPixelmon pokemon) {
		Text title;
		List<Text> lore;
		Map<String, Object> variables = Maps.newHashMap();
		variables.put("dummy", pokemon);

		try {
			title = DaycarePlugin.getInstance().getTextParsingUtils().parse(
					DaycarePlugin.getInstance().getMsgConfig().get(MsgConfigKeys.POKEMON_TITLE_PEN),
					player,
					null,
					variables
			);
		} catch (NucleusException e) {
			title = Text.of();
		}

		try {
			lore = DaycarePlugin.getInstance().getTextParsingUtils().parse(
					DaycarePlugin.getInstance().getMsgConfig().get(MsgConfigKeys.POKEMON_LORE_PEN),
					player,
					null,
					variables
			);
		} catch (NucleusException e) {
			lore = Lists.newArrayList();
		}

		item.offer(Keys.DISPLAY_NAME, title);
		item.offer(Keys.ITEM_LORE, lore);
	}
}
