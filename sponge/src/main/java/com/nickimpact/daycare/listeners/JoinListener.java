package com.nickimpact.daycare.listeners;

import com.nickimpact.daycare.SpongeDaycarePlugin;
import com.nickimpact.daycare.api.pens.Ranch;
import com.nickimpact.daycare.api.util.PluginInstance;
import com.nickimpact.daycare.implementation.SpongeRanch;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;


public class JoinListener {

	@Listener
	public void onJoin(ClientConnectionEvent.Join e, @First Player player) {
		PluginInstance.getPlugin().getService().getStorage().getRanch(player.getUniqueId()).thenAccept(ranch -> {
			if(!ranch.isPresent()) {
				SpongeRanch r = new SpongeRanch(player.getUniqueId());
				SpongeDaycarePlugin.getSpongeInstance().getService().getRanchManager().addRanch(r);
				SpongeDaycarePlugin.getSpongeInstance().getService().getStorage().addRanch(r);
			} else {
				SpongeDaycarePlugin.getSpongeInstance().getService().getRanchManager().addRanch(ranch.get());
			}
		});
	}
}
