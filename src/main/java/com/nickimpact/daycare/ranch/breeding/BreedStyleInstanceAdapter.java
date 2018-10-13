package com.nickimpact.daycare.ranch.breeding;

import com.google.gson.Gson;
import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.api.breeding.BreedStyle;
import com.nickimpact.impactor.json.Adapter;
import com.nickimpact.impactor.json.Registry;

public class BreedStyleInstanceAdapter extends Adapter<BreedStyle.Instance> {

	public BreedStyleInstanceAdapter() {
		super(DaycarePlugin.getInstance());
	}

	@Override
	public Gson getGson() {
		return DaycarePlugin.prettyGson;
	}

	@Override
	public Registry getRegistry() {
		return DaycarePlugin.getInstance().getService().getBreedStyleRegistry().getInstanceRegistry();
	}
}
