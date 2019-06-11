package com.nickimpact.daycare.provided;

import com.nickimpact.daycare.SpongeDaycarePlugin;
import com.nickimpact.daycare.api.configuration.ConfigKeys;
import com.nickimpact.daycare.api.pens.PenUnlockModule;
import org.mariuszgromada.math.mxparser.Expression;
import org.mariuszgromada.math.mxparser.Function;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public class EconomicModule implements PenUnlockModule {

	@Override
	public String getRequirement(int pen) {
		return SpongeDaycarePlugin.getSpongeInstance().getEconomy().getDefaultCurrency().format(this.calcPrice(pen)).toPlain();
	}

	@Override
	public boolean canPay(UUID owner, int pen) {
		Optional<UniqueAccount> account = SpongeDaycarePlugin.getSpongeInstance().getEconomy().getOrCreateAccount(owner);
		return account.filter(uniqueAccount -> uniqueAccount.getBalance(SpongeDaycarePlugin.getSpongeInstance().getEconomy().getDefaultCurrency()).compareTo(this.calcPrice(pen)) >= 0).isPresent();
	}

	@Override
	public boolean pay(UUID owner, int pen) {
		Optional<UniqueAccount> account = SpongeDaycarePlugin.getSpongeInstance().getEconomy().getOrCreateAccount(owner);
		return account.filter(uniqueAccount -> uniqueAccount.withdraw(SpongeDaycarePlugin.getSpongeInstance().getEconomy().getDefaultCurrency(), this.calcPrice(pen), Sponge.getCauseStackManager().getCurrentCause()).getResult() == ResultType.SUCCESS).isPresent();

	}

	private BigDecimal calcPrice(int id) {
		Function function = new Function("P(b, i, p) = " + SpongeDaycarePlugin.getSpongeInstance().getConfig().get(ConfigKeys.PEN_PRICE_EQUATION));
		Expression expression = new Expression(String.format(
				"P(%.2f, %.2f, %d)",
				SpongeDaycarePlugin.getSpongeInstance().getConfig().get(ConfigKeys.BASE_PEN_PRICE),
				SpongeDaycarePlugin.getSpongeInstance().getConfig().get(ConfigKeys.INCREMENT_PEN_PRICE),
				id
		), function);
		return new BigDecimal(expression.calculate());
	}
}
