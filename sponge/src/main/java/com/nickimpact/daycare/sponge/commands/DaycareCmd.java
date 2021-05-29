package com.nickimpact.daycare.sponge.commands;

import com.nickimpact.daycare.sponge.SpongeDaycarePlugin;
import com.nickimpact.daycare.sponge.commands.admin.DaycareAdminCmd;
import com.nickimpact.daycare.sponge.commands.annotations.Alias;
import com.nickimpact.daycare.sponge.commands.annotations.Permission;
import com.nickimpact.daycare.sponge.configuration.MsgConfigKeys;
import com.nickimpact.daycare.sponge.ui.RanchUI;
import com.nickimpact.daycare.sponge.utils.TextParser;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Optional;

@Alias("daycare")
@Permission("daycare.command.daycare.base")
public class DaycareCmd extends DaycareCmdExecutor {

	public DaycareCmd(SpongeDaycarePlugin plugin) {
		super(plugin);
	}

	public void execute(Player player) {
		new RanchUI(player).open();
	}

	@Override
	public Optional<Text> getDescription() {
		return Optional.empty();
	}

	@Override
	public CommandElement[] getArguments() {
		return new CommandElement[0];
	}

	@Override
	public DaycareCmdExecutor[] getSubcommands() {
		return new DaycareCmdExecutor[] {
				new DaycareAdminCmd(this.plugin)
		};
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if(src instanceof Player) {
			new RanchUI((Player) src).open();
			return CommandResult.success();
		}
		throw new CommandException(Text.of("You must be a player to use this command"));
	}
}
