package com.nickimpact.daycare.api.commands;

import com.google.common.collect.Lists;
import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.PluginInfo;
import com.nickimpact.daycare.api.commands.annotations.AdminCmd;
import com.nickimpact.daycare.api.commands.annotations.CommandAliases;
import com.nickimpact.daycare.api.commands.annotations.Parent;
import com.nickimpact.daycare.utils.MessageUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;

/**
 * This class will represent a command built off the Sponge Command API, and will
 * serve as the basis for commands as they are written for a cleaner and much easier
 * registration process.
 *
 * @author NickImpact
 */
public abstract class SpongeCommand implements CommandExecutor
{
    private String basePermission;

    public SpongeCommand()
    {
        if(!hasProperAnnotations())
        {

            DaycarePlugin.getInstance().getConsole().ifPresent(console -> console.sendMessages(
                    Text.of(PluginInfo.ERROR_PREFIX, "======= Invalid Command Structure ======="),
                    Text.of(PluginInfo.ERROR_PREFIX, "Executor: ", TextColors.RED, this.getClass().getSimpleName()),
                    Text.of(PluginInfo.ERROR_PREFIX, "Reason: ", TextColors.RED, "Missing header annotation"),
                    Text.of(PluginInfo.ERROR_PREFIX, "=========================================")
            ));

        }
    }

    public boolean hasProperAnnotations()
    {
        return this.getClass().isAnnotationPresent(CommandAliases.class);
    }

    public List<String> getAllAliases()
    {
        return Lists.newArrayList(this.getClass().getAnnotation(CommandAliases.class).value());
    }

    public abstract CommandElement[] getArgs();

    public abstract Text getDescription();

    public abstract Text getUsage();

    public abstract SpongeCommand[] getSubCommands();

    public CommandSpec getCommandSpec()
    {
    	CommandSpec.Builder cb = CommandSpec.builder();
    	if(!(this instanceof SpongeSubCommand)) {
		    this.basePermission = formPermission(null);
	    }
	    cb.permission(this.basePermission);

        SpongeCommand[] subCmds = getSubCommands();
        HashMap<List<String>, CommandSpec> subCommands = new HashMap<>();
        if (subCmds != null && subCmds.length > 0)
            for (SpongeCommand cmd : subCmds)
            {
                cmd.basePermission = formPermission(cmd.getClass().isAnnotationPresent(Parent.class) ? cmd.getAllAliases().get(0) : null);
                subCommands.put(cmd.getAllAliases(), cmd.getCommandSpec());
            }

        CommandElement[] args = getArgs();
        if (args == null || args.length == 0)
            args = new CommandElement[]{GenericArguments.none()};

        return cb.children(subCommands)
                .description(getDescription())
                .executor(this)
                .arguments(args)
                .build();
    }

    public void register()
    {
        try
        {
            if(this.hasProperAnnotations()) {
                Sponge.getCommandManager().register(DaycarePlugin.getInstance(), getCommandSpec(), getAllAliases());
            }
        }
        catch (IllegalArgumentException iae)
        {
            iae.printStackTrace();
        }
    }

    public boolean testPermissionSuffix(CommandSource src, String suffix) {
        return src.hasPermission(this.basePermission + "." + suffix);
    }

    public void sendCommandUsage(CommandSource src)
    {
        src.sendMessage(getDescription());
    }

    private String formPermission(@Nullable String parent) {
    	String perm = PluginInfo.ID + ".command.";
    	if((this.getClass().isAnnotationPresent(AdminCmd.class))) {
    		perm += "admin.";
	    }

	    if(parent != null) {
    		perm += parent + ".";
	    }
	    perm += getAllAliases().get(0);

    	return perm;
    }
}
