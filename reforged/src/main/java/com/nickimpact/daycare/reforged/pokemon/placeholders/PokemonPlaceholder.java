package com.nickimpact.daycare.reforged.pokemon.placeholders;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.placeholder.PlaceholderContext;
import org.spongepowered.api.text.placeholder.PlaceholderParser;

import java.util.function.Function;

public class PokemonPlaceholder implements PlaceholderParser {

    private final String id;
    private final String name;
    private final Function<Pokemon, Text> parser;

    public PokemonPlaceholder(String id, String name, Function<Pokemon, Text> parser) {
        this.id = id;
        this.name = name;
        this.parser = parser;
    }

    @Override
    public Text parse(PlaceholderContext context) {
        return context.getAssociatedObject()
                .filter(source -> source instanceof Pokemon)
                .map(source -> (Pokemon) source)
                .map(this.parser)
                .orElse(Text.EMPTY);
    }

    @Override
    public String getId() {
        return "daycare-reforged:" + this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
