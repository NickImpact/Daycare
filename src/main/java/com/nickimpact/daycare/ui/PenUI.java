package com.nickimpact.daycare.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.configuration.MsgConfigKeys;
import com.nickimpact.daycare.ranch.Pen;
import com.nickimpact.daycare.ranch.Pokemon;
import com.nickimpact.daycare.ranch.Ranch;
import com.nickimpact.daycare.stats.Statistics;
import com.nickimpact.daycare.ui.storage.PartyUI;
import com.nickimpact.daycare.utils.MessageUtils;
import com.nickimpact.impactor.gui.v2.Displayable;
import com.nickimpact.impactor.gui.v2.Icon;
import com.nickimpact.impactor.gui.v2.Layout;
import com.nickimpact.impactor.gui.v2.UI;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * (Some note will appear here)
 *
 * @author NickImpact (Nick DeGruccio)
 */
public class PenUI implements Displayable {

	private static final ItemStack NO_EGG = ItemStack.builder().itemType(ItemTypes.BARRIER).add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "No Egg Available...")).build();

	/** The ranch being focused on */
	private Ranch ranch;

	/** The focus point for this UI */
	private Pen pen;

	private UI display;

	private Task runner;

	public PenUI(Player player, Ranch ranch, Pen pen, int id) {
		this.ranch = ranch;
		this.pen = pen;

		Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
		tokens.put("pen_id", src -> Optional.of(Text.of(id)));
		this.display = UI.builder()
				.title(MessageUtils.fetchAndParseMsg(player, MsgConfigKeys.PEN_UI_TITLE, tokens, null))
				.dimension(InventoryDimension.of(9, 5))
				.build(DaycarePlugin.getInstance())
				.define(setupDisplay(player, id));

		this.runner = Sponge.getScheduler().createTaskBuilder().execute(() -> {
			this.updateBorder(player);
			if(this.pen.getEgg().isPresent()) {
				if(this.display.getSlot(13).getDisplay().equalTo(NO_EGG)) {
					this.display.setSlot(13, this.eggIcon(player));
				}
			}
		}).interval(1, TimeUnit.SECONDS).submit(DaycarePlugin.getInstance());
		this.display.setCloseAction((e, p) -> runner.cancel());
	}

	@Override
	public UI getDisplay() {
		return this.display;
	}

	private Layout setupDisplay(Player player, int id) {
		Layout.Builder builder = Layout.builder().dimension(InventoryDimension.of(9, 5));

		builder = this.drawBorder(player, builder);
		builder = this.drawPokemon(player, id, builder);
		builder = this.drawEgg(builder, player);

		Icon back = Icon.from(
				ItemStack.builder()
						.itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:eject_button").orElse(ItemTypes.BARRIER))
						.add(Keys.DISPLAY_NAME, MessageUtils.fetchMsg(player, MsgConfigKeys.ITEM_BACK))
						.build()
		);
		back.addListener(clickable -> {
			this.close(player);
			new RanchUI(player).open(player);
		});
		builder.slot(back, 30);

		Icon history = Icon.from(
				ItemStack.builder()
						.itemType(ItemTypes.FILLED_MAP)
						.add(Keys.DISPLAY_NAME, MessageUtils.fetchMsg(MsgConfigKeys.PEN_HISTORY))
						.build()
		);

		SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy");
		Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
		tokens.put("date_unlocked", src -> {
			if(this.pen.getDateUnlocked() == null) {
				return Optional.empty();
			}

			return Optional.of(Text.of(sdf.format(this.pen.getDateUnlocked())));
		});
		tokens.put("total_eggs_produced", src -> Optional.of(Text.of(this.pen.getNumEggsProduced())));
		history.getDisplay().offer(Keys.ITEM_LORE, MessageUtils.fetchAndParseMsgs(player, MsgConfigKeys.PEN_HISTORY_LORE, tokens, null));
		builder.slot(history, 32);

		return builder.build();
	}

	private Layout.Builder drawPokemon(Player player, int penID, Layout.Builder builder) {
		Optional<Pokemon> slot1 = this.pen.getAtPosition(1);
		Optional<Pokemon> slot2 = this.pen.getAtPosition(2);

		Icon s1 = slot1.map(pokemon -> StandardIcons.getPicture(player, pokemon, DaycarePlugin.getInstance().getMsgConfig().get(MsgConfigKeys.POKEMON_LORE_PEN)))
				.orElseGet(() -> Icon.from(ItemStack.builder().itemType(ItemTypes.BARRIER).add(Keys.DISPLAY_NAME, MessageUtils.fetchMsg(MsgConfigKeys.PEN_EMPTY_SLOT)).build()));

		Icon s2 = slot2.map(pokemon -> StandardIcons.getPicture(player, pokemon, DaycarePlugin.getInstance().getMsgConfig().get(MsgConfigKeys.POKEMON_LORE_PEN)))
				.orElseGet(() -> Icon.from(ItemStack.builder().itemType(ItemTypes.BARRIER).add(Keys.DISPLAY_NAME, MessageUtils.fetchMsg(MsgConfigKeys.PEN_EMPTY_SLOT)).build()));

		s1.addListener(clickable -> {
			clickable.getPlayer().closeInventory();
			if(slot1.isPresent()) {
				if(pen.getEgg().isPresent()) {
					player.sendMessage(MessageUtils.fetchMsg(player, MsgConfigKeys.MUST_COLLECT_EGG_FIRST));
					return;
				}
				new SelectionUI(clickable.getPlayer(), ranch, pen, penID, slot1.get(), 1).open(player);
			} else {
				new PartyUI(clickable.getPlayer(), this.ranch, this.pen, penID, 1).open(player);
			}
		});

		s2.addListener(clickable -> {
			clickable.getPlayer().closeInventory();
			if(slot2.isPresent()) {
				if(pen.getEgg().isPresent()) {
					player.sendMessage(MessageUtils.fetchMsg(player, MsgConfigKeys.MUST_COLLECT_EGG_FIRST));
					return;
				}
				new SelectionUI(clickable.getPlayer(), ranch, pen, penID, slot2.get(), 2).open(player);
			} else {
				new PartyUI(clickable.getPlayer(), this.ranch, this.pen, penID, 2).open(player);
			}
		});

		return builder.slot(s1, 11).slot(s2, 15);
	}

	private Layout.Builder drawEgg(Layout.Builder builder, Player player) {
		if(this.pen.isFull()) {
			builder = builder.slot(this.eggIcon(player), 13);
		}
		return builder;
	}

	private Icon eggIcon(Player player) {
		Optional<Pokemon> optEgg = this.pen.getEgg();

		Icon icon = optEgg.map(egg -> StandardIcons.getPicture(player, egg, DaycarePlugin.getInstance().getMsgConfig().get(MsgConfigKeys.POKEMON_LORE_PEN)))
				.orElseGet(() -> Icon.from(ItemStack.builder().itemType(ItemTypes.BARRIER).add(Keys.DISPLAY_NAME, MessageUtils.fetchMsg(player, MsgConfigKeys.PEN_NO_EGG)).build()));

		optEgg.ifPresent(egg -> {
			icon.getDisplay().offer(Keys.ITEM_LORE, MessageUtils.fetchMsgs(player, MsgConfigKeys.PEN_EGG_PRESENT));
			icon.addListener(clickable -> {
				if(clickable.getEvent() instanceof ClickInventoryEvent.Primary) {
					PlayerPartyStorage storage = Pixelmon.storageManager.getParty(player.getUniqueId());
					storage.add(egg.getPokemon());
					this.ranch.getStats().incrementStat(Statistics.Stats.EGGS_COLLECTED);
					clickable.getPlayer().sendMessages(MessageUtils.fetchMsgs(player, MsgConfigKeys.PEN_EGG_CLAIM));
				} else {
					this.ranch.getStats().incrementStat(Statistics.Stats.EGGS_DELETED);
					clickable.getPlayer().sendMessages(MessageUtils.fetchMsgs(player, MsgConfigKeys.PEN_EGG_DISMISSED));
				}
				this.pen.setEgg(null);
				getDisplay().setSlot(13, Icon.from(NO_EGG));
				this.updateBorder(player);
			});
		});
		return icon;
	}

	private void drawBorder(Player player) {
		Icon icon = Icon.from(ItemStack.builder().itemType(ItemTypes.STAINED_GLASS_PANE).add(Keys.DISPLAY_NAME, getTitle(player)).add(Keys.DYE_COLOR, getColoring()).build());
		int i;
		for(i = 0; i < 9; ++i) {
			this.display.setSlot(i, icon);
			this.display.setSlot(this.display.getDimension().getRows() * this.display.getDimension().getColumns() - i - 1, icon);
		}

		for(i = 1; i < this.display.getDimension().getRows() - 1; ++i) {
			this.display.setSlot(i * 9, icon);
			this.display.setSlot((i + 1) * 9 - 1, icon);
		}

		for(i = 19; i < 26; i++) {
			this.display.setSlot(i, icon);
		}
	}

	/**
	 * Initial call for border drawing. This method will apply the changes to the layout builder it is given, rather than
	 * attempt to set the slots directly.
	 *
	 * @param builder The current instance of the layout builder
	 */
	private Layout.Builder drawBorder(Player player, Layout.Builder builder) {
		Icon border = borderIcon(player);
		return builder.border(border).slots(border, 19, 20, 21, 22, 23, 24, 25);
	}

	private Icon borderIcon(Player player) {
		return Icon.from(ItemStack.builder()
				.itemType(ItemTypes.STAINED_GLASS_PANE)
				.add(Keys.DISPLAY_NAME, getTitle(player))
				.add(Keys.DYE_COLOR, getColoring())
				.add(Keys.ITEM_LORE, !this.pen.isFull() ? Lists.newArrayList() : this.pen.getInstance().getDescription())
				.build());
	}

	private void updateBorder(Player player) {
		List<Integer> slots = Lists.newArrayList();
		int i;
		for(i = 0; i < 9; ++i) {
			slots.add(i);
			slots.add(this.display.getDimension().getRows() * this.display.getDimension().getColumns() - i - 1);
		}

		for(i = 1; i < this.display.getDimension().getRows() - 1; ++i) {
			slots.add(i * 9);
			slots.add((i + 1) * 9 - 1);
		}

		slots.addAll(Lists.newArrayList(19, 20, 21, 22, 23, 24, 25));

		Icon border = borderIcon(player);
		for(int slot : slots) {
			this.display.setSlot(slot, border);
		}
	}

	private Text getTitle(Player player) {
		Text title = Text.EMPTY;
		if(pen.isFull()) {
			if(this.pen.canBreed()) {
				title = MessageUtils.fetchMsg(player, MsgConfigKeys.PEN_TITLES_BREEDING);
			} else if(this.pen.getEgg().isPresent()) {
				title = MessageUtils.fetchMsg(player, MsgConfigKeys.PEN_TITLES_EGG_AVAILABLE);
			} else {
				title = MessageUtils.fetchMsg(player, MsgConfigKeys.PEN_TITLES_UNABLE);
			}
		}
		return title;
	}

	private DyeColor getColoring() {
		DyeColor coloring = DyeColors.BLACK;
		if(pen.isFull()) {
			if(this.pen.canBreed()) {
				coloring = DyeColors.GREEN;
			} else if(this.pen.getEgg().isPresent()) {
				coloring = DyeColors.YELLOW;
			} else {
				coloring = DyeColors.RED;
			}
		}
		return coloring;
	}
}
