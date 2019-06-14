package com.nickimpact.daycare.provided;

import com.nickimpact.daycare.SpigotDaycarePlugin;
import com.nickimpact.daycare.api.configuration.ConfigKeys;
import com.nickimpact.daycare.api.pens.PenUnlockModule;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.mariuszgromada.math.mxparser.Expression;
import org.mariuszgromada.math.mxparser.Function;

import java.util.UUID;

public class EconomicModule implements PenUnlockModule {

    @Override
    public String getRequirement(int pen) {
        return SpigotDaycarePlugin.getInstance().getEconomy().format(this.calcPrice(pen));
    }

    @Override
    public boolean canPay(UUID owner, int pen) {
        return SpigotDaycarePlugin.getInstance().getEconomy().getBalance(Bukkit.getOfflinePlayer(owner)) >= this.calcPrice(pen);
    }

    @Override
    public boolean pay(UUID owner, int pen) {
        return SpigotDaycarePlugin.getInstance().getEconomy().withdrawPlayer(Bukkit.getOfflinePlayer(owner), this.calcPrice(pen)).type == EconomyResponse.ResponseType.SUCCESS;
    }

    private double calcPrice(int id) {
        Function function = new Function("P(b, i, p) = " + SpigotDaycarePlugin.getInstance().getConfiguration().get(ConfigKeys.PEN_PRICE_EQUATION));
        Expression expression = new Expression(String.format(
                "P(%.2f, %.2f, %d)",
                SpigotDaycarePlugin.getInstance().getConfiguration().get(ConfigKeys.BASE_PEN_PRICE),
                SpigotDaycarePlugin.getInstance().getConfiguration().get(ConfigKeys.INCREMENT_PEN_PRICE),
                id
        ), function);
        return expression.calculate();
    }
}
