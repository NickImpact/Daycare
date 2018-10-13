package com.nickimpact.daycare.commands;

import com.nickimpact.daycare.commands.admin.AdminBaseCmd;
import com.nickimpact.daycare.ui.RanchUI;
import com.nickimpact.impactor.api.commands.SpongeCommand;
import com.nickimpact.impactor.api.commands.annotations.Aliases;
import com.nickimpact.impactor.api.commands.annotations.Permission;
import com.nickimpact.impactor.api.plugins.SpongePlugin;
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
@Aliases({"daycare"})
@Permission
public class DaycareCmd extends SpongeCommand {

	public DaycareCmd(SpongePlugin plugin) {
		super(plugin);
	}

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
		return new SpongeCommand[] {
				new AddCmd(this.plugin),
				new AdminBaseCmd(this.plugin)
		};
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if(src instanceof Player) {
			new RanchUI((Player)src).open((Player)src);
		}

		return CommandResult.success();
	}
}
