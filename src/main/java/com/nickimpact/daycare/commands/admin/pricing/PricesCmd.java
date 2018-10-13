package com.nickimpact.daycare.commands.admin.pricing;

import com.google.common.collect.Lists;
import com.nickimpact.daycare.DaycareInfo;
import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.configuration.ConfigKeys;
import com.nickimpact.daycare.configuration.MsgConfigKeys;
import com.nickimpact.daycare.utils.MessageUtils;
import com.nickimpact.impactor.api.commands.SpongeCommand;
import com.nickimpact.impactor.api.commands.SpongeSubCommand;
import com.nickimpact.impactor.api.commands.annotations.Aliases;
import com.nickimpact.impactor.api.commands.annotations.Permission;
import com.nickimpact.impactor.api.plugins.SpongePlugin;
import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;
import org.mariuszgromada.math.mxparser.Function;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

@Aliases({"prices"})
@Permission(admin = true)
public class PricesCmd extends SpongeSubCommand {

	private static PriceWrapper wrapper;

	public PricesCmd(SpongePlugin plugin) {
		super(plugin);
	}

	private final Text base = Text.of("base");
	private final Text increment = Text.of("increment");

	private final Text equation = Text.of("equation");

	@Override
	public CommandElement[] getArgs() {
		return new CommandElement[]{
				GenericArguments.doubleNum(base),
				GenericArguments.doubleNum(increment),
				GenericArguments.remainingJoinedStrings(equation)
		};
	}

	@Override
	public Text getDescription() {
		return Text.of();
	}

	@Override
	public Text getUsage() {
		return Text.of("/daycare admin prices");
	}

	@Override
	public SpongeCommand[] getSubCommands() {
		return new SpongeCommand[0];
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if(DaycarePlugin.getInstance().getEconomy() == null) {
			throw new CommandException(MessageUtils.fetchMsg(src, MsgConfigKeys.CMD_PRICING_NO_ECONOMY_SERVICE));
		}

		List<Text> prices = Lists.newArrayList();
		PaginationList.Builder builder = PaginationList.builder()
				.header(Text.of())
				.title(Text.of())
				.linesPerPage(10);

		String exp = args.<String>getOne(equation).get();
		if(!exp.contains("b") || !exp.contains("i") || !exp.contains("p")) {
			throw new CommandException(Text.of("Your equation must make use of the three variables: b = base, p = increment, i = pen-ID - 1"));
		}
		double ba = args.<Double>getOne(base).get();
		double inc = args.<Double>getOne(increment).get();

		wrapper = new PriceWrapper(ba, inc, exp);

		boolean header = false;
		for(int k = 0; k < 14; k++) {
			Function function = new Function("P(b, i, p) = " + args.<String>getOne(equation).get());
			Expression expression = new Expression(String.format(
					"P(%.2f, %.2f, %d)",
					args.<Double>getOne(base).get(),
					args.<Double>getOne(increment).get(),
					k
			), function);
			if(!header) {
				header = true;
				builder.header(
						Text.of(
								TextColors.GRAY, "Equation: ", TextColors.GREEN, function.getFunctionExpressionString(), Text.NEW_LINE,
								TextColors.GRAY, "base = ", TextColors.GREEN, DaycarePlugin.getInstance().getConfig().get(ConfigKeys.BASE_PEN_PRICE), Text.NEW_LINE,
								TextColors.GRAY, "increment = ", TextColors.GREEN, DaycarePlugin.getInstance().getConfig().get(ConfigKeys.INCREMENT_PEN_PRICE), Text.NEW_LINE,
								TextColors.GRAY, "pen = ", TextColors.GREEN, "pen id - 1 (Zero based)", Text.NEW_LINE,
								Text.builder("Click here to apply equation and parameters")
										.color(TextColors.YELLOW)
										.onClick(TextActions.executeCallback(source -> {
											wrapper.apply();
											source.sendMessage(Text.of(DaycareInfo.PREFIX, "Parameters applied!"));
										}))
										.onHover(TextActions.showText(Text.of(TextColors.GRAY, "Click me! " , TextColors.RED, "Bidoof commands it!")))
										.build(), Text.NEW_LINE
						));
			}
			prices.add(Text.of(
					TextColors.YELLOW, "Pen ", k + 1, " = ", TextColors.GREEN,
					DaycarePlugin.getInstance().getEconomy().getDefaultCurrency().format(new BigDecimal(expression.calculate()))
			));
		}

		builder.contents(prices).build().sendTo(src);
		return CommandResult.success();
	}
}
