package com.nickimpact.daycare.common.json;

import com.nickimpact.daycare.api.IDaycarePlugin;
import com.nickimpact.daycare.api.pens.DaycarePokemonWrapper;
import com.nickimpact.impactor.api.json.Adapter;
import com.nickimpact.impactor.api.json.Registry;

public class PokemonWrapperAdapter extends Adapter<DaycarePokemonWrapper> {

	private Registry<DaycarePokemonWrapper> registry;

	public PokemonWrapperAdapter(IDaycarePlugin plugin) {
		super(plugin);
		this.registry = new Registry<>(plugin);
	}

	@Override
	public Registry<DaycarePokemonWrapper> getRegistry() {
		return this.registry;
	}
}
