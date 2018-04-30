package com.nickimpact.daycare.ui.storage;

import com.google.common.collect.Maps;
import com.nickimpact.daycare.DaycarePlugin;
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
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.ComputerBox;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerComputerStorage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class PcUI implements Displayable {

	private Ranch ranch;
	private Pen pen;
	private int slot;
	private UI display;

	/** The player's PC at time of use */
	private Map<Integer, List<PCRepresentable>> pc;

	/** The current page of the PC */
	private int page;

	private Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
	private final String PAGE_TOKEN = "page";

	private final int MAX_PAGES = PixelmonConfig.computerBoxes;

	private Selection selection;

	public PcUI(Player player, Ranch ranch, Pen pen, int penID, int slot) {
		this.ranch = ranch;
		this.pen = pen;
		this.slot = slot;
		this.pc = getBoxContents(player);
		this.page = 1;

		this.display = UI.builder()
				.property(InventoryDimension.of(9, 6))
				.title(TextSerializers.FORMATTING_CODE.deserialize(DaycarePlugin.getInstance().getMsgConfig().get(MsgConfigKeys.PC_TITLE)))
				.build(player, DaycarePlugin.getInstance())
				.define(setupDisplay(player, penID, slot));
	}

	@Override
	public UI getDisplay() {
		return this.display;
	}

	private Layout setupDisplay(Player player, int penID, int slot) {
		Layout.Builder builder = Layout.builder()
				.slots(Icon.BORDER, 6, 7, 8, 15, 17, 24, 26, 33, 35, 42, 43, 44, 45, 46, 47, 48, 49, 50);

		Icon none = Icon.from(
				ItemStack.builder()
						.itemType(ItemTypes.BARRIER)
						.add(Keys.DISPLAY_NAME, MessageUtils.fetchMsg(player, MsgConfigKeys.PARTY_NO_SELECTION))
						.build()
		);
		builder = builder.slot(none, 16);

		Icon back = Icon.from(ItemStack.builder().itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:eject_button").get()).add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Go Back ")).build());
		back.addListener(clickable -> {
			this.close();
			new PartyUI(clickable.getPlayer(), this.ranch, this.pen, penID, slot).open();
		});
		builder = builder.slot(back, 34);

		// We are going to simply just draw the first page here
		builder = this.drawPage(builder, player, 1, penID);

		tokens.put(PAGE_TOKEN, src -> Optional.of(Text.of(this.page)));
		Icon curr = Icon.from(
				ItemStack.builder()
						.itemType(ItemTypes.PAPER)
						.add(Keys.DISPLAY_NAME, MessageUtils.fetchAndParseMsg(player, MsgConfigKeys.PC_CURR, tokens, null))
						.build()
		);
		builder = builder.slot(curr, 52);

		Icon last = Icon.from(
				ItemStack.builder()
						.itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:trade_holder_left").orElse(ItemTypes.BARRIER))
						.add(Keys.DISPLAY_NAME, MessageUtils.fetchMsg(player, MsgConfigKeys.PC_LEFT))
						.build()
		);
		builder = builder.slot(last, 51);

		Icon next = Icon.from(
				ItemStack.builder()
						.itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:trade_holder_right").orElse(ItemTypes.BARRIER))
						.add(Keys.DISPLAY_NAME, MessageUtils.fetchMsg(player, MsgConfigKeys.PC_RIGHT))
						.build()
		);
		builder = builder.slot(next, 53);

		final Layout.Builder copy = builder;
		last.addListener(clickable -> {
			Layout.Builder copy2 = copy;
			if(this.page == 1) {
				this.page = MAX_PAGES;
			} else {
				this.page -= 1;
			}

			this.tokens.put(PAGE_TOKEN, src -> Optional.of(Text.of(this.page)));
			copy2 = this.drawPage(copy2, player, this.page, penID);
			Icon current = Icon.from(
					ItemStack.builder()
							.itemType(ItemTypes.PAPER)
							.add(Keys.DISPLAY_NAME, MessageUtils.fetchAndParseMsg(player, MsgConfigKeys.PC_CURR, tokens, null))
							.build()
			);
			copy2 = copy2.slot(current, 52);
			this.display.define(copy2.build());
		});
		next.addListener(clickable -> {
			Layout.Builder copy2 = copy;
			if(this.page == MAX_PAGES) {
				this.page = 1;
			} else {
				this.page += 1;
			}

			this.tokens.put(PAGE_TOKEN, src -> Optional.of(Text.of(this.page)));
			copy2 = this.drawPage(copy2, player, this.page, penID);
			Icon current = Icon.from(
					ItemStack.builder()
							.itemType(ItemTypes.PAPER)
							.add(Keys.DISPLAY_NAME, MessageUtils.fetchAndParseMsg(player, MsgConfigKeys.PC_CURR, tokens, null))
							.build()
			);
			copy2 = copy2.slot(current, 52);
			this.display.define(copy2.build());
		});

		return builder.build();
	}

	private Layout.Builder drawPage(Layout.Builder builder, Player player, int box, int penID) {
		for(int y = 0; y < 5; y++) {
			for(int x = 0; x < 6; x++) {
				Optional<EntityPixelmon> optPoke = this.get(player, box, x + (6 * y));
				if(optPoke.isPresent()) {
					EntityPixelmon pokemon = optPoke.get();
					Icon icon = StandardIcons.getPicture(player, new Pokemon(pokemon), DaycarePlugin.getInstance().getMsgConfig().get(MsgConfigKeys.POKEMON_LORE_SELECT));

					final int X = x;
					final int Y = y;
					icon.addListener(clickable -> {
						PlayerComputerStorage stor = PixelmonStorage.computerManager.getPlayerStorage((EntityPlayerMP) player);
						this.selection = new Selection(pokemon, X + (6 * Y));

						Icon confirm = Icon.from(
								ItemStack.builder()
										.itemType(ItemTypes.DYE)
										.add(Keys.DYE_COLOR, DyeColors.LIME)
										.add(Keys.DISPLAY_NAME, MessageUtils.fetchMsg(player, MsgConfigKeys.PARTY_CONFIRM))
										.build()
						);
						confirm.addListener(clickable1 -> {
							stor.getBox(box).changePokemon(X + (6 * Y), null);
							Pokemon rep = new Pokemon(this.selection.getPokemon());
							if(this.slot == 1) {
								this.pen.setSlot1(rep);
							} else {
								this.pen.setSlot2(rep);
							}
							clickable.getPlayer().closeInventory();
							new PenUI(display.getPlayer(), this.ranch, this.pen, penID).open();
						});
						this.display.setSlot(16, confirm);
					});
					builder.slot(icon, x + (9 * y));
				}
			}
		}

		return builder;
	}

	private Map<Integer, List<PCRepresentable>> getBoxContents(Player player) {
		Map<Integer, List<PCRepresentable>> contents = Maps.newTreeMap();
		PlayerComputerStorage storage = PixelmonStorage.computerManager.getPlayerStorage((EntityPlayerMP) player);
		for(ComputerBox box : storage.getBoxList()) {
			contents.put(box.position, getPokemonInBox(box));
		}

		return contents;
	}

	private List<PCRepresentable> getPokemonInBox(ComputerBox box){
		List<PCRepresentable> pcContents = new ArrayList<>();
		for(int i = 0; i < 30; i++){
			pcContents.add(new PCRepresentable(box.getNBTByPosition(i), i));
		}

		return pcContents;
	}

	private Optional<EntityPixelmon> get(Player player, int box, int pos){
		List<PCRepresentable> page = this.pc.get(box - 1);
		return page.get(pos).get().map(nbt -> (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, (World)player.getWorld()));
	}

	@RequiredArgsConstructor
	private class PCRepresentable {

		private final NBTTagCompound nbt;

		/** Represents the actual slot position of the pokemon in the box currently being viewed */
		@Getter private final int pos;

		public Optional<NBTTagCompound> get() {
			return Optional.ofNullable(nbt);
		}
	}
}
