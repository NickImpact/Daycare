package com.nickimpact.daycare.api.configuration.keys;

import com.nickimpact.daycare.api.configuration.ConfigAdapter;
import com.nickimpact.daycare.api.configuration.ConfigKey;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "of")
public class LongKey implements ConfigKey<Long> {
	private final String path;
	private final long def;

	@Override
	public Long get(ConfigAdapter adapter) {
		return adapter.getLong(path, def);
	}
}
