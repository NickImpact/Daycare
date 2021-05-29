package com.nickimpact.daycare.reforged.pokemon.placeholders;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.attacks.AttackBase;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.placeholder.PlaceholderContext;
import org.spongepowered.api.text.placeholder.PlaceholderParser;

import java.util.function.Function;

public class MoveUpdatePlaceholder implements PlaceholderParser {

    private final String id;
    private final String name;
    private final Function<MoveUpdateContext, Text> parser;

    public MoveUpdatePlaceholder(String id, String name, Function<MoveUpdateContext, Text> parser) {
        this.id = id;
        this.name = name;
        this.parser = parser;
    }

    @Override
    public Text parse(PlaceholderContext context) {
        return context.getAssociatedObject()
                .filter(source -> source instanceof MoveUpdateContext)
                .map(source -> (MoveUpdateContext) source)
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

    public static class MoveUpdateContext {

        private final AttackBase attack;
        private final Context context;

        public MoveUpdateContext(AttackBase attack, Context context) {
            this.attack = attack;
            this.context = context;
        }

        public AttackBase getAttack() {
            return attack;
        }

        public Context getContext() {
            return context;
        }

        public enum Context {

            OLD,
            NEW

        }
    }

}
