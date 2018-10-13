package com.nickimpact.daycare.impl;

import com.nickimpact.daycare.api.DaycareService;
import com.nickimpact.daycare.api.breeding.BreedStyleRegistry;

public class DaycareServiceImpl implements DaycareService {

	private BreedStyleRegistry bsr = new BreedStyleRegistry();

	@Override
	public BreedStyleRegistry getBreedStyleRegistry() {
		return bsr;
	}
}
