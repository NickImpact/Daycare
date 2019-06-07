package com.nickimpact.daycare.common.json;

import com.google.gson.Gson;
import com.nickimpact.daycare.api.IDaycarePlugin;
import com.nickimpact.daycare.api.pens.Ranch;
import com.nickimpact.impactor.api.json.Adapter;
import com.nickimpact.impactor.api.json.Registry;

public class RanchAdapter extends Adapter<Ranch> {

	private Registry<Ranch> registry;

	public RanchAdapter(IDaycarePlugin plugin) {
		super(plugin);
		this.registry = new Registry<>(plugin);
	}

	@Override
	protected Gson getGson() {
		return ((IDaycarePlugin) plugin).getGson();
	}

	@Override
	public Registry<Ranch> getRegistry() {
		return this.registry;
	}

}
