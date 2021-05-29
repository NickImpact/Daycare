package com.nickimpact.daycare.common.json;

import com.nickimpact.daycare.api.IDaycarePlugin;
import com.nickimpact.daycare.api.pens.Ranch;
import net.impactdev.impactor.api.json.Adapter;
import net.impactdev.impactor.api.json.Registry;

public class RanchAdapter extends Adapter<Ranch> {

	private Registry<Ranch> registry;

	public RanchAdapter(IDaycarePlugin plugin) {
		super(plugin);
		this.registry = new Registry<>(plugin);
	}

	@Override
	public Registry<Ranch> getRegistry() {
		return this.registry;
	}

}
