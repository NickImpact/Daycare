package com.nickimpact.daycare.reforged.ui;

import com.google.common.collect.Maps;
import com.nickimpact.daycare.api.configuration.ConfigKeys;
import com.nickimpact.daycare.reforged.implementation.ReforgedDaycarePokemonWrapper;
import com.nickimpact.daycare.reforged.implementation.ReforgedPen;
import com.nickimpact.daycare.sponge.SpongeDaycarePlugin;
import com.nickimpact.daycare.sponge.configuration.MsgConfigKeys;
import com.nickimpact.daycare.sponge.implementation.SpongeRanch;
import com.nickimpact.daycare.sponge.text.TextParsingUtils;
import com.nickimpact.daycare.sponge.ui.common.CommonUIComponents;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.sponge.ui.SpongeIcon;
import com.nickimpact.impactor.sponge.ui.SpongeLayout;
import com.nickimpact.impactor.sponge.ui.SpongeUI;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class RetrievalUI {

    private SpongeUI display;
    private Player viewer;
    private ReforgedDaycarePokemonWrapper pokemon;

    private SpongeRanch ranch;
    private ReforgedPen pen;
    private int slot;

    public RetrievalUI(Player viewer, ReforgedDaycarePokemonWrapper pokemon, SpongeRanch ranch, ReforgedPen pen, int slot) {
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

    private SpongeUI createDisplay() {
        Map<String, Object> variables = Maps.newHashMap();
        variables.put("poke", pokemon.getDelegate());

        return SpongeUI.builder()
                .title(SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsg(
                        this.viewer,
                        MsgConfigKeys.SELECT_TITLE,
                        null, variables
                ))
                .dimension(InventoryDimension.of(9, 6))
                .build();
    }

    private SpongeLayout layout() {
        Map<String, Object> variables = Maps.newHashMap();
        variables.put("poke", this.pokemon.getDelegate());
        variables.put("wrapper", this.pokemon);

        Config config = SpongeDaycarePlugin.getSpongeInstance().getConfig();
        BigDecimal price = new BigDecimal(config.get(ConfigKeys.PRICE_PER_LVL) + config.get(ConfigKeys.PRICE_PER_LVL) * pokemon.getGainedLevels());

        TextParsingUtils parser = SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils();

        Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
        tokens.put("daycare_price", src -> Optional.of(Text.of(SpongeDaycarePlugin.getSpongeInstance().getEconomy().getDefaultCurrency().format(price))));

        SpongeIcon pokemon = new SpongeIcon(CommonUIComponents.pokemonDisplay(this.pokemon.getDelegate()));
        pokemon.getDisplay().offer(Keys.DISPLAY_NAME, parser.fetchAndParseMsg(this.viewer, MsgConfigKeys.POKEMON_TITLE_PEN, tokens, variables));

        return CommonUIComponents.confirmBase(
                pokemon,
                new CommonUIComponents.CommonConfirmComponent(
                        SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsgs(this.viewer, MsgConfigKeys.CONFIRM_RETRIEVAL_POKEMON, tokens, variables),
                        (player, event) -> {
                            this.display.close(player);

                            if (this.pen.getAtPosition(this.slot).isPresent()) {
                                EconomyService economy = SpongeDaycarePlugin.getSpongeInstance().getEconomy();
                                economy.getOrCreateAccount(player.getUniqueId()).ifPresent(account -> {
                                    if (account.withdraw(economy.getDefaultCurrency(), price, Sponge.getCauseStackManager().getCurrentCause()).getResult() == ResultType.SUCCESS) {
                                        PlayerPartyStorage party = Pixelmon.storageManager.getParty(player.getUniqueId());
                                        Pokemon target = this.pokemon.getDelegate();
                                        target.setLevel(this.pokemon.getDelegate().getLevel() + this.pokemon.getGainedLevels());
                                        party.add(target);
                                        this.pen.takeFromSlot(this.slot);
                                        SpongeDaycarePlugin.getSpongeInstance().getService().getStorage().updateRanch(this.ranch);
                                        player.sendMessages(parser.fetchAndParseMsgs(player, MsgConfigKeys.SELECT_RETRIEVE, tokens, variables));
                                    } else {
                                        player.sendMessages(parser.fetchAndParseMsg(player, MsgConfigKeys.RANCH_UI_PEN_INSUFFICIENT_FUNDS, tokens, variables));
                                    }
                                });
                            }
                        }
                ),
                (player, event) -> new ReforgedPenUI(player, this.ranch, this.pen).open()
        );
    }
}
