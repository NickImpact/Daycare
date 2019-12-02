package com.nickimpact.daycare.sponge.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.nickimpact.daycare.sponge.SpongeDaycarePlugin;
import com.nickimpact.daycare.sponge.configuration.MsgConfigKeys;
import com.nickimpact.daycare.sponge.ui.RanchUI;
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
			player.sendMessage(SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().parse("{{daycare_prefix}} Usage: /daycare admin <addnpc <npc name> | deletenpc>", player, null, null));
		}

		@Subcommand("addnpc|anpc")
		@CommandPermission("daycare.command.admin.addnpc")
		@Syntax("<name>")
		public void addNPC(Player player, String npcName) {
			SpongeDaycarePlugin.getSpongeInstance().getService().getNPCManager().addNPCAdder(player.getUniqueId(), npcName);
			player.sendMessage(SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsg(player, MsgConfigKeys.CMD_ADDNPC_RIGHTCLICK_NOTICE, null, null));
		}

		@Subcommand("deletenpc|dnpc")
		@CommandPermission("daycare.command.admin.deletenpc")
		public void deleteNPC(Player player) {
			SpongeDaycarePlugin.getSpongeInstance().getService().getNPCManager().removeRemover(player.getUniqueId());
		}
	}
}
