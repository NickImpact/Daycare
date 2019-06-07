package com.nickimpact.daycare.common.deprecated;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import lombok.Getter;

import java.util.Date;

@Getter
@Deprecated
public class OldPen {

	private OldPokemon slot1;
	private OldPokemon slot2;
	private OldPokemon egg;

	private Date dateUnlocked;
	private boolean unlocked;

}
