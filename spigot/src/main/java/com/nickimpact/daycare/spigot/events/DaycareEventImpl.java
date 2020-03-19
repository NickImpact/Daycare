package com.nickimpact.daycare.spigot.events;

import com.nickimpact.daycare.api.events.DaycareEvent;
import com.nickimpact.daycare.api.pens.DaycarePokemonWrapper;
import com.nickimpact.daycare.api.pens.Pen;
import com.pixelmonmod.pixelmon.battles.attacks.AttackBase;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Optional;
import java.util.UUID;

public abstract class DaycareEventImpl extends Event implements DaycareEvent, Cancellable {

    private UUID owner;
    private Pen pen;

    private boolean cancelled;

    public DaycareEventImpl(UUID uuid, Pen pen) {
        this.owner = uuid;
        this.pen = pen;
    }

    @Override
    public UUID getOwner() {
        return this.owner;
    }

    @Override
    public Pen getPen() {
        return this.pen;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean flag) {
        this.cancelled = flag;
    }

    public static class AddPokemon extends DaycareEventImpl implements DaycareEvent.AddPokemon<DaycarePokemonWrapper> {

        private DaycarePokemonWrapper pokemon;
        private static HandlerList handlerList = new HandlerList();

        public AddPokemon(UUID uuid, Pen pen, DaycarePokemonWrapper pokemon) {
            super(uuid, pen);
            this.pokemon = pokemon;
        }

        @Override
        public DaycarePokemonWrapper getPokemon() {
            return pokemon;
        }

        @Override
        public HandlerList getHandlers() {
            return handlerList;
        }

        public static HandlerList getHandlerList() {
            return handlerList;
        }
    }

    public static class RemovePokemon extends DaycareEventImpl implements DaycareEvent.RemovePokemon<DaycarePokemonWrapper> {

        private DaycarePokemonWrapper pokemon;
        private static HandlerList handlerList = new HandlerList();

        public RemovePokemon(UUID uuid, Pen pen, DaycarePokemonWrapper pokemon) {
            super(uuid, pen);
            this.pokemon = pokemon;
        }

        @Override
        public DaycarePokemonWrapper getRemovedPokemon() {
            return this.pokemon;
        }

        @Override
        public HandlerList getHandlers() {
            return handlerList;
        }

        public static HandlerList getHandlerList() {
            return handlerList;
        }
    }

    public static class Breed extends DaycareEventImpl implements DaycareEvent.Breed<DaycarePokemonWrapper> {

        private DaycarePokemonWrapper male;
        private DaycarePokemonWrapper female;
        private DaycarePokemonWrapper egg;

        private static HandlerList handlerList = new HandlerList();

        public Breed(UUID uuid, Pen pen, DaycarePokemonWrapper male, DaycarePokemonWrapper female, DaycarePokemonWrapper egg) {
            super(uuid, pen);
            this.male = male;
            this.female = female;
            this.egg = egg;
        }

        @Override
        public DaycarePokemonWrapper getMaleParent() {
            return this.male;
        }

        @Override
        public DaycarePokemonWrapper getFemaleParent() {
            return this.female;
        }

        @Override
        public DaycarePokemonWrapper getEgg() {
            return this.egg;
        }

        @Override
        public void setTarget(DaycarePokemonWrapper pokemon) {
            this.egg = pokemon;
        }

        @Override
        public HandlerList getHandlers() {
            return handlerList;
        }

        public static HandlerList getHandlerList() {
            return handlerList;
        }
    }

    public static class CollectEgg extends DaycareEventImpl implements DaycareEvent.CollectEgg<DaycarePokemonWrapper> {

        private DaycarePokemonWrapper egg;

        private static HandlerList handlerList = new HandlerList();

        public CollectEgg(UUID uuid, Pen pen, DaycarePokemonWrapper egg) {
            super(uuid, pen);
            this.egg = egg;
        }

        public DaycarePokemonWrapper getEgg() {
            return this.egg;
        }

        @Override
        public HandlerList getHandlers() {
            return handlerList;
        }

        public static HandlerList getHandlerList() {
            return handlerList;
        }
    }

    public static class LevelUp extends DaycareEventImpl implements DaycareEvent.LevelUp<DaycarePokemonWrapper> {

        private DaycarePokemonWrapper pokemon;
        private int level;
        private int gained;

        private static HandlerList handlerList = new HandlerList();

        public LevelUp(UUID uuid, Pen pen, DaycarePokemonWrapper wrapper, int level) {
            super(uuid, pen);
            this.pokemon = wrapper;
            this.level = level;
            this.gained = wrapper.getGainedLevels();
        }

        @Override
        public DaycarePokemonWrapper getPokemon() {
            return this.pokemon;
        }

        @Override
        public int toLevel() {
            return this.level;
        }

        @Override
        public int getGainedLevels() {
            return this.gained;
        }

        @Override
        public HandlerList getHandlers() {
            return handlerList;
        }

        public static HandlerList getHandlerList() {
            return handlerList;
        }
    }

    public static class LearnMove extends DaycareEventImpl implements DaycareEvent.LearnMove<DaycarePokemonWrapper, AttackBase> {

        private DaycarePokemonWrapper learner;
        private AttackBase learned;
        private AttackBase forgot;

        private static HandlerList handlerList = new HandlerList();

        public LearnMove(UUID uuid, Pen pen, DaycarePokemonWrapper learner, AttackBase learned) {
            this(uuid, pen, learner, learned, null);
        }

        public LearnMove(UUID uuid, Pen pen, DaycarePokemonWrapper learner, AttackBase learned, AttackBase forgot) {
            super(uuid, pen);
            this.learner = learner;
            this.learned = learned;
            this.forgot = forgot;
        }


        @Override
        public DaycarePokemonWrapper getLearner() {
            return this.learner;
        }

        @Override
        public AttackBase getAttackLearned() {
            return this.learned;
        }

        @Override
        public Optional<AttackBase> getForgottenMove() {
            return Optional.ofNullable(this.forgot);
        }

        @Override
        public HandlerList getHandlers() {
            return handlerList;
        }

        public static HandlerList getHandlerList() {
            return handlerList;
        }
    }

    public static class Evolve extends DaycareEventImpl implements DaycareEvent.Evolve<DaycarePokemonWrapper, EnumSpecies> {

        private DaycarePokemonWrapper evolving;
        private EnumSpecies to;

        private static HandlerList handlerList = new HandlerList();

        public Evolve(UUID uuid, Pen pen, DaycarePokemonWrapper evolving, EnumSpecies to) {
            super(uuid, pen);
            this.evolving = evolving;
            this.to = to;
        }

        @Override
        public DaycarePokemonWrapper getPokemonEvolving() {
            return this.evolving;
        }

        @Override
        public EnumSpecies getSpeciesEvolvedInto() {
            return this.to;
        }

        @Override
        public HandlerList getHandlers() {
            return handlerList;
        }

        public static HandlerList getHandlerList() {
            return handlerList;
        }
    }
}