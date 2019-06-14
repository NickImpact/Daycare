package com.nickimpact.daycare.implementation;

import com.nickimpact.daycare.api.pens.Pen;
import com.nickimpact.daycare.api.pens.Ranch;
import com.nickimpact.daycare.api.pens.Statistics;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SpigotRanch extends Ranch<SpigotPen> {

    public SpigotRanch(UUID uuid) {
        super(uuid);
    }

    private SpigotRanch(SpigotRanchBuilder builder) {
        super(builder.id, builder.owner, builder.pens, builder.stats);
    }

    @Override
    public SpigotPen newPen(int id) {
        return null;
    }

    public static class SpigotRanchBuilder implements RanchBuilder {

        private UUID id;
        private UUID owner;
        private List<SpigotPen> pens;
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
            this.pens = pens.stream().map(p -> (SpigotPen) p).collect(Collectors.toList());
            return this;
        }

        @Override
        public RanchBuilder stats(Statistics stats) {
            this.stats = stats;
            return this;
        }

        @Override
        public SpigotRanch build() {
            return new SpigotRanch(this);
        }
    }
}
