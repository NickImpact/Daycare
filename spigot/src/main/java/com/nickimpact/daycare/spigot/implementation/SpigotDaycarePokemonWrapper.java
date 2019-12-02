package com.nickimpact.daycare.spigot.implementation;

import com.nickimpact.daycare.spigot.SpigotDaycarePlugin;
import com.nickimpact.daycare.api.configuration.ConfigKeys;
import com.nickimpact.daycare.api.pens.DaycarePokemonWrapper;
import com.nickimpact.daycare.api.pens.Pen;
import com.nickimpact.daycare.api.pens.Ranch;
import com.nickimpact.daycare.api.util.GsonUtils;
import com.nickimpact.daycare.spigot.events.DaycareEventImpl;
import com.nickimpact.daycare.spigot.observers.PenObservers;
import com.nickimpact.daycare.spigot.ui.PenUI;
import com.nickimpact.daycare.spigot.utils.MessageUtils;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Moveset;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.evolution.conditions.EvoCondition;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.evolution.types.LevelingEvolution;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import gg.psyduck.pmixins.api.EvolutionPatch;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Loader;
import org.bukkit.Bukkit;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiPredicate;

public class SpigotDaycarePokemonWrapper extends DaycarePokemonWrapper<Pokemon> {

    public SpigotDaycarePokemonWrapper(Pokemon pokemon) {
        super(pokemon);
    }

    private SpigotDaycarePokemonWrapper(SpigotDaycarePokemonWrapperBuilder builder) {
        super(Pixelmon.pokemonFactory.create(GsonUtils.deserialize(builder.json)));
        this.json = builder.json;
        this.gainedLvls = builder.gainedLvls;
        this.lastLevelTime = builder.lastLvl;
    }

    @Override
    public Pokemon getDelegate() {
        return this.delegate != null ? this.delegate : Pixelmon.pokemonFactory.create(GsonUtils.deserialize(this.json));
    }

    @Override
    public NBTTagCompound toNBT(Pokemon pokemon) {
        return pokemon.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void createEgg(Ranch ranch, Pen pen) {
        SpigotDaycarePokemonWrapper p1 = (SpigotDaycarePokemonWrapper) pen.getAtPosition(1).get();
        SpigotDaycarePokemonWrapper p2 = (SpigotDaycarePokemonWrapper) pen.getAtPosition(2).get();
        int id = p1.getDelegate().getGender() == Gender.Male ? 1 : 2;

        DaycareEventImpl.Breed event = new DaycareEventImpl.Breed(ranch.getOwnerUUID(), pen, id == 1 ? p1 : p2, id == 1 ? p2 : p1, this);
        Bukkit.getPluginManager().callEvent(event);
        if(!event.isCancelled()) {
            pen.setEgg(this);
        }
    }

    @Override
    public void tryLevelUp(Ranch ranch, Pen pen) {
        if(this.getDelegate().getLevel() + this.getGainedLevels() > 100) {
            this.setGainedLevels(100 - this.getDelegate().getLevel());
            return;
        }

        LocalDateTime lastLevel = this.getLastLevelApplyTime();
        if(lastLevel == null) {
            this.setLastLevelApplyTime(lastLevel = LocalDateTime.now());
        }

        if(LocalDateTime.now().isAfter(lastLevel.plusSeconds(SpigotDaycarePlugin.getInstance().getConfiguration().get(ConfigKeys.LVL_WAIT_TIME)))) {
            DaycareEventImpl.LevelUp event = new DaycareEventImpl.LevelUp(ranch.getOwnerUUID(), pen, this, this.getDelegate().getLevel() + this.getGainedLevels() + 1);
            Bukkit.getPluginManager().callEvent(event);
            if(!event.isCancelled()) {
                this.setLastLevelApplyTime(LocalDateTime.now());
                this.incrementGainedLevels();

                boolean shouldUpdate = false;
                if (pen.getSettings().canLearnMoves()) {
                    if (learnMove(ranch, pen)) {
                        shouldUpdate = true;
                    }
                }

                if (Loader.isModLoaded("pmixins")) {
                    if (pen.getSettings().canEvolve()) {
                        if (evolve(ranch, pen)) {
                            if (pen.getSettings().canLearnMoves()) {
                                learnMove(ranch, pen);
                            }

                            shouldUpdate = true;
                        }
                    }
                }

                if (shouldUpdate) {
                    this.updatePokemon();
                    PenObservers.getObserver(ranch.getOwnerUUID()).ifPresent(PenUI::update);
                    SpigotDaycarePlugin.getInstance().getService().getStorage().updateRanch(ranch);
                }
            }
        }
    }

    private static final BiPredicate<Pokemon, List<EvoCondition>> evoConditionCheck = (pokemon, conditions) -> conditions.stream().allMatch(condition -> {
        if (condition instanceof EvolutionPatch) {
            return ((EvolutionPatch) condition).passes(pokemon);
        }

        return false;
    });

    @Override
    public boolean evolve(Ranch ranch, Pen pen) {
        Pokemon pokemon = this.getDelegate();
        ArrayList<LevelingEvolution> evolutions = this.getDelegate().getEvolutions(LevelingEvolution.class);
        if(evolutions.size() == 0) {
            return false;
        }

        for(LevelingEvolution evolution : evolutions) {
            if(evolution.to == null || evolution.to.name == null) continue;

            if(evolution.getLevel() <= pokemon.getLevel() + this.getGainedLevels() && evoConditionCheck.test(pokemon, evolution.conditions)) {
                DaycareEventImpl.Evolve event = new DaycareEventImpl.Evolve(ranch.getOwnerUUID(), pen, this, EnumSpecies.getFromNameAnyCase(evolution.to.name));
                Bukkit.getPluginManager().callEvent(event);
                if(!event.isCancelled()) {
                    Bukkit.getServer().getPlayer(ranch.getOwnerUUID()).sendMessage(MessageUtils.parse(
                        String.format("Your &e%s &7evolved into a &e%s &7after leveling up in the daycare!", pokemon.getSpecies().getPokemonName(), evolution.to.name),
                        true, false
                    ));
                    pokemon.evolve(evolution.to);

                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean learnMove(Ranch ranch, Pen pen) {
        Moveset moveset = this.getDelegate().getMoveset();
        LinkedHashMap<Integer, ArrayList<Attack>> levelupMoves = this.getDelegate().getBaseStats().levelUpMoves;
        int currentLvl = this.getGainedLevels() + this.getDelegate().getLevel();

        ArrayList<Attack> attacks = levelupMoves.get(currentLvl);
        if(attacks != null) {
            for(Attack attack : attacks) {
                if(moveset.size() < 4) {
                    DaycareEventImpl.LearnMove event = new DaycareEventImpl.LearnMove(ranch.getOwnerUUID(), pen, this, attack.getActualMove());
                    Bukkit.getPluginManager().callEvent(event);
                    if(!event.isCancelled()) {
                        moveset.add(attack);

                        Bukkit.getServer().getPlayer(ranch.getOwnerUUID()).sendMessage(MessageUtils.parse(
                                String.format("&7Your &e%s &7leveled up, and ended up learning &e%s!", this.getDelegate().getSpecies().getPokemonName(), attack.getActualMove().getTranslatedName().getFormattedText()),
                                true, false
                        ));
                    }
                } else {
                    DaycareEventImpl.LearnMove event = new DaycareEventImpl.LearnMove(ranch.getOwnerUUID(), pen, this, attack.getActualMove(), moveset.get(0).getActualMove());
                    Bukkit.getPluginManager().callEvent(event);
                    if(!event.isCancelled()) {
                        Attack old = moveset.remove(0);
                        moveset.add(attack);

                        Bukkit.getServer().getPlayer(ranch.getOwnerUUID()).sendMessage(MessageUtils.parse(
                                String.format("&7Your &e%s &7leveled up, and ended up learning &e%s &7in replacement of &e%s!",
                                        this.getDelegate().getSpecies().getPokemonName(),
                                        attack.getActualMove().getTranslatedName().getFormattedText(),
                                        old.getActualMove().getTranslatedName().getFormattedText()),
                                true, false
                        ));
                    }

                }

            }

            return true;
        }

        return false;
    }

    public static class SpigotDaycarePokemonWrapperBuilder implements DaycarePokemonWrapperBuilder {

        private String json;
        private int gainedLvls;
        private LocalDateTime lastLvl;

        public SpigotDaycarePokemonWrapperBuilder json(String json) {
            this.json = json;
            return this;
        }

        public SpigotDaycarePokemonWrapperBuilder gainedLvls(int lvls) {
            this.gainedLvls = lvls;
            return this;
        }

        public SpigotDaycarePokemonWrapperBuilder lastLvl(LocalDateTime lastLvl) {
            this.lastLvl = lastLvl;
            return this;
        }

        @Override
        public SpigotDaycarePokemonWrapper build() {
            return new SpigotDaycarePokemonWrapper(this);
        }

    }
}
