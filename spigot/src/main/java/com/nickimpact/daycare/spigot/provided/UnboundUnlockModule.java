package com.nickimpact.daycare.spigot.provided;

import com.nickimpact.daycare.api.pens.PenUnlockModule;

import java.util.UUID;

public class UnboundUnlockModule implements PenUnlockModule {

	@Override
	public String getRequirement(int pen) {
		return null;
	}

	@Override
	public boolean canPay(UUID owner, int pen) {
		return false;
	}

	@Override
	public boolean pay(UUID owner, int pen) {
		return false;
	}
	
}
