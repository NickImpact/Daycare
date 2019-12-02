package com.nickimpact.daycare.spigot.listeners;

import com.nickimpact.daycare.spigot.SpigotDaycarePlugin;
import com.nickimpact.daycare.api.DaycareService;
import com.nickimpact.daycare.api.util.PluginInstance;
import com.nickimpact.daycare.spigot.implementation.SpigotRanch;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinListener implements Listener {

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		PluginInstance.getPlugin().getService().getStorage().getRanch(player.getUniqueId()).thenAccept(ranch -> {
			if(!ranch.isPresent()) {
				SpigotDaycarePlugin.getInstance().getPluginLogger().debug("No ranch found, creating a new one");
				SpigotRanch r = new SpigotRanch(player.getUniqueId());
				SpigotDaycarePlugin.getInstance().getService().getRanchManager().addRanch(r);
				SpigotDaycarePlugin.getInstance().getService().getStorage().addRanch(r).exceptionally(ex -> {
					ex.printStackTrace();
					return false;
				});
			} else {
				SpigotDaycarePlugin.getInstance().getService().getRanchManager().addRanch(ranch.get());
			}
		}).exceptionally(x -> {
			x.printStackTrace();
			return null;
		});
	}

	@EventHandler
	public void onLeave(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		DaycareService service = PluginInstance.getPlugin().getService();
		service.getRanchManager().getRanch(player.getUniqueId()).ifPresent(ranch -> {
			service.getStorage().updateRanch(ranch);
			service.getRanchManager().getLoadedRanches().removeIf(r -> r.getIdentifier().equals(ranch.getIdentifier()));
		});
	}

}
