package com.nickimpact.daycare.api.pens;

import java.util.UUID;

public interface PenUnlockModule {

	String getRequirement(int pen);

	boolean canPay(UUID owner, int pen);

	boolean pay(UUID owner, int pen);

}
