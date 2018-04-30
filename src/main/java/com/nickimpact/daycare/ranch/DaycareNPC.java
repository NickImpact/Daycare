package com.nickimpact.daycare.ranch;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class DaycareNPC {

	private final UUID uuid;
	private final String name;
}
