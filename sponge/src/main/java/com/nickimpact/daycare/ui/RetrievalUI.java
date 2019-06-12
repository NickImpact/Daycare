package com.nickimpact.daycare.ui;

import com.google.common.collect.Maps;
import com.nickimpact.daycare.SpongeDaycarePlugin;
import com.nickimpact.daycare.api.configuration.ConfigKeys;
import com.nickimpact.daycare.configuration.MsgConfigKeys;
import com.nickimpact.daycare.implementation.SpongeDaycarePokemonWrapper;
import com.nickimpact.daycare.implementation.SpongePen;
import com.nickimpact.daycare.implementation.SpongeRanch;
import com.nickimpact.daycare.text.TextParsingUtils;
import com.nickimpact.daycare.ui.common.CommonUIComponents;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.sponge.ui.SpongeIcon;
import com.nickimpact.impactor.sponge.ui.SpongeLayout;
import com.nickimpact.impactor.sponge.ui.SpongeUI;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class RetrievalUI {

    private SpongeUI display;
    private Player viewer;
    private SpongeDaycarePokemonWrapper pokemon;

    private SpongeRanch ranch;
    private SpongePen pen;
    private int slot;

    public RetrievalUI(Player viewer, SpongeDaycarePokemonWrapper pokemon, SpongeRanch ranch, SpongePen pen, int slot) {
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

        TextParsingUtils parser = SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils();

        Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
        tokens.put("daycare_price", src -> {
            Config config = SpongeDaycarePlugin.getSpongeInstance().getConfig();
            double amount = config.get(ConfigKeys.PRICE_PER_LVL) * pokemon.getGainedLevels();
            return Optional.of(Text.of(SpongeDaycarePlugin.getSpongeInstance().getEconomy().getDefaultCurrency().format(new BigDecimal(amount))));
        });


        SpongeIcon pokemon = new SpongeIcon(CommonUIComponents.pokemonDisplay(this.pokemon.getDelegate()));
        pokemon.getDisplay().offer(Keys.DISPLAY_NAME, parser.fetchAndParseMsg(this.viewer, MsgConfigKeys.POKEMON_TITLE_PEN, tokens, variables));

        return CommonUIComponents.confirmBase(
                pokemon,
                new CommonUIComponents.CommonConfirmComponent(
                        SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsgs(this.viewer, MsgConfigKeys.CONFIRM_RETRIEVAL_POKEMON, tokens, variables),
                        (player, event) -> {
                            this.display.close(player);
                            PlayerPartyStorage party = Pixelmon.storageManager.getParty(player.getUniqueId());
                            this.pokemon.getDelegate().setLevel(this.pokemon.getDelegate().getLevel() + this.pokemon.getGainedLevels());
                            party.add(this.pokemon.getDelegate());
                            this.pen.takeFromSlot(this.slot);
                            SpongeDaycarePlugin.getSpongeInstance().getService().getStorage().updateRanch(this.ranch);
                            player.sendMessages(parser.fetchAndParseMsgs(player, MsgConfigKeys.SELECT_RETRIEVE, tokens, variables));
                        }
                ),
                (player, event) -> new PenUI(player, this.ranch, this.pen).open()
        );
    }
}
