package com.nickimpact.daycare.api.configuration.keys;

import com.google.common.collect.ImmutableList;
import com.nickimpact.daycare.api.configuration.ConfigAdapter;
import com.nickimpact.daycare.api.configuration.ConfigKey;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@AllArgsConstructor(staticName = "of")
public class ListKey implements ConfigKey<List<String>> {
	private final String path;
	private final List<String> def;

	@Override
	public List<String> get(ConfigAdapter adapter) {
		return ImmutableList.copyOf(adapter.getList(path, def));
	}
}
