package com.nickimpact.daycare.internal;

import com.pixelmonmod.pixelmon.battles.attacks.specialAttacks.basic.HiddenPower;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.EVsStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.IVStore;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.text.DecimalFormat;
import java.util.function.Function;

/**
 * (Some note will appear here)
 *
 * @author NickImpact (Nick DeGruccio)
 */
public enum EnumPokemonFields {

	NAME(pokemon -> {
		if (pokemon.isEgg) {
			return "Unknown";
		}

		return pokemon.getName();
	}),
	ABILITY(pokemon -> pokemon.getAbility().getName()),
	NATURE(pokemon -> pokemon.getNature().name()),
	GENDER(pokemon -> pokemon.getGender().name()),
	SHINY(pokemon -> {
		if (!pokemon.getIsShiny())
			return Text.EMPTY;

		return Text.of(TextColors.GRAY, "(", TextColors.GOLD, "Shiny", TextColors.GRAY, ")");
	}),

	GROWTH(pokemon -> pokemon.getGrowth().name()),
	LEVEL(pokemon -> pokemon.getLvl().getLevel()),
	FORM(EntityPixelmon::getForm),
	CLONES(pokemon -> {
		switch (pokemon.getSpecies()) {
			case Mew:
				return pokemon.getEntityData().getShort(NbtKeys.STATS_NUM_CLONED);
			default:
				return 0;
		}
	}),
	EV_PERCENT(pokemon -> new DecimalFormat("#0.##").format(totalEVs(pokemon.stats.EVs) / 510.0 * 100) + "%"),
	IV_PERCENT(pokemon -> new DecimalFormat("#0.##").format(totalIVs(pokemon.stats.IVs) / 186.0 * 100) + "%"),
	EV_TOTAL(pokemon -> (int) totalEVs(pokemon.stats.EVs)),
	IV_TOTAL(pokemon -> (int) totalIVs(pokemon.stats.IVs)),
	NICKNAME(pokemon -> TextSerializers.LEGACY_FORMATTING_CODE.deserialize(pokemon.getNickname())),
	EV_HP(pokemon -> pokemon.stats.EVs.HP),
	EV_ATK(pokemon -> pokemon.stats.EVs.Attack),
	EV_DEF(pokemon -> pokemon.stats.EVs.Defence),
	EV_SPATK(pokemon -> pokemon.stats.EVs.SpecialAttack),
	EV_SPDEF(pokemon -> pokemon.stats.EVs.SpecialDefence),
	EV_SPEED(pokemon -> pokemon.stats.EVs.Speed),
	IV_HP(pokemon -> pokemon.stats.IVs.HP),
	IV_ATK(pokemon -> pokemon.stats.IVs.Attack),
	IV_DEF(pokemon -> pokemon.stats.IVs.Defence),
	IV_SPATK(pokemon -> pokemon.stats.IVs.SpAtt),
	IV_SPDEF(pokemon -> pokemon.stats.IVs.SpDef),
	IV_SPEED(pokemon -> pokemon.stats.IVs.Speed),
	HALLOWEEN(pokemon -> pokemon.getSpecialTexture() == 2 ? "Halloween" : ""),
	ROASTED(pokemon -> pokemon.getSpecialTexture() == 1 ? "Roasted" : ""),
	HIDDEN_POWER(pokemon -> HiddenPower.getHiddenPowerType(pokemon.stats.IVs)),
	MOVES_1(pokemon -> pokemon.getMoveset().attacks[0].baseAttack.getLocalizedName()),
	MOVES_2(pokemon -> pokemon.getMoveset().attacks[1].baseAttack.getLocalizedName()),
	MOVES_3(pokemon -> pokemon.getMoveset().attacks[2].baseAttack.getLocalizedName()),
	MOVES_4(pokemon -> pokemon.getMoveset().attacks[3].baseAttack.getLocalizedName());

	public final Function<EntityPixelmon, Object> function;

	private EnumPokemonFields(Function<EntityPixelmon, Object> function) {
		this.function = function;
	}

	private static double totalEVs(EVsStore evs) {
		return evs.HP + evs.Attack + evs.Defence + evs.SpecialAttack + evs.SpecialDefence + evs.Speed;
	}

	private static double totalIVs(IVStore ivs) {
		return ivs.HP + ivs.Attack + ivs.Defence + ivs.SpAtt + ivs.SpDef + ivs.Speed;
	}
}
