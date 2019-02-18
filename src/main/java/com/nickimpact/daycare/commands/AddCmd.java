package com.nickimpact.daycare.commands;

import com.google.common.collect.Maps;
import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.DaycareInfo;
import com.nickimpact.daycare.commands.arguments.PokemonPartyArg;
import com.nickimpact.daycare.ranch.Pokemon;
import com.nickimpact.daycare.ranch.Ranch;
import com.nickimpact.impactor.api.commands.SpongeCommand;
import com.nickimpact.impactor.api.commands.SpongeSubCommand;
import com.nickimpact.impactor.api.commands.annotations.Aliases;
import com.nickimpact.impactor.api.commands.annotations.Permission;
import com.nickimpact.impactor.api.commands.elements.PositiveIntegerElement;
import com.nickimpact.impactor.api.plugins.SpongePlugin;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import io.github.nucleuspowered.nucleus.api.exceptions.NucleusException;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * (Some note will appear here)
 *
 * @author NickImpact (Nick DeGruccio)
 */
@Aliases({"add"})
@Permission()
public class AddCmd extends SpongeSubCommand {

	private final Text partySlot = Text.of("slot");

	public AddCmd(SpongePlugin plugin) {
		super(plugin);
	}

	@Override
	public CommandElement[] getArgs() {
		return new CommandElement[] {
				new PokemonPartyArg(partySlot),
				new PositiveIntegerElement(Text.of("pen"), false)
		};
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
			Player player = (Player)src;
			Optional<Ranch> ranch = DaycarePlugin.getInstance().getRanches().stream().filter(r -> r.getOwnerUUID().equals(player.getUniqueId())).findAny();
			if(ranch.isPresent()) {
				Ranch r = ranch.get();

				PlayerPartyStorage party = Pixelmon.storageManager.getParty(player.getUniqueId());
				final int slot = args.<Integer>getOne(partySlot).get();
				final int pen = args.<Integer>getOne(Text.of("pen")).get() - 1;
				if(r.getPen(pen).isFull()) {
					throw new CommandException(Text.of("That pen is full..."));
				}
				com.pixelmonmod.pixelmon.api.pokemon.Pokemon pokemon = party.get(slot);
				if(pokemon == null) {
					throw new CommandException(Text.of("That slot is empty..."));
				}

				party.set(slot, null);
				Pokemon holder = new Pokemon(pokemon);
				r.addToPen(holder, pen);

				Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
				tokens.put("pen", cs -> Optional.of(Text.of(pen)));

				Map<String, Object> variables = Maps.newHashMap();
				variables.put("dummy", pokemon);
				variables.put("dummy2", holder);
				try {
					player.sendMessage(DaycarePlugin.getInstance().getTextParsingUtils().parse(
						"{{daycare_prefix}} &7Successfully deposited your {{pokemon}} into pen {{pen}}",
							player,
							tokens,
							variables
					));
				} catch (NucleusException e) {
					player.sendMessage(Text.of(
							DaycareInfo.PREFIX, TextColors.GRAY, "Sucessfully deposited your ", TextColors.GREEN,
							pokemon.getDisplayName(), TextColors.GRAY, " into pen ", TextColors.YELLOW, pen
					));
				}

				return CommandResult.success();

			}

			throw new CommandException(Text.of("Couldn't locate a ranch for you..."));
		}

		throw new CommandException(Text.of("You must be a player to use this command..."));
	}
}
