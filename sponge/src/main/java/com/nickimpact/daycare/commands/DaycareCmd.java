package com.nickimpact.daycare.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.nickimpact.daycare.ui.RanchUI;
import org.spongepowered.api.entity.living.player.Player;

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

		}

		@CommandAlias("addnpc")
		@CommandPermission("daycare.command.admin.addnpc")
		public void addNPC(Player player, String npcName) {

		}

		@CommandAlias("deleteNPC")
		@CommandPermission("daycare.command.admin.deletenpc")
		public void deleteNPC(Player player, String npcName) {

		}
	}
}
