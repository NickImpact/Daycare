package com.nickimpact.daycare.commands;

import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.api.commands.SpongeCommand;
import com.nickimpact.daycare.api.commands.annotations.CommandAliases;
import com.nickimpact.daycare.ui.RanchUI;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

/**
 * (Some note will appear here)
 *
 * @author NickImpact (Nick DeGruccio)
 */
@CommandAliases({"daycare"})
public class DaycareCmd extends SpongeCommand {

	@Override
	public CommandElement[] getArgs() {
		return new CommandElement[0];
	}

	@Override
	public Text getDescription() {
		return Text.of();
	}

	@Override
	public Text getUsage() {
		return Text.of();
	}

	@Override
	public SpongeCommand[] getSubCommands() {
		return new SpongeCommand[0];
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if(src instanceof Player) {
			((Player)src).openInventory(new RanchUI(
					(Player)src,
					DaycarePlugin.getInstance()
							.getRanches()
							.stream()
							.filter(ranch -> ranch.getOwnerUUID().equals(((Player) src).getUniqueId()))
							.findAny().get()
			).getInventory());
		}

		return CommandResult.success();
	}
}
