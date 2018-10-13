package com.nickimpact.daycare.ui;

import com.google.common.collect.Maps;
import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.configuration.ConfigKeys;
import com.nickimpact.daycare.configuration.MsgConfigKeys;
import com.nickimpact.daycare.ranch.Pen;
import com.nickimpact.daycare.ranch.Pokemon;
import com.nickimpact.daycare.ranch.Ranch;
import com.nickimpact.daycare.utils.MessageUtils;
import com.nickimpact.impactor.gui.v2.Displayable;
import com.nickimpact.impactor.gui.v2.Icon;
import com.nickimpact.impactor.gui.v2.Layout;
import com.nickimpact.impactor.gui.v2.UI;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * (Some note will appear here)
 *
 * @author NickImpact (Nick DeGruccio)
 */
public class SelectionUI implements Displayable {

	/** The ranch being focused on */
	private Ranch ranch;

	/** The pen to return to, if the back button is selected */
	private Pen pen;

	/** The pokemon of focus in this UI */
	private Pokemon pokemon;
	private int slot;

	private UI display;

	public SelectionUI(Player player, Ranch ranch, Pen pen, int id, Pokemon pokemon, int slot) {
		this.ranch = ranch;
		this.pen = pen;
		this.pokemon = pokemon;
		this.slot = slot;

		Map<String, Object> variables = Maps.newHashMap();
		variables.put("dummy", pokemon.getPokemon());
		variables.put("dummy2", pokemon);

		display = UI.builder()
				.title(MessageUtils.fetchAndParseMsg(player, MsgConfigKeys.SELECT_TITLE, null, variables))
				.build(DaycarePlugin.getInstance())
				.define(setupDisplay(player, id));
	}

	@Override
	public UI getDisplay() {
		return this.display;
	}

	private Layout setupDisplay(Player player, int penID) {
		Layout.Builder lb = Layout.builder().dimension(InventoryDimension.of(9, 3)).border();
		lb.slot(StandardIcons.getPicture(player,  pokemon, DaycarePlugin.getInstance().getMsgConfig().get(MsgConfigKeys.POKEMON_LORE_PEN)), 10);
		lb.slot(Icon.BORDER, 11);

		Icon back = Icon.from(ItemStack.builder().itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:eject_button").get()).add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Go Back ")).build());
		back.addListener(clickable -> {
			this.close(player);
			new PenUI(clickable.getPlayer(), this.ranch, this.pen, penID).open(player);
		});
		lb.slot(back, 13);

		// Store pokemon name in the case of an evolution when trying to retrieve
		final EnumPokemon species = pokemon.getPokemon().getSpecies();

		Map<String, Object> variables = Maps.newHashMap();
		variables.put("dummy", pokemon.getPokemon());
		variables.put("dummy2", pokemon);
		variables.put("pokemon_old", species.name);
		BigDecimal price = calcPrice();
		Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
		tokens.put("price", src -> Optional.of(DaycarePlugin.getInstance().getEconomy().getDefaultCurrency().format(price)));

		Icon retrieve = Icon.from(ItemStack.builder()
				.itemType(ItemTypes.DYE)
				.add(Keys.DYE_COLOR, DyeColors.LIME)
				.add(Keys.DISPLAY_NAME, MessageUtils.fetchAndParseMsg(player, MsgConfigKeys.SELECT_RETRIEVE_TITLE, null, variables))
				.add(Keys.ITEM_LORE, MessageUtils.fetchAndParseMsgs(player, MsgConfigKeys.SELECT_RETRIEVE_LORE, tokens, variables))
				.build()
		);
		retrieve.addListener(clickable -> {
			final EntityPixelmon poke = this.pokemon.getPokemon();
			final BigDecimal confirmPrice = calcPrice();
			if(confirmPrice.compareTo(price) > 0) {
				tokens.put("price", src -> Optional.of(DaycarePlugin.getInstance().getEconomy().getDefaultCurrency().format(price)));
				clickable.getPlayer().sendMessages(MessageUtils.fetchAndParseMsgs(player, MsgConfigKeys.SELECT_RETRIEVE_PRICE_CHANGE, tokens, variables));
				return;
			}

			Optional<UniqueAccount> optAcc = DaycarePlugin.getInstance().getEconomy().getOrCreateAccount(clickable.getPlayer().getUniqueId());
			optAcc.ifPresent(acc -> {
				if(acc.getBalance(DaycarePlugin.getInstance().getEconomy().getDefaultCurrency()).compareTo(confirmPrice) < 0) {
					player.sendMessage(MessageUtils.fetchAndParseMsg(clickable.getPlayer(), MsgConfigKeys.RANCH_UI_PEN_INSUFFICIENT_FUNDS, null, null));
					return;
				}

				acc.withdraw(
						DaycarePlugin.getInstance().getEconomy().getDefaultCurrency(),
						confirmPrice,
						Sponge.getCauseStackManager().getCurrentCause()
				);
				Optional<PlayerStorage> optStor = PixelmonStorage.pokeBallManager.getPlayerStorage((EntityPlayerMP) clickable.getPlayer());
				optStor.ifPresent(stor -> {
					poke.getLvl().setLevel(Math.min(100, poke.getLvl().getLevel() + pokemon.getGainedLvls()));
					stor.addToParty(poke);
				});

				if(species != poke.getSpecies()) {
					variables.put("dummy", pokemon.getPokemon());
					variables.put("dummy2", pokemon);
					clickable.getPlayer().sendMessages(MessageUtils.fetchAndParseMsgs(player, MsgConfigKeys.SELECT_RETRIEVE_EVOLVED, tokens, variables));
				}
				clickable.getPlayer().sendMessages(MessageUtils.fetchAndParseMsgs(player, MsgConfigKeys.SELECT_RETRIEVE, tokens, variables));
				this.close(player);
				if(this.slot == 1) {
					this.pen.setSlot1(null);
				} else {
					this.pen.setSlot2(null);
				}
				if(this.pen.getInstance() != null) {
					this.pen.halt();
					this.pen.setInstance(null);
				}
				new PenUI(clickable.getPlayer(), this.ranch, this.pen, penID).open(player);
			});
		});
		lb.slot(retrieve, 15);

		return lb.build();
	}

	private BigDecimal calcPrice() {
		int levels = this.pokemon.getGainedLvls();
		return new BigDecimal((levels + 1) * DaycarePlugin.getInstance().getConfig().get(ConfigKeys.PRICE_PER_LVL));
	}
}
