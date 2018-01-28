package com.nickimpact.daycare.listeners;

import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.ranch.Ranch;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

/**
 * (Some note will appear here)
 *
 * @author NickImpact (Nick DeGruccio)
 */
public class ConnectionListener {

	@Listener
	public void onJoin(ClientConnectionEvent.Join e) {
		Player player = e.getTargetEntity();
		DaycarePlugin.getInstance().getStorage().getRanch(player.getUniqueId()).thenAccept(r -> {
			if(r == null) {
				r = new Ranch(player);
				DaycarePlugin.getInstance().getStorage().addRanch(r);
			}
			DaycarePlugin.getInstance().getRanches().add(r);
		});
	}

	@Listener
	public void onDisconnect(ClientConnectionEvent.Disconnect e) {
		Player player = e.getTargetEntity();
		Ranch ranch = DaycarePlugin.getInstance().getRanches().stream().filter(r -> r.getOwnerUUID().equals(player.getUniqueId())).findAny().orElse(null);
		if(ranch != null) {
			DaycarePlugin.getInstance().getStorage().updateRanch(ranch);
			DaycarePlugin.getInstance().getRanches().remove(ranch);
		}
	}
}
