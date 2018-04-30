package com.nickimpact.daycare.commands.admin.pricing;

import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.configuration.ConfigKeys;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
class PriceWrapper {

	private double base;
	private double increment;
	private String equation;

	void apply() {
		DaycarePlugin.getInstance().getConfig().set(ConfigKeys.BASE_PEN_PRICE, base);
		DaycarePlugin.getInstance().getConfig().set(ConfigKeys.INCREMENT_PEN_PRICE, increment);
		DaycarePlugin.getInstance().getConfig().set(ConfigKeys.PEN_PRICE_EQUATION, equation);
	}

}
