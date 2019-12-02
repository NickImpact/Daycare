package com.nickimpact.daycare.common.json;

import com.nickimpact.daycare.api.IDaycarePlugin;
import com.nickimpact.daycare.api.pens.Pen;
import com.nickimpact.impactor.api.json.Adapter;
import com.nickimpact.impactor.api.json.Registry;

public class PenAdapter extends Adapter<Pen> {

	private Registry<Pen> registry;

	public PenAdapter(IDaycarePlugin plugin) {
		super(plugin);
		this.registry = new Registry<>(plugin);
	}

	@Override
	public Registry<Pen> getRegistry() {
		return this.registry;
	}
}
