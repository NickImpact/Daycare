package com.nickimpact.daycare.spigot.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.nickimpact.daycare.spigot.SpigotDaycarePlugin;
import com.nickimpact.daycare.spigot.ui.RanchUI;
import com.nickimpact.daycare.spigot.utils.MessageUtils;
import org.bukkit.entity.Player;

@CommandAlias("daycare")
@CommandPermission("daycare.command.daycare.base")
public class DaycareCmd extends BaseCommand {

	@Default
	public void execute(Player player) {
		new RanchUI(player).open();
	}

	@Subcommand("admin")
	@CommandPermission("daycare.command.admin.base")
	public class Admin extends BaseCommand {

		@Default
		public void onNoArgs(Player player) {
			player.sendMessage(MessageUtils.parse("Usage: /daycare admin <addnpc <npc name> | deletenpc>", true, true));
		}

		@Subcommand("addnpc|anpc")
		@CommandPermission("daycare.command.admin.addnpc")
		@Syntax("<name>")
		public void addNPC(Player player, String npcName) {
			SpigotDaycarePlugin.getInstance().getService().getNPCManager().addNPCAdder(player.getUniqueId(), npcName);
			player.sendMessage(MessageUtils.parse("&7Right click on a chatting NPC to set them as a Daycare Representative!", true, false));
		}

		@Subcommand("deletenpc|dnpc")
		@CommandPermission("daycare.command.admin.deletenpc")
		public void deleteNPC(Player player) {
			SpigotDaycarePlugin.getInstance().getService().getNPCManager().removeRemover(player.getUniqueId());
		}
	}
}
