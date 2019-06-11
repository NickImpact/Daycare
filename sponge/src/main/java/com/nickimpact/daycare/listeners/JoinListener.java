package com.nickimpact.daycare.listeners;

import com.nickimpact.daycare.SpongeDaycarePlugin;
import com.nickimpact.daycare.implementation.SpongeRanch;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ClientConnectionEvent;


public class JoinListener {

	@Listener
	public void onJoin(ClientConnectionEvent.Join e, @First Player player) {
		boolean available = SpongeDaycarePlugin.getSpongeInstance().getService().getRanchManager().readRanch(player.getUniqueId());
		if(!available) {
			SpongeRanch ranch = new SpongeRanch(player.getUniqueId());
			SpongeDaycarePlugin.getSpongeInstance().getService().getRanchManager().addRanch(ranch);
			SpongeDaycarePlugin.getSpongeInstance().getService().getStorage().addRanch(ranch);
		}
	}
}
