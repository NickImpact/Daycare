package com.nickimpact.daycare.api.events;

public interface Cancelable {

	boolean isCancelled();

	void setCancelled(boolean flag);

}
