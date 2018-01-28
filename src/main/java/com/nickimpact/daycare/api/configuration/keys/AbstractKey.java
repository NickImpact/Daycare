package com.nickimpact.daycare.api.configuration.keys;

import com.nickimpact.daycare.api.configuration.ConfigAdapter;
import com.nickimpact.daycare.api.configuration.ConfigKey;
import lombok.AllArgsConstructor;

import java.util.function.Function;

@AllArgsConstructor(staticName = "of")
public class AbstractKey<T> implements ConfigKey<T> {
	private final Function<ConfigAdapter, T> function;

	@Override
	public T get(ConfigAdapter adapter) {
		return function.apply(adapter);
	}
}
