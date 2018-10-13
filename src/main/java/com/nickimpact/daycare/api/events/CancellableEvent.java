package com.nickimpact.daycare.api.events;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;

/**
 * Represents an event which is cancellable.
 *
 * @author NickImpact (Nick DeGruccio)
 */
public class CancellableEvent implements Cancellable, Event {

	private boolean cancelled = false;

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}

	@Override
	public Cause getCause() {
		return Sponge.getCauseStackManager().getCurrentCause();
	}
}
