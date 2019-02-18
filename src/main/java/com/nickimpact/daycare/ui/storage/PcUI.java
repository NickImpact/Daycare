package com.nickimpact.daycare.ui.storage;

import com.google.common.collect.Lists;
import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.configuration.MsgConfigKeys;
import com.nickimpact.daycare.ranch.Pen;
import com.nickimpact.daycare.ranch.Pokemon;
import com.nickimpact.daycare.ranch.Ranch;
import com.nickimpact.daycare.ui.PenUI;
import com.nickimpact.daycare.ui.StandardIcons;
import com.nickimpact.daycare.utils.MessageUtils;
import com.nickimpact.impactor.gui.v2.*;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.api.storage.PCBox;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PcUI implements PageDisplayable {

	private final PokemonSpec UNBREEDABLE = new PokemonSpec("unbreedable");

	private Ranch ranch;
	private Pen pen;
	private int penID;
	private int slot;
	private Page display;

	/** The player's PC at time of use */
	private List<PCRepresentable> pc;

	private Selection selection;

	public PcUI(Player player, Ranch ranch, Pen pen, int penID, int slot) {
		this.ranch = ranch;
		this.pen = pen;
		this.penID = penID;
		this.slot = slot;
		this.pc = getBoxContents(player);

		this.display = Page.builder()
				.property(InventoryTitle.of(MessageUtils.fetchMsg(player, MsgConfigKeys.PC_TITLE)))
				.layout(this.buildBaseDesign(player))
				.previous(Icon.from(
						ItemStack.builder()
								.itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:trade_holder_left").orElse(ItemTypes.BARRIER))
								.build()
				), 51)
				.current(Icon.from(
						ItemStack.builder()
								.itemType(ItemTypes.PAPER)
								.build()
				), 52)
				.next(Icon.from(
						ItemStack.builder()
								.itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:trade_holder_right").orElse(ItemTypes.BARRIER))
								.build()
				), 53)
				.build(DaycarePlugin.getInstance())
				.define(this.forgePCIcons(player), InventoryDimension.of(6, 5));
	}

	private Layout buildBaseDesign(Player player) {
		Layout.Builder builder = Layout.builder().dimension(InventoryDimension.of(9, 6))
				.slots(Icon.BORDER, 6, 7, 8, 15, 17, 24, 26, 33, 35, 42, 43, 44, 45, 46, 47, 48, 49, 50);

		Icon none = Icon.from(
				ItemStack.builder()
						.itemType(ItemTypes.BARRIER)
						.add(Keys.DISPLAY_NAME, MessageUtils.fetchMsg(player, MsgConfigKeys.PARTY_NO_SELECTION))
						.build()
		);
		builder = builder.slot(none, 16);

		Icon back = Icon.from(ItemStack.builder().itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:eject_button").get()).add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Go Back")).build());
		back.addListener(clickable -> {
			this.display.close(player);
			new PartyUI(clickable.getPlayer(), this.ranch, this.pen, penID, slot).open(player);
		});
		builder = builder.slot(back, 34);

		return builder.build();
	}

	private List<Icon> forgePCIcons(Player player) {
		List<Icon> icons = Lists.newArrayList();
		int index = 0;
		for(PCRepresentable pcr : this.getBoxContents(player)) {
			if(pcr.get().isPresent()) {
				com.pixelmonmod.pixelmon.api.pokemon.Pokemon pokemon = pcr.get().get();
				Icon icon = StandardIcons.getPicture(player, new Pokemon(pokemon), DaycarePlugin.getInstance().getMsgConfig().get(MsgConfigKeys.POKEMON_LORE_SELECT));

				if(!pokemon.isEgg()) {
					final int i = index;
					icon.addListener(clickable -> {
						if(this.UNBREEDABLE.matches(pokemon)) {
							player.sendMessage(Text.of(DaycarePlugin.getInstance().getPluginInfo().error(), TextColors.GRAY, "That pokemon is marked as unbreedable..."));
							return;
						}

						PCStorage stor = Pixelmon.storageManager.getPCForPlayer(player.getUniqueId());
						this.selection = new Selection(pokemon, pcr.pos);

						Icon confirm = Icon.from(
								ItemStack.builder()
										.itemType(ItemTypes.DYE)
										.add(Keys.DYE_COLOR, DyeColors.LIME)
										.add(Keys.DISPLAY_NAME, MessageUtils.fetchMsg(player, MsgConfigKeys.PARTY_CONFIRM))
										.build()
						);
						confirm.addListener(clickable1 -> {
							stor.getBox(i / 30).set(pcr.pos, null);
							Pokemon rep = new Pokemon(this.selection.getPokemon());
							if (this.slot == 1) {
								this.pen.setSlot1(rep);
							} else {
								this.pen.setSlot2(rep);
							}
							if(this.pen.isFull()) {
								this.pen.initialize(ranch.getOwnerUUID());
							}
							clickable.getPlayer().closeInventory();
							new PenUI(player, this.ranch, this.pen, penID).open(player);
						});
						this.display.apply(confirm, 16);
					});
				}
				icons.add(icon);
			} else {
				icons.add(Icon.EMPTY);
			}

			++index;
		}

		return icons;
	}

	private List<PCRepresentable> getBoxContents(Player player) {
		List<PCRepresentable> contents = Lists.newArrayList();
		PCStorage storage = Pixelmon.storageManager.getPCForPlayer(player.getUniqueId());
		for(PCBox box : storage.getBoxes()) {
			contents.addAll(getPokemonInBox(box));
		}

		return contents;
	}

	private List<PCRepresentable> getPokemonInBox(PCBox box){
		List<PCRepresentable> pcContents = new ArrayList<>();
		for(int i = 0; i < 30; i++){
			pcContents.add(new PCRepresentable(box.get(i), i));
		}

		return pcContents;
	}

	@Override
	public Page getDisplay() {
		return this.display;
	}

	@RequiredArgsConstructor
	private class PCRepresentable {

		private final com.pixelmonmod.pixelmon.api.pokemon.Pokemon pokemon;

		/** Represents the actual slot position of the pokemon in the box currently being viewed */
		@Getter private final int pos;

		public Optional<com.pixelmonmod.pixelmon.api.pokemon.Pokemon> get() {
			return Optional.ofNullable(pokemon);
		}
	}
}
