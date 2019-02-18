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
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;

import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

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
		PlayerPartyStorage storage = Pixelmon.storageManager.getParty(player.getUniqueId());

		int index = 10;
		for(com.pixelmonmod.pixelmon.api.pokemon.Pokemon pokemon : storage.getAll()) {
			if(index == 13) {
				++index;
			}

			if(pokemon == null) {
				++index;
				continue;
			}

			Icon icon = StandardIcons.getPicture(player, new Pokemon(pokemon), DaycarePlugin.getInstance().getMsgConfig().get(MsgConfigKeys.POKEMON_LORE_SELECT));
			if(!pokemon.isEgg()) {
				final int pos = index;
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

					this.selection = new Selection(pokemon, (pos > 13 ? pos - 11 : pos - 10));
					Icon confirm = Icon.from(
							ItemStack.builder()
									.itemType(ItemTypes.DYE)
									.add(Keys.DYE_COLOR, DyeColors.LIME)
									.add(Keys.DISPLAY_NAME, MessageUtils.fetchMsg(player, MsgConfigKeys.PARTY_CONFIRM))
									.build()
					);
					confirm.addListener(clickable1 -> {
						if (storage.countPokemon() == 1) {
							return;
						}
						storage.retrieveAll();
						storage.set(this.selection.getSlot(), null);
						Pokemon rep = new Pokemon(pokemon);
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
				lb.slot(icon, pos);
			}

			++index;
		}

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

	private boolean only1AblePokemon(PlayerPartyStorage storage) {
		int able = 0;
		for(com.pixelmonmod.pixelmon.api.pokemon.Pokemon pokemon : storage.getAll()) {
			if(pokemon != null) {
				if(!pokemon.isEgg()) {
					++able;
				}
			}
		}

		return able <= 1;
	}
}
