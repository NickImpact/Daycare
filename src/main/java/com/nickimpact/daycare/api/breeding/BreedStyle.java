package com.nickimpact.daycare.api.breeding;

import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.api.events.DaycareEvent;
import com.nickimpact.daycare.configuration.MsgConfigKeys;
import com.nickimpact.daycare.ranch.Pen;
import com.nickimpact.daycare.ranch.Pokemon;
import com.nickimpact.daycare.utils.MessageUtils;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Optional;
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
			EntityPixelmon offspring = new EntityPixelmon(p1.getPokemon().world);
			offspring.makeEntityIntoEgg(p1.getPokemon(), p2.getPokemon());
			Pokemon egg = new Pokemon(offspring);

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
