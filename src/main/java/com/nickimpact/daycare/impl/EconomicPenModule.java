package com.nickimpact.daycare.impl;

import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.api.pens.PenUnlockModule;
import com.nickimpact.daycare.configuration.ConfigKeys;
import org.mariuszgromada.math.mxparser.Expression;
import org.mariuszgromada.math.mxparser.Function;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public class EconomicPenModule implements PenUnlockModule {

	@Override
	public Text getRequirement(int id) {
		return Text.of(TextColors.RED, "Unlock Price: ", TextColors.GREEN, DaycarePlugin.getInstance().getEconomy().getDefaultCurrency().format(this.calcPrice(id)));
	}

	@Override
	public boolean canUnlock(UUID uuid, int id) {
		Optional<UniqueAccount> account = DaycarePlugin.getInstance().getEconomy().getOrCreateAccount(uuid);
		return account.filter(uniqueAccount -> uniqueAccount.getBalance(DaycarePlugin.getInstance().getEconomy().getDefaultCurrency()).compareTo(this.calcPrice(id)) >= 0).isPresent();

	}

	@Override
	public boolean process(UUID uuid, int id) {
		Optional<UniqueAccount> account = DaycarePlugin.getInstance().getEconomy().getOrCreateAccount(uuid);
		return account.filter(uniqueAccount -> uniqueAccount.withdraw(DaycarePlugin.getInstance().getEconomy().getDefaultCurrency(), this.calcPrice(id), Sponge.getCauseStackManager().getCurrentCause()).getResult() == ResultType.SUCCESS).isPresent();

	}

	private BigDecimal calcPrice(int id) {
		Function function = new Function("P(b, i, p) = " + DaycarePlugin.getInstance().getConfig().get(ConfigKeys.PEN_PRICE_EQUATION));
		Expression expression = new Expression(String.format(
				"P(%.2f, %.2f, %d)",
				DaycarePlugin.getInstance().getConfig().get(ConfigKeys.BASE_PEN_PRICE),
				DaycarePlugin.getInstance().getConfig().get(ConfigKeys.INCREMENT_PEN_PRICE),
				id
		), function);
		return new BigDecimal(expression.calculate());
	}
}
