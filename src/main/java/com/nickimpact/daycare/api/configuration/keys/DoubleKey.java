package com.nickimpact.daycare.api.configuration.keys;

import com.nickimpact.daycare.api.configuration.ConfigAdapter;
import com.nickimpact.daycare.api.configuration.ConfigKey;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "of")
public class DoubleKey implements ConfigKey<Double> {
	private final String path;
	private final double def;

	@Override
	public Double get(ConfigAdapter adapter) {
		return adapter.getDouble(path, def);
	}
}
