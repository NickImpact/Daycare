package com.nickimpact.daycare.reforged.pokemon.placeholders;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.nickimpact.daycare.reforged.implementation.ReforgedDaycarePokemonWrapper;
import com.nickimpact.daycare.reforged.utils.Flags;
import com.nickimpact.daycare.sponge.configuration.MsgConfigKeys;
import com.nickimpact.daycare.sponge.placeholders.SourceSpecificPlaceholderParser;
import com.nickimpact.daycare.sponge.utils.TextParser;
import com.pixelmonmod.pixelmon.entities.pixelmon.specs.UnbreedableFlag;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.EVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.IVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.evolution.types.LevelingEvolution;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.placeholder.PlaceholderParser;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class ReforgedPlaceholderManager {

    private static final DecimalFormat PERCENTAGE = new DecimalFormat("#0.##");

    private final List<PlaceholderParser> parsers = Lists.newArrayList();

    public ReforgedPlaceholderManager() {
        this.populate();
    }

    public void register(PlaceholderParser parser) {
        this.parsers.add(parser);
    }

    public ImmutableList<PlaceholderParser> getAllParsers() {
        return ImmutableList.copyOf(this.parsers);
    }

    public void populate() {
        this.register(new PokemonPlaceholder(
                "pokemon",
                "The name of a Pokemon",
                pokemon -> Text.of(pokemon.getSpecies().getLocalizedName())
        ));
        this.register(new PokemonPlaceholder(
                "nickname",
                "A Pokemon's nickname",
                pokemon -> Optional.ofNullable(pokemon.getNickname()).map(Text::of).orElse((LiteralText) Text.EMPTY)
        ));
        this.register(new PokemonPlaceholder(
                "shiny",
                "A Marker indicating a Pokemon is Shiny",
                pokemon -> {
                    if(pokemon.isShiny()) {
                        return Text.of(TextColors.YELLOW, "\u2605");
                    }

                    return Text.EMPTY;
                }
        ));
        this.register(new PokemonPlaceholder(
                "ability",
                "Pokemon's Ability",
                pokemon -> Text.of(pokemon.getAbility().getLocalizedName()
                        .replaceAll("[\u00a7][r]", "")
                        .replaceAll("[\u00a7][k].", "")
                )
        ));
        this.register(new PokemonPlaceholder(
                "level",
                "Pokemon's Level",
                pokemon -> Text.of(pokemon.getLevel())
        ));
        this.register(new PokemonPlaceholder(
                "form",
                "Pokemon's Form",
                pokemon -> {
                    return Optional.ofNullable(pokemon.getFormEnum())
                            .filter(form -> form.getForm() != 0)
                            .map(form -> (Text) Text.of(form.getLocalizedName()))
                            .orElse(Text.of("N/A"));
                }
        ));
        this.register(new PokemonPlaceholder(
                "gender",
                "Pokemon's Gender",
                pokemon -> {
                    Gender gender = pokemon.getGender();
                    TextColor color = gender == Gender.Male ? TextColors.AQUA :
                            gender == Gender.Female ? TextColors.LIGHT_PURPLE : TextColors.GRAY;

                    return Text.of(color, pokemon.getGender().getLocalizedName());
                }
        ));
        this.register(new PokemonPlaceholder(
                "nature",
                "Pokemon's Nature",
                pokemon -> {
                    Text result = Text.of(pokemon.getBaseNature().getLocalizedName());
                    if(pokemon.getMintNature() != null) {
                        result = Text.of(result, TextColors.GRAY, " (", TextColors.GOLD,
                                pokemon.getMintNature().getLocalizedName(), TextColors.GRAY, ")");
                    }

                    return result;
                }
        ));
        this.register(new PokemonPlaceholder(
                "size",
                "Pokemon's Size",
                pokemon -> Text.of(pokemon.getGrowth().getLocalizedName())
        ));
        this.register(new PokemonPlaceholder(
                "unbreedable",
                "Whether a Pokemon is Breedable or not",
                pokemon -> {
                    if(UnbreedableFlag.UNBREEDABLE.matches(pokemon)) {
                        return Text.of(TextColors.RED, "Unbreedable");
                    } else {
                        return Text.of(TextColors.GREEN, "Breedable");
                    }
                }
        ));

        for(String stat : Lists.newArrayList("ev", "iv")) {
            for (StatsType type : Lists.newArrayList(StatsType.HP, StatsType.Attack, StatsType.Defence, StatsType.SpecialAttack, StatsType.SpecialDefence, StatsType.Speed)) {
                this.register(new PokemonPlaceholder(
                        stat + "_" + type.name().toLowerCase(),
                        "A Pokemon's " + type.getLocalizedName() + " " + stat.toUpperCase() + " Stat",
                        pokemon -> {
                            if(stat.equals("ev")) {
                                return Text.of(pokemon.getStats().evs.get(type));
                            } else {
                                return Text.of(pokemon.getStats().ivs.get(type));
                            }
                        }
                ));
            }
        }
        this.register(new PokemonPlaceholder(
                "ev_percentage",
                "A Pokemon's Percentage of Total EVs Gained",
                pokemon -> {
                    EVStore evs = pokemon.getEVs();
                    double sum = 0;
                    for(int stat : evs.getArray()) {
                        sum += stat;
                    }

                    return Text.of(PERCENTAGE.format(sum / 510.0 * 100) + "%");
                }
        ));
        this.register(new PokemonPlaceholder(
                "iv_percentage",
                "A Pokemon's Percentage of Total IVs Gained",
                pokemon -> {
                    IVStore ivs = pokemon.getIVs();
                    double sum = 0;
                    for(int stat : ivs.getArray()) {
                        sum += stat;
                    }

                    return Text.of(PERCENTAGE.format(sum / 186.0 * 100) + "%");
                }
        ));
        this.register(new PokemonPlaceholder(
                "dynamax_level",
                "A Pokemon's Dynamax Level",
                pokemon -> Text.of(pokemon.getDynamaxLevel())
        ));
        for(AtomicInteger i = new AtomicInteger(1); i.get() <= 4; i.incrementAndGet()) {
            this.register(new PokemonPlaceholder(
                    "move_" + i,
                    "A Pokemon's Move in Slot " + i,
                    pokemon -> Optional.ofNullable(pokemon.getMoveset().get(i.get()))
                            .map(attack -> attack.getActualMove().getLocalizedName())
                            .map(Text::of)
                            .orElse((LiteralText) Text.EMPTY)
            ));
        }
        this.register(new PokemonPlaceholder(
                "texture",
                "A Pokemon's custom texture",
                pokemon -> Optional.ofNullable(pokemon.getCustomTexture()).map(Text::of).orElse((LiteralText) Text.EMPTY)
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                ReforgedDaycarePokemonWrapper.class,
                "gained_lvls",
                "The levels gained by a pokemon in the Daycare",
                pokemon -> Text.of(pokemon.getGainedLevels())
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                ReforgedDaycarePokemonWrapper.class,
                "calced_lvl",
                "The calculated level of the pokemon in the daycare",
                pokemon -> Text.of(pokemon.getDelegate().getLevel() + pokemon.getGainedLevels())
        ));
        this.register(new PokemonPlaceholder(
                "held_item",
                "A Pokemon's Held Item",
                pokemon -> {
                    ItemStack item = pokemon.getHeldItem();
                    if(item == ItemStack.EMPTY) {
                        return Text.EMPTY;
                    }

                    return Text.of(pokemon.getHeldItem().getDisplayName());
                }
        ));
        this.register(new PokemonPlaceholder(
                "evolution",
                "A Pokemon's Evolution",
                pokemon -> {
                    ArrayList<LevelingEvolution> evolutions = pokemon.getEvolutions(LevelingEvolution.class);
                    if(evolutions.size() > 0) {
                        LevelingEvolution evolution = evolutions.get(0);
                        return Text.of(evolution.to.name);
                    }

                    return Text.EMPTY;
                }
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Text.class,
                "claim_price",
                "Claim Price of a Pokemon in the Daycare",
                text -> text
        ));
        this.register(new MoveUpdatePlaceholder(
                "new_move",
                "The name of the Move a Pokemon is learning",
                context -> {
                    if(context.getContext() == MoveUpdatePlaceholder.MoveUpdateContext.Context.NEW) {
                        return Text.of(context.getAttack().getLocalizedName());
                    }

                    return Text.EMPTY;
                }
        ));
        this.register(new MoveUpdatePlaceholder(
                "old_move",
                "The name of the Move a Pokemon is forgetting",
                context -> {
                    if(context.getContext() == MoveUpdatePlaceholder.MoveUpdateContext.Context.OLD) {
                        return Text.of(context.getAttack().getLocalizedName());
                    }

                    return Text.EMPTY;
                }
        ));
        this.register(new PokemonPlaceholder(
                "breedable",
                "Whether a Pokemon is Breedable",
                pokemon -> {
                    if(Flags.UNBREEDABLE.matches(pokemon)) {
                        return Text.of(TextColors.RED, TextParser.parse(TextParser.read(MsgConfigKeys.UNBREEDABLE_TRANSLATION)));
                    } else {
                        return Text.of(TextColors.GREEN, TextParser.parse(TextParser.read(MsgConfigKeys.BREEDABLE_TRANSLATION)));
                    }
                }
        ));
        this.register(new PokemonPlaceholder(
                "ivs_total",
                "The total value of IVs this pokemon has",
                pokemon -> Text.of(Arrays.stream(pokemon.getStats().ivs.getArray()).sum())
        ));
        this.register(new PokemonPlaceholder(
                "gender_icon",
                "Gender Icon of a Pokemon",
                pokemon -> {
                    switch (pokemon.getGender()) {
                        case Male:
                            return Text.of(TextColors.AQUA, "\u2642");
                        case Female:
                            return Text.of(TextColors.LIGHT_PURPLE, "\u2640");
                        default:
                            return Text.EMPTY;
                    }
                }
        ));
    }

}
