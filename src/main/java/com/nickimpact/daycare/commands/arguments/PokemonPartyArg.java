package com.nickimpact.daycare.commands.arguments;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.commands.elements.BaseCommandElement;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.List;

/**
 * (Some note will appear here)
 *
 * @author NickImpact (Nick DeGruccio)
 */
public class PokemonPartyArg extends BaseCommandElement<Integer> {

	public PokemonPartyArg(@Nullable Text key) {
		super(key);
	}

	@Nullable
	@Override
	protected Integer parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
		try {
			int a = Integer.parseUnsignedInt(args.next());
			if ((a > 0 && a < 7)) {
				return a - 1;
			}

			throw args.createError(Text.of("The argument must be a number between 1-6"));
		} catch (NumberFormatException e) {
			throw args.createError(Text.of("The argument must be a positive number"));
		}
	}

	@Override
	public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
		return Lists.newArrayList();
	}
}
