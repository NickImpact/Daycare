package com.nickimpact.daycare.spigot.ui;

import com.google.common.collect.Lists;
import com.nickimpact.daycare.spigot.SpigotDaycarePlugin;
import com.nickimpact.daycare.api.configuration.ConfigKeys;
import com.nickimpact.daycare.spigot.implementation.SpigotDaycarePokemonWrapper;
import com.nickimpact.daycare.spigot.implementation.SpigotPen;
import com.nickimpact.daycare.spigot.implementation.SpigotRanch;
import com.nickimpact.daycare.spigot.utils.ItemStackUtils;
import com.nickimpact.daycare.spigot.utils.MessageUtils;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.spigot.ui.SpigotIcon;
import com.nickimpact.impactor.spigot.ui.SpigotLayout;
import com.nickimpact.impactor.spigot.ui.SpigotUI;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

public class RetrievalUI {

	private SpigotUI display;
	private Player viewer;
	private SpigotDaycarePokemonWrapper pokemon;

	private SpigotRanch ranch;
	private SpigotPen pen;
	private int slot;

	public RetrievalUI(Player viewer, SpigotDaycarePokemonWrapper pokemon, SpigotRanch ranch, SpigotPen pen, int slot) {
		this.viewer = viewer;
		this.pokemon = pokemon;
		this.display = this.createDisplay();
		this.display.define(this.layout());
		this.ranch = ranch;
		this.pen = pen;
		this.slot = slot;
	}

	public void open() {
		this.display.open(this.viewer);
	}

	private SpigotUI createDisplay() {
		return SpigotUI.builder()
				.title(String.format("&cDaycare &7\u00bb &3%s", pokemon.getDelegate().getSpecies().getPokemonName()))
				.size(54)
				.build();
	}

	private SpigotLayout layout() {
		Config config = SpigotDaycarePlugin.getInstance().getConfiguration();
		double price = config.get(ConfigKeys.PRICE_PER_LVL) + config.get(ConfigKeys.PRICE_PER_LVL) * pokemon.getGainedLevels();

		SpigotDaycarePlugin.getInstance().getEconomy().format(price);

		SpigotIcon pokemon = new SpigotIcon(
				ItemStackUtils.itemBuilder()
						.fromItem(CommonUIComponents.pokemonDisplay(this.pokemon.getDelegate()))
						.name(String.format(
								"&3%s %s&7| &bLvl %d",
								this.pokemon.getDelegate().getSpecies().getPokemonName(),
								this.pokemon.getDelegate().isShiny() ? "&7(&6Shiny&7) " : "",
								this.pokemon.getDelegate().getLevel() + this.pokemon.getGainedLevels()
						)).build()
		);

		return CommonUIComponents.confirmBase(
				pokemon,
				new CommonUIComponents.CommonConfirmComponent(
						Lists.newArrayList(
								"&7By clicking here, you agree to",
								String.format("&7pay the price of &e%s", SpigotDaycarePlugin.getInstance().getEconomy().format(price)),
								String.format("&7in order to retrieve your &a%s", this.pokemon.getDelegate().getSpecies().getPokemonName())
						).stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).collect(Collectors.toList()),
						(player, event) -> {
							this.display.close(player);

							if (this.pen.getAtPosition(this.slot).isPresent()) {
								Economy economy = SpigotDaycarePlugin.getInstance().getEconomy();
								if(economy.withdrawPlayer(player, price).type == EconomyResponse.ResponseType.SUCCESS) {
									PlayerPartyStorage party = Pixelmon.storageManager.getParty(player.getUniqueId());
									Pokemon target = this.pokemon.getDelegate();
									target.setLevel(this.pokemon.getDelegate().getLevel() + this.pokemon.getGainedLevels());
									party.add(target);
									this.pen.takeFromSlot(this.slot);
									SpigotDaycarePlugin.getInstance().getService().getStorage().updateRanch(this.ranch);
									player.sendMessage(MessageUtils.parse(String.format("&7You've retrieved your &e%s &7for &a%s&7!", this.pokemon.getDelegate().getSpecies().getPokemonName(), SpigotDaycarePlugin.getInstance().getEconomy().format(price)), true, false));
								} else {
									player.sendMessage(MessageUtils.parse("&cInsufficient funds...", true, true));
								}
							}
						}
				),
				(player, event) -> PenUI.builder().viewer(player).ranch(this.ranch).pen(this.pen).build().open()
		);
	}

}
