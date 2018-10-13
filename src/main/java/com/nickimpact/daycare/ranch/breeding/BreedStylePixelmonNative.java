package com.nickimpact.daycare.ranch.breeding;

import com.google.common.collect.Lists;
import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.api.breeding.BreedStyle;
import com.nickimpact.daycare.configuration.ConfigKeys;
import com.nickimpact.daycare.ranch.Pen;
import com.nickimpact.daycare.ranch.Pokemon;
import com.nickimpact.impactor.json.Typing;
import lombok.Getter;
import lombok.Setter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BreedStylePixelmonNative implements BreedStyle {

	@Override
	public String getID() {
		return "pixelmon-native";
	}

	@Override
	public BSPNInstance createInstance(UUID owner, Pen pen, Pokemon p1, Pokemon p2) {
		return new BSPNInstance(owner, pen, p1, p2);
	}

	@Getter
	@Setter
	private static class StageUpdate extends BreedStyleUpdate {

		private int prevStage;
		private int newStage;

		private StageUpdate(UUID owner, Pen pen, int prevStage, int newStage) {
			super(owner, pen);
			this.prevStage = prevStage;
			this.newStage = newStage;
		}
	}

	@Getter
	@Typing("pixelmon-native")
	public static class BSPNInstance extends Instance {

		private String type = this.getClass().getAnnotation(Typing.class).value();
		private int stage = 1;

		BSPNInstance(UUID owner, Pen pen, Pokemon p1, Pokemon p2) {
			super(owner, pen, p1, p2);
			this.register();
		}

		@Override
		public Task register() {
			return runner = Sponge.getScheduler().createTaskBuilder()
					.execute(() -> this.fireSync(() -> {
						if (!Sponge.getEventManager().post(new StageUpdate(owner, pen, stage, stage + 1))) {
							this.update();
						}
					}))
					.delay(DaycarePlugin.getInstance().getConfig().get(ConfigKeys.BREED_PNATIVE_DELAY), TimeUnit.MINUTES)
					.interval(DaycarePlugin.getInstance().getConfig().get(ConfigKeys.BREED_PNATIVE_INTERVAL), TimeUnit.MINUTES)
					.async()
					.submit(DaycarePlugin.getInstance());
		}

		@Override
		public boolean meetsRequirements() {
			return this.stage == 6;
		}

		@Override
		public void update() {
			if (!pen.canBreed()) {
				this.stage = 1;
				return;
			}

			if (this.meetsRequirements()) {
				this.breed();
				this.stage = 1;
			} else {
				++this.stage;
			}
		}

		@Override
		public List<Text> getDescription() {
			return Lists.newArrayList(Text.of(TextColors.GRAY, "Current Stage: ", TextColors.YELLOW, stage));
		}
	}
}
