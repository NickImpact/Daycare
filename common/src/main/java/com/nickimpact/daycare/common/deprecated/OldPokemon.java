package com.nickimpact.daycare.common.deprecated;

import lombok.Getter;

import java.util.Date;

@Getter
@Deprecated
public class OldPokemon {

	private String json;

	private int startLvl;
	private int gainedLvls;
	private Date lastLvl;

}
