package com.nickimpact.daycare.api.breeding;

import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.api.events.DaycareEvent;
import com.nickimpact.daycare.configuration.MsgConfigKeys;
import com.nickimpact.daycare.ranch.Pen;
import com.nickimpact.daycare.ranch.Pokemon;
import com.nickimpact.daycare.utils.MessageUtils;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.util.helpers.BreedLogic;
import lombok.Getter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.UUID;

/**
 * Represents the backbone to different types of breeding environments for the plugin in its whole.
 * We want to be able to support multiple types of breeding, such as a timed task version, or one
 * similar to the main mod itself.
 */
public interface BreedStyle {

	String getID();

	Instance createInstance(UUID owner, Pen pen, Pokemon p1, Pokemon p2);

	abstract class BreedStyleUpdate extends DaycareEvent {
		protected BreedStyleUpdate(UUID owner, Pen pen) {
			super(owner, pen);
		}
	}

	abstract class Instance {
		protected transient UUID owner;
		protected transient Pen pen;
		protected transient Pokemon p1;
		protected transient Pokemon p2;
		@Getter protected transient Task runner;

		protected Instance(UUID owner, Pen pen, Pokemon p1, Pokemon p2){
			this.owner = owner;
			this.pen = pen;
			this.p1 = p1;
			this.p2 = p2;
		}

		public abstract Task register();

		public Instance supply(UUID owner, Pen pen, Pokemon p1, Pokemon p2) {
			this.owner = owner;
			this.pen = pen;
			this.p1 = p1;
			this.p2 = p2;

			return this;
		}

		protected void fireSync(Runnable runnable) {
			Sponge.getScheduler().createTaskBuilder().execute(runnable).submit(DaycarePlugin.getInstance());
		}

		public abstract boolean meetsRequirements();

		protected void breed() {
			com.pixelmonmod.pixelmon.api.pokemon.Pokemon base = BreedLogic.makeEgg(this.p1.getPokemon(), this.p2.getPokemon());
			if(base == null) return;

			Pokemon egg = new Pokemon(base);

			if(!Sponge.getEventManager().post(new DaycareEvent.Breed(owner, pen, p1, p2, egg))) {
				Sponge.getServer().getPlayer(owner).ifPresent(player -> {
					player.sendMessages(MessageUtils.fetchAndParseMsgs(player, MsgConfigKeys.EGGS_AVAILABLE, null, null));
				});

				pen.setEgg(egg);
			}
		}

		protected abstract void update();

		public abstract List<Text> getDescription();
	}
}
