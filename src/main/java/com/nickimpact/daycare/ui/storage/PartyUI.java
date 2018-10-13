package com.nickimpact.daycare.ui.storage;

import com.google.common.collect.Lists;
import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.configuration.ConfigKeys;
import com.nickimpact.daycare.configuration.MsgConfigKeys;
import com.nickimpact.daycare.ranch.Pen;
import com.nickimpact.daycare.ranch.Pokemon;
import com.nickimpact.daycare.ranch.Ranch;
import com.nickimpact.daycare.ui.PenUI;
import com.nickimpact.daycare.ui.StandardIcons;
import com.nickimpact.daycare.utils.MessageUtils;
import com.nickimpact.impactor.gui.v2.Displayable;
import com.nickimpact.impactor.gui.v2.Icon;
import com.nickimpact.impactor.gui.v2.Layout;
import com.nickimpact.impactor.gui.v2.UI;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class PartyUI implements Displayable {

	private final PokemonSpec UNBREEDABLE = new PokemonSpec("unbreedable");

	private Ranch ranch;
	private Pen pen;
	private int slot;
	private UI display;

	private Selection selection;

	public PartyUI(Player player, Ranch ranch, Pen pen, int id, int slot) {
		this.ranch = ranch;
		this.pen = pen;
		this.slot = slot;

		this.display = UI.builder()
				.property(InventoryDimension.of(9, 5))
				.title(MessageUtils.fetchMsg(player, MsgConfigKeys.PARTY_TITLE))
				.build(DaycarePlugin.getInstance())
				.define(setupDisplay(player, id));
	}

	@Override
	public UI getDisplay() {
		return this.display;
	}

	private Layout setupDisplay(Player player, int id) {
		Layout.Builder lb = Layout.builder().dimension(InventoryDimension.of(9, 5)).border();
		Optional<PlayerStorage> storage = PixelmonStorage.pokeBallManager.getPlayerStorage((EntityPlayerMP)player);
		storage.ifPresent(stor -> {
			for(int i = 10; i < 17; i++) {
				if(i == 13) {
					continue;
				}

				int k = i;
				if(k > 13) {
					--k;
				}

				NBTTagCompound nbt;
				nbt = stor.partyPokemon[k - 10];
				if(nbt == null) {
					continue;
				}

				EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, (World) player.getWorld());
				Icon icon = StandardIcons.getPicture(player, new Pokemon(pokemon), DaycarePlugin.getInstance().getMsgConfig().get(MsgConfigKeys.POKEMON_LORE_SELECT));

				if(!pokemon.isEgg) {
					final int index = k;
					final int pos = i;
					icon.addListener(clickable -> {
						if(this.UNBREEDABLE.matches(pokemon)) {
							player.sendMessage(Text.of(DaycarePlugin.getInstance().getPluginInfo().error(), TextColors.GRAY, "That pokemon is marked as unbreedable..."));
							return;
						}
						icon.getDisplay().offer(Keys.ITEM_ENCHANTMENTS, Lists.newArrayList(
								Enchantment.builder()
										.type(EnchantmentTypes.UNBREAKING)
										.level(1)
										.build()
						));
						icon.getDisplay().offer(Keys.HIDE_ENCHANTMENTS, true);
						this.display.setSlot(pos, icon);
						this.selection = new Selection(pokemon, index - 10);
						Icon confirm = Icon.from(
								ItemStack.builder()
										.itemType(ItemTypes.DYE)
										.add(Keys.DYE_COLOR, DyeColors.LIME)
										.add(Keys.DISPLAY_NAME, MessageUtils.fetchMsg(player, MsgConfigKeys.PARTY_CONFIRM))
										.build()
						);
						confirm.addListener(clickable1 -> {
							if (stor.partyPokemon.length == 1 || only1AblePokemon(stor)) {
								return;
							}
							stor.recallAllPokemon();
							stor.removeFromPartyPlayer(this.selection.getSlot());
							Pokemon rep = new Pokemon(this.selection.getPokemon());
							if (this.slot == 1) {
								this.pen.setSlot1(rep);
							} else {
								this.pen.setSlot2(rep);
							}
							if(this.pen.isFull() && DaycarePlugin.getInstance().getConfig().get(ConfigKeys.BREEDING_ENABLED)) {
								this.pen.initialize(ranch.getOwnerUUID());
							}
							clickable.getPlayer().closeInventory();
							new PenUI(player, this.ranch, this.pen, id).open(player);
						});
						this.display.setSlot(33, confirm);

					});
					lb.slot(icon, i);
				}
			}
		});

		lb.row(Icon.BORDER, 2);

		Icon back = Icon.from(ItemStack.builder().itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:eject_button").get()).add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Go Back ")).build());
		back.addListener(clickable -> {
			this.close(player);
			new PenUI(clickable.getPlayer(), this.ranch, this.pen, id).open(player);
		});
		lb.slot(back, 29);

		Icon pc = Icon.from(
				ItemStack.builder()
						.itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:pc").orElse(ItemTypes.BARRIER))
						.add(Keys.DISPLAY_NAME, MessageUtils.fetchMsg(player, MsgConfigKeys.PARTY_PC))
						.build()
		);
		pc.addListener(clickable -> {
			this.close(player);
			new PcUI(clickable.getPlayer(), this.ranch, this.pen, id, this.slot).open(player, 1);
		});
		lb.slot(pc, 31);

		Icon noSelection = Icon.from(
				ItemStack.builder()
						.itemType(ItemTypes.BARRIER)
						.add(Keys.DISPLAY_NAME, MessageUtils.fetchMsg(player, MsgConfigKeys.PARTY_NO_SELECTION))
						.build()
		);
		lb.slot(noSelection, 33);
		return lb.build();
	}

	private boolean only1AblePokemon(PlayerStorage storage) {
		int able = 0;
		for(NBTTagCompound nbt : storage.partyPokemon) {
			if(nbt != null) {
				if(!nbt.getBoolean(NbtKeys.IS_EGG)) {
					++able;
				}
			}
		}

		return able <= 1;
	}
}
