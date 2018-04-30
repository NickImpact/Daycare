package com.nickimpact.daycare.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.configuration.ConfigKeys;
import com.nickimpact.daycare.configuration.MsgConfigKeys;
import com.nickimpact.daycare.exceptions.AlreadyUnlockedException;
import com.nickimpact.daycare.ranch.Pen;
import com.nickimpact.daycare.ranch.Ranch;
import com.nickimpact.daycare.utils.MessageUtils;
import com.nickimpact.impactor.gui.v2.Displayable;
import com.nickimpact.impactor.gui.v2.Icon;
import com.nickimpact.impactor.gui.v2.Layout;
import com.nickimpact.impactor.gui.v2.UI;
import com.nickimpact.impactor.items.ItemUtils;
import io.github.nucleuspowered.nucleus.api.exceptions.NucleusException;
import org.mariuszgromada.math.mxparser.Expression;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class RanchUI implements Displayable {

	private Ranch ranch;

	private UI display;

	public RanchUI(Player player) {
		this.ranch = DaycarePlugin.getInstance()
				.getRanches()
				.stream()
				.filter(ranch -> ranch.getOwnerUUID().equals(player.getUniqueId()))
				.findAny().get();

		this.display = UI.builder()
				.title(MessageUtils.fetchAndParseMsg(player, MsgConfigKeys.RANCH_UI_TITLE, null, null))
				.dimension(InventoryDimension.of(9, 6))
				.build(player, DaycarePlugin.getInstance())
				.define(setupDisplay(player));
	}

	@Override
	public UI getDisplay() {
		return this.display;
	}

	private Layout setupDisplay(Player player) {
		Layout.Builder builder = Layout.builder()
				.border()
				.slots(Icon.BORDER, 28, 29, 30, 31, 32, 33, 34);
		builder = drawPens(player, builder);
		builder = playerIcon(player, builder);
		builder = settingsIcon(player, builder);
		builder = statisticsDisplay(player, builder);

		return builder.build();
	}

	private Layout.Builder drawPens(Player player, Layout.Builder builder) {
		int slot = 10;
		int id = 1;
		for(Pen pen : this.ranch.getPens()) {
			if(slot == 17) {
				slot = 19;
			}
			builder = builder.slot(this.drawPen(player, id++, slot), slot);
			slot++;
		}

		return builder;
	}

	private Icon drawPen(Player player, int id, int index) {
		Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
		tokens.put("pen_id", src -> Optional.of(Text.of(id)));
		Icon icon = Icon.from(ItemStack.builder()
				.itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:ranch").orElse(ItemTypes.BARRIER))
				.add(Keys.DISPLAY_NAME, MessageUtils.fetchAndParseMsg(player, MsgConfigKeys.RANCH_UI_PEN_ID, tokens, null))
				.build()
		);

		Pen pen = this.ranch.getPen(id - 1);
		if(pen.isUnlocked()) {
			List<Text> lore = Lists.newArrayList();
			lore.add(this.penInfo(player, pen, 1));
			lore.add(this.penInfo(player, pen, 2));

			if(pen.getEgg().isPresent()) {
				lore.add(MessageUtils.fetchMsg(player, MsgConfigKeys.RANCH_UI_EGG_AVAILABLE));
			}

			icon.getDisplay().offer(Keys.ITEM_LORE, lore);
			icon.addListener(clickable -> {
				Sponge.getScheduler().createTaskBuilder().execute(() -> {
					clickable.getPlayer().closeInventory();
					new PenUI(clickable.getPlayer(), ranch, pen, id).open();
				}).delayTicks(1).submit(DaycarePlugin.getInstance());
			});
		} else {
			org.mariuszgromada.math.mxparser.Function function = new org.mariuszgromada.math.mxparser.Function("P(b, i, p) = " + DaycarePlugin.getInstance().getConfig().get(ConfigKeys.PEN_PRICE_EQUATION));
			Expression expression = new Expression(String.format(
					"P(%.2f, %.2f, %d)",
					DaycarePlugin.getInstance().getConfig().get(ConfigKeys.BASE_PEN_PRICE),
					DaycarePlugin.getInstance().getConfig().get(ConfigKeys.INCREMENT_PEN_PRICE),
					id - 1
			), function);

			BigDecimal price = new BigDecimal(expression.calculate());
			tokens.put("price", src -> Optional.of(DaycarePlugin.getInstance().getEconomy().getDefaultCurrency().format(price)));
			icon.getDisplay().offer(Keys.ITEM_LORE, MessageUtils.fetchAndParseMsgs(player, MsgConfigKeys.RANCH_UI_PEN_LOCKED, tokens, null));
			icon.addListener(clickable -> {
				try {
					if (!ranch.unlock(id - 1)) {
						clickable.getPlayer().sendMessage(MessageUtils.fetchAndParseMsg(clickable.getPlayer(), MsgConfigKeys.RANCH_UI_PEN_INSUFFICIENT_FUNDS, null, null));
					} else {
						this.display.setSlot(index, this.drawPen(player, id, index));
						tokens.put("pen", src -> Optional.of(Text.of(id)));
						tokens.put("price", src -> Optional.of(DaycarePlugin.getInstance().getEconomy().getDefaultCurrency().format(price)));
						clickable.getPlayer().sendMessages(
								MessageUtils.fetchAndParseMsg(
										clickable.getPlayer(),
										MsgConfigKeys.UNLOCK_PEN,
										tokens,
										null
								)
						);
					}
				} catch (AlreadyUnlockedException e) {
					clickable.getPlayer().sendMessages(Text.of("Unable to open pen due to an error..."));
				}
			});
		}

		return icon;
	}

	private Text penInfo(Player player, Pen pen, int id) {
		Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
		tokens.put("slot_id", src -> Optional.of(Text.of(id)));
		return pen.getAtPosition(id).map(pokemon -> {
					Map<String, Object> variables = Maps.newHashMap();
					variables.put("dummy", pokemon.getPokemon());
					variables.put("dummy2", pokemon);
					return MessageUtils.fetchAndParseMsg(player, MsgConfigKeys.RANCH_UI_PEN_INFO, tokens, variables);
				}).orElse(MessageUtils.fetchAndParseMsg(player, MsgConfigKeys.RANCH_UI_PEN_EMPTY, tokens, null));
	}

	private Layout.Builder playerIcon(Player player, Layout.Builder builder) {
		ItemStack skull = ItemUtils.createSkull(player.getUniqueId(), MessageUtils.fetchAndParseMsg(player, MsgConfigKeys.RANCH_UI_PLAYER_INFO, null, null), Lists.newArrayList());
		return builder.slot(Icon.from(skull), 38);
	}

	private Layout.Builder settingsIcon(Player player, Layout.Builder builder) {
		Icon icon = Icon.from(ItemStack.builder()
				.itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:diamond_hammer").get())
				.add(Keys.DISPLAY_NAME, MessageUtils.fetchAndParseMsg(player, MsgConfigKeys.RANCH_UI_SETTINGS, null, null))
				.add(Keys.HIDE_ATTRIBUTES, true)
				.add(Keys.HIDE_MISCELLANEOUS, true)
				.build());
		icon.addListener(clickable -> {
			this.close();
			new SettingsUI(player, this.ranch).open();
		});
		return builder.slot(icon, 40);
	}

	private Layout.Builder statisticsDisplay(Player player, Layout.Builder builder) {
		ItemStack statistics;
		try {
			statistics = ItemStack.builder()
					.itemType(ItemTypes.WRITTEN_BOOK)
					.add(Keys.DISPLAY_NAME, MessageUtils.fetchAndParseMsg(player, MsgConfigKeys.RANCH_UI_STATS, null, null))
					.add(
							Keys.ITEM_LORE,
							DaycarePlugin.getInstance().getTextParsingUtils().parse(
									DaycarePlugin.getInstance().getMsgConfig().get(MsgConfigKeys.STATISTICS),
									player,
									null,
									null
							)
					)
					.add(Keys.HIDE_ATTRIBUTES, true)
					.add(Keys.HIDE_MISCELLANEOUS, true)
					.build();
		} catch (NucleusException e) {
			statistics = ItemStack.builder().itemType(ItemTypes.BARRIER).add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "ERROR")).build();
		}

		return builder.slot(Icon.from(statistics), 42);
	}
}
