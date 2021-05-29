package com.nickimpact.daycare.sponge.implementation;

import com.nickimpact.daycare.api.pens.Pen;
import com.nickimpact.daycare.api.pens.Ranch;
import com.nickimpact.daycare.api.pens.Statistics;
import net.impactdev.impactor.api.json.JsonTyping;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@JsonTyping("daycare_sponge_ranch")
public class SpongeRanch extends Ranch<SpongePen> {

	public SpongeRanch(UUID uuid) {
		super(uuid);
	}

	private SpongeRanch(SpongeRanchBuilder builder) {
		super(builder.id, builder.owner, builder.pens, builder.stats);
	}

	@Override
	public SpongePen newPen(int id) {
		return (SpongePen) SpongePen.builder().id(id).build();
	}

	public static class SpongeRanchBuilder implements RanchBuilder {

		private UUID id;
		private UUID owner;
		private List<SpongePen> pens;
		private Statistics stats;

		@Override
		public RanchBuilder identifier(UUID identifier) {
			this.id = identifier;
			return this;
		}

		@Override
		public RanchBuilder owner(UUID owner) {
			this.owner = owner;
			return this;
		}

		@Override
		public RanchBuilder pens(List<Pen> pens) {
			this.pens = pens.stream().map(p -> (SpongePen) p).collect(Collectors.toList());
			return this;
		}

		@Override
		public RanchBuilder stats(Statistics stats) {
			this.stats = stats;
			return this;
		}

		@Override
		public RanchBuilder from(Ranch ranch) {
			return null;
		}

		@Override
		public SpongeRanch build() {
			return new SpongeRanch(this);
		}
	}
}
