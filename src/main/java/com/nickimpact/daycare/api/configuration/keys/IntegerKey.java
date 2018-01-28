package com.nickimpact.daycare.api.configuration.keys;

import com.nickimpact.daycare.api.configuration.ConfigAdapter;
import com.nickimpact.daycare.api.configuration.ConfigKey;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "of")
public class IntegerKey implements ConfigKey<Integer> {
	private final String path;
	private final int def;

	@Override
	public Integer get(ConfigAdapter adapter) {
		return adapter.getInt(path, def);
	}
}
