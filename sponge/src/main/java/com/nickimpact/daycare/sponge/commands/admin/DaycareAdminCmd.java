package com.nickimpact.daycare.sponge.commands.admin;

import com.nickimpact.daycare.sponge.SpongeDaycarePlugin;
import com.nickimpact.daycare.sponge.commands.DaycareCmdExecutor;
import com.nickimpact.daycare.sponge.commands.annotations.Alias;
import com.nickimpact.daycare.sponge.commands.annotations.Permission;
import com.nickimpact.daycare.sponge.configuration.MsgConfigKeys;
import com.nickimpact.daycare.sponge.utils.TextParser;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Optional;

@Alias("admin")
@Permission("daycare.command.admin.base")
public class DaycareAdminCmd extends DaycareCmdExecutor {

    public DaycareAdminCmd(SpongeDaycarePlugin plugin) {
        super(plugin);
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
                new AddNPC(this.plugin),
                new DeleteNPC(this.plugin)
        };
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        return CommandResult.success();
    }

    @Alias("addNPC")
    @Permission("daycare.command.admin.addnpc")
    public static class AddNPC extends DaycareCmdExecutor {

        private final Text NPC_NAME = Text.of("npc_name");

        public AddNPC(SpongeDaycarePlugin plugin) {
            super(plugin);
        }

        @Override
        public Optional<Text> getDescription() {
            return Optional.empty();
        }

        @Override
        public CommandElement[] getArguments() {
            return new CommandElement[] {
                    GenericArguments.remainingJoinedStrings(NPC_NAME)
            };
        }

        @Override
        public DaycareCmdExecutor[] getSubcommands() {
            return new DaycareCmdExecutor[0];
        }

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            if(src instanceof Player) {
                Player player = (Player) src;
                SpongeDaycarePlugin.getSpongeInstance().getService().getNPCManager().addNPCAdder(player.getUniqueId(), args.<String>getOne(NPC_NAME).get());
                player.sendMessage(TextParser.parse(TextParser.read(MsgConfigKeys.CMD_ADDNPC_RIGHTCLICK_NOTICE)));
                return CommandResult.success();
            }

            throw new CommandException(Text.of("You must be a player to use this command"));
        }
    }

    @Alias("deletenpc")
    @Permission("daycare.commands.admin.deletenpc")
    public static class DeleteNPC extends DaycareCmdExecutor {

        public DeleteNPC(SpongeDaycarePlugin plugin) {
            super(plugin);
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
            return new DaycareCmdExecutor[0];
        }

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            if(src instanceof Player) {
                Player player = (Player) src;
                SpongeDaycarePlugin.getSpongeInstance().getService().getNPCManager().removeRemover(player.getUniqueId());
                return CommandResult.success();
            }

            throw new CommandException(Text.of("You must be a player to use this command"));
        }
    }
}
