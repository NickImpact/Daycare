package com.nickimpact.daycare.reforged.ui;

import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import com.nickimpact.daycare.api.configuration.ConfigKeys;
import com.nickimpact.daycare.api.events.DaycareEvent;
import com.nickimpact.daycare.reforged.implementation.ReforgedDaycarePokemonWrapper;
import com.nickimpact.daycare.reforged.implementation.ReforgedPen;
import com.nickimpact.daycare.sponge.SpongeDaycarePlugin;
import com.nickimpact.daycare.sponge.configuration.MsgConfigKeys;
import com.nickimpact.daycare.sponge.implementation.SpongeRanch;
import com.nickimpact.daycare.sponge.ui.common.CommonUIComponents;
import com.nickimpact.daycare.sponge.utils.TextParser;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.sponge.ui.SpongeIcon;
import net.impactdev.impactor.sponge.ui.SpongeLayout;
import net.impactdev.impactor.sponge.ui.SpongeUI;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Supplier;

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
        List<Supplier<Object>> sources = Lists.newArrayList();
        sources.add(() -> pokemon.getDelegate());

        return SpongeUI.builder()
                .title(TextParser.parse(TextParser.read(MsgConfigKeys.SELECT_TITLE), sources))
                .dimension(InventoryDimension.of(9, 6))
                .build();
    }

    private SpongeLayout layout() {
        List<Supplier<Object>> sources = Lists.newArrayList();
        sources.add(() -> pokemon.getDelegate());
        sources.add(() -> pokemon);

        Config config = SpongeDaycarePlugin.getSpongeInstance().getConfig();
        BigDecimal price = BigDecimal.valueOf(config.get(ConfigKeys.PRICE_PER_LVL) + config.get(ConfigKeys.PRICE_PER_LVL) * pokemon.getGainedLevels());
        sources.add(() -> Text.of(SpongeDaycarePlugin.getSpongeInstance().getEconomy().getDefaultCurrency().format(price)));

        SpongeIcon pokemon = new SpongeIcon(CommonUIComponents.pokemonDisplay(this.pokemon.getDelegate()));
        pokemon.getDisplay().offer(Keys.DISPLAY_NAME, TextParser.parse(TextParser.read(MsgConfigKeys.POKEMON_TITLE_PEN), sources));

        return CommonUIComponents.confirmBase(
                pokemon,
                new CommonUIComponents.CommonConfirmComponent(
                        TextParser.parse(TextParser.read(MsgConfigKeys.CONFIRM_RETRIEVAL_POKEMON), sources),
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

                                        Impactor.getInstance().getEventBus().post(
                                                DaycareEvent.RemovePokemon.class,
                                                new TypeToken<Pokemon>(){},
                                                this.viewer.getUniqueId(),
                                                this.pen,
                                                target
                                        );
                                        this.pen.takeFromSlot(this.slot);
                                        SpongeDaycarePlugin.getSpongeInstance().getService().getStorage().updateRanch(this.ranch);
                                        player.sendMessages(TextParser.parse(TextParser.read(MsgConfigKeys.SELECT_RETRIEVE), sources));
                                    } else {
                                        player.sendMessages(TextParser.parse(TextParser.read(MsgConfigKeys.RANCH_UI_PEN_INSUFFICIENT_FUNDS), sources));
                                    }
                                });
                            }
                        }
                ),
                (player, event) -> new ReforgedPenUI(player, this.ranch, this.pen).open()
        );
    }
}
