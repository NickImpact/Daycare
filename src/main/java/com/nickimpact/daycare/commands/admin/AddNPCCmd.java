package com.nickimpact.daycare.commands.admin;

import com.google.common.collect.Maps;
import com.nickimpact.daycare.DaycareInfo;
import com.nickimpact.daycare.configuration.MsgConfigKeys;
import com.nickimpact.daycare.utils.MessageUtils;
import com.nickimpact.impactor.api.commands.SpongeCommand;
import com.nickimpact.impactor.api.commands.SpongeSubCommand;
import com.nickimpact.impactor.api.commands.annotations.Aliases;
import com.nickimpact.impactor.api.commands.annotations.Permission;
import com.nickimpact.impactor.api.plugins.SpongePlugin;
import lombok.Getter;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Map;
import java.util.UUID;

@Aliases({"setnpc", "npc"})
@Permission(admin = true)
public class AddNPCCmd extends SpongeSubCommand {

	@Getter private static Map<UUID, String> adding = Maps.newHashMap();

	private static final Text NAME = Text.of("name");

	public AddNPCCmd(SpongePlugin plugin) {
		super(plugin);
	}

	@Override
	public CommandElement[] getArgs() {
		return new CommandElement[]{
				GenericArguments.remainingJoinedStrings(NAME)
		};
	}

	@Override
	public Text getDescription() {
		return Text.of("Adds a NPC to the representable Daycare NPCs");
	}

	@Override
	public Text getUsage() {
		return Text.of("/daycare admin setnpc");
	}

	@Override
	public SpongeCommand[] getSubCommands() {
		return new SpongeCommand[0];
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if(src instanceof Player) {
			adding.put(((Player) src).getUniqueId(), args.<String>getOne(NAME).get());
			src.sendMessage(MessageUtils.fetchMsg(src, MsgConfigKeys.CMD_ADDNPC_RIGHTCLICK_NOTICE));
			return CommandResult.success();
		}
		throw new CommandException(MessageUtils.fetchAndParseMsg(src, MsgConfigKeys.CMD_NON_PLAYER, null, null));
	}
}
