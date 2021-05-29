package com.nickimpact.daycare.common.json;

import com.nickimpact.daycare.api.IDaycarePlugin;
import com.nickimpact.daycare.api.pens.Pen;
import net.impactdev.impactor.api.json.Adapter;
import net.impactdev.impactor.api.json.Registry;

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
