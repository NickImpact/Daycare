package com.nickimpact.daycare.common.storage.implementation.sql;

public enum PenSlot {

	FIRST("slot1"),
	SECOND("slot2");

	private String key;

	PenSlot(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
}
