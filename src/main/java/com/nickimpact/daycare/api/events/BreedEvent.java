package com.nickimpact.daycare.api.events;


import com.nickimpact.daycare.ranch.Pokemon;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

/**
 * (Some note will appear here)
 *
 * @author NickImpact (Nick DeGruccio)
 */
@AllArgsConstructor
@Getter
public class BreedEvent extends CancellableEvent {

	private final UUID owner;
	private final int penID;

	private Pokemon parent1;
	private Pokemon parent2;
	private Pokemon offspring;
}
