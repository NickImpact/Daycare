package com.nickimpact.daycare.common.deprecated;

import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import lombok.Getter;

import java.util.Date;

@Getter
@Deprecated
public class OldPokemon {

	private String json;

	private Gender gender;
	private int startLvl;
	private int gainedLvls;
	private Date lastLvl;

}
