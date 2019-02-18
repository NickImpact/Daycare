package com.nickimpact.daycare.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.configuration.MsgConfigKeys;
import com.nickimpact.daycare.ranch.Pokemon;
import com.nickimpact.daycare.utils.MessageUtils;
import com.nickimpact.impactor.gui.v2.Icon;
import com.pixelmonmod.pixelmon.config.PixelmonItems;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.util.helpers.SpriteHelper;
import io.github.nucleuspowered.nucleus.api.exceptions.NucleusException;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Map;

/**
 * (Some note will appear here)
 *
 * @author NickImpact (Nick DeGruccio)
 */
public class StandardIcons {

	public static Icon getPicture(Player player, Pokemon pokemon, List<String> baselore) {
		return new Icon(createPicture(player, pokemon, baselore));
	}

	private static ItemStack createPicture(Player player, Pokemon poke, List<String> baselore) {
		com.pixelmonmod.pixelmon.api.pokemon.Pokemon pokemon = poke.getPokemon();
		net.minecraft.item.ItemStack nativeItem = new net.minecraft.item.ItemStack(PixelmonItems.itemPixelmonSprite);
		NBTTagCompound nbt = new NBTTagCompound();
		String idValue = String.format("%03d", pokemon.getBaseStats().nationalPokedexNumber);
		if (pokemon.isEgg()){
			switch(pokemon.getSpecies()) {
				case Manaphy:
				case Togepi:
					nbt.setString(NbtKeys.SPRITE_NAME, String.format("pixelmon:sprites/eggs/%s1", pokemon.getSpecies().name.toLowerCase()));
				default:
					nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/eggs/egg1");
			}
		} else {
			if (pokemon.isShiny()) {
				nbt.setString(NbtKeys.SPRITE_NAME,
						"pixelmon:sprites/shinypokemon/" + idValue + SpriteHelper.getSpriteExtra(
								pokemon.getBaseStats().pixelmonName, pokemon.getForm()));
			} else {
				nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/pokemon/" + idValue + SpriteHelper.getSpriteExtra(
						pokemon.getBaseStats().pixelmonName, pokemon.getForm()));
			}
		}
		nativeItem.setTagCompound(nbt);
		ItemStack item = (ItemStack) (Object) nativeItem;
		applyInfo(player, item, poke, baselore);
		return item;
	}

	private static void applyInfo(Player player, ItemStack item, Pokemon pokemon, List<String> baselore) {
		Text title;
		List<Text> lore;
		Map<String, Object> variables = Maps.newHashMap();
		variables.put("dummy", pokemon);
		variables.put("dummy2", pokemon.getPokemon());

		if(pokemon.getPokemon().isEgg()) {
			title = MessageUtils.fetchMsg(player, MsgConfigKeys.EGG);
			item.offer(Keys.DISPLAY_NAME, title);
		} else {
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
						baselore,
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
}
