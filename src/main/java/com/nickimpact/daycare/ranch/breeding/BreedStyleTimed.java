package com.nickimpact.daycare.ranch.breeding;

import com.google.common.collect.Lists;
import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.api.breeding.BreedStyle;
import com.nickimpact.daycare.configuration.ConfigKeys;
import com.nickimpact.daycare.ranch.Pen;
import com.nickimpact.daycare.ranch.Pokemon;
import com.nickimpact.impactor.json.Typing;
import com.nickimpact.impactor.time.Time;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BreedStyleTimed implements BreedStyle {

	private static final Random rng = new Random();

	@Override
	public String getID() {
		return "timed-chance";
	}

	@Override
	public Instance createInstance(UUID owner, Pen pen, Pokemon p1, Pokemon p2) {
		return new BSTInstance(owner, pen, p1, p2);
	}

	@Typing("timed-chance")
	public static class BSTInstance extends Instance {

		private String type = this.getClass().getAnnotation(Typing.class).value();

		private transient final long maxWait = DaycarePlugin.getInstance().getConfig().get(ConfigKeys.BREED_TIMED_MAX_WAIT);
		private transient final long minWait = DaycarePlugin.getInstance().getConfig().get(ConfigKeys.BREED_TIMED_MIN_WAIT);
		private long currWait = 0;

		BSTInstance(UUID owner, Pen pen, Pokemon p1, Pokemon p2) {
			super(owner, pen, p1, p2);
			this.register();
		}

		@Override
		public Task register() {
			return runner = Sponge.getScheduler().createTaskBuilder()
					.execute(() -> {
						if(currWait >= minWait && currWait % DaycarePlugin.getInstance().getConfig().get(ConfigKeys.BREED_TIMED_CHECK_PERIOD) == 0) {
							this.fireSync(this::update);
						}
						++currWait;
					})
					.interval(DaycarePlugin.getInstance().getConfig().get(ConfigKeys.BREED_TIMED_UPDATE_INTERVAL), TimeUnit.SECONDS)
					.async()
					.submit(DaycarePlugin.getInstance());
		}

		@Override
		public boolean meetsRequirements() {
			return this.currWait >= (minWait * 60) && rng.nextDouble() * 100 < DaycarePlugin.getInstance().getConfig().get(ConfigKeys.BREED_TIMED_EGG_CHANCE);
		}

		@Override
		protected void update() {
			if(!pen.canBreed()) {
				this.currWait = 0;
				return;
			}

			if(this.meetsRequirements() || this.currWait == (this.maxWait * 60)) {
				this.breed();
				this.currWait = 0;
			}
		}

		@Override
		public List<Text> getDescription() {
			return Lists.newArrayList(
					Text.of(TextColors.GRAY, "Time Elapsed: ", TextColors.YELLOW, this.interpreted(new Time(currWait)))
			);
		}

		private String interpreted(Time time) {
			return time.getTime() <= 0L ? "00:00:00" : String.format("%02d:%02d:%02d", TimeUnit.SECONDS.toHours(time.getTime()), TimeUnit.SECONDS.toMinutes(time.getTime()) % 60L, time.getTime() % 60L);
		}
	}
}
