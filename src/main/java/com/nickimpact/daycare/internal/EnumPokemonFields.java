package com.nickimpact.daycare.internal;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.attacks.specialAttacks.basic.HiddenPower;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.EVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.IVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.extraStats.LakeTrioStats;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.extraStats.MewStats;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import net.minecraft.item.ItemStack;
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
		if (pokemon.isEgg()) {
			return "Unknown";
		}

		return pokemon.getSpecies().getPokemonName();
	}),
	ABILITY(pokemon -> pokemon.getAbility().getLocalizedName()),
	NATURE(pokemon -> pokemon.getNature().name()),
	GENDER(pokemon -> {
		switch(pokemon.getGender()) {
			case Male:
				return Text.of(TextColors.AQUA, "Male");
			case Female:
				return Text.of(TextColors.LIGHT_PURPLE, "Female");
			default:
				return Text.of(TextColors.WHITE, "None");
		}
	}),
	GENDER_ICON(pokemon -> {
		switch(pokemon.getGender()) {
			case Male:
				return Text.of(TextColors.AQUA, "\u2642");
			case Female:
				return Text.of(TextColors.LIGHT_PURPLE, "\u2640");
			default:
				return Text.EMPTY;
		}
	}),
	SHINY(pokemon -> {
		if (!pokemon.isShiny())
			return Text.EMPTY;

		return Text.of(TextColors.GRAY, "(", TextColors.GOLD, "Shiny", TextColors.GRAY, ")");
	}),
	HELD_ITEM(pokemon -> {
		ItemStack item = pokemon.getHeldItem();
		if(item == ItemStack.EMPTY) {
			return "Nothing";
		}

		return pokemon.getHeldItem().getDisplayName();
	}),
	GROWTH(pokemon -> pokemon.getGrowth().name()),
	LEVEL(Pokemon::getLevel),
	FORM(Pokemon::getForm),
	CLONES(pokemon -> {
		if (pokemon.getSpecies() == EnumSpecies.Mew) {
			return ((MewStats) pokemon.getExtraStats()).numCloned;
		}
		return 0;
	}),
	ENCHANTS(pokemon -> {
		switch (pokemon.getSpecies()) {
			case Mesprit:
			case Azelf:
			case Uxie:
				return ((LakeTrioStats) pokemon.getExtraStats()).numEnchanted;
			default:
				return 0;
		}
	}),
	EV_PERCENT(pokemon -> new DecimalFormat("#0.##").format(totalEVs(pokemon.getStats().evs) / 510.0 * 100) + "%"),
	IV_PERCENT(pokemon -> new DecimalFormat("#0.##").format(totalIVs(pokemon.getStats().ivs) / 186.0 * 100) + "%"),
	EV_TOTAL(pokemon -> (int) totalEVs(pokemon.getStats().evs)),
	IV_TOTAL(pokemon -> (int) totalIVs(pokemon.getStats().ivs)),
	NICKNAME(pokemon -> TextSerializers.LEGACY_FORMATTING_CODE.deserialize(pokemon.getNickname() != null  && !pokemon.getNickname().isEmpty() ? pokemon.getNickname() : pokemon.getSpecies().getPokemonName())),
	EV_HP(pokemon -> pokemon.getStats().evs.hp),
	EV_ATK(pokemon -> pokemon.getStats().evs.attack),
	EV_DEF(pokemon -> pokemon.getStats().evs.defence),
	EV_SPATK(pokemon -> pokemon.getStats().evs.specialAttack),
	EV_SPDEF(pokemon -> pokemon.getStats().evs.specialDefence),
	EV_SPEED(pokemon -> pokemon.getStats().evs.speed),
	IV_HP(pokemon -> pokemon.getStats().ivs.hp),
	IV_ATK(pokemon -> pokemon.getStats().ivs.attack),
	IV_DEF(pokemon -> pokemon.getStats().ivs.defence),
	IV_SPATK(pokemon -> pokemon.getStats().ivs.specialAttack),
	IV_SPDEF(pokemon -> pokemon.getStats().ivs.specialDefence),
	IV_SPEED(pokemon -> pokemon.getStats().ivs.speed),
	SPECIAL_TEXTURE(pokemon -> {
		try {
			return pokemon.getSpecialTexture().name();
		} catch (Exception e) {
			return "";
		}
	}),
	HIDDEN_POWER(pokemon -> HiddenPower.getHiddenPowerType(pokemon.getStats().ivs)),
	MOVES_1(pokemon -> pokemon.getMoveset().attacks[0].baseAttack.getLocalizedName()),
	MOVES_2(pokemon -> pokemon.getMoveset().attacks[1].baseAttack.getLocalizedName()),
	MOVES_3(pokemon -> pokemon.getMoveset().attacks[2].baseAttack.getLocalizedName()),
	MOVES_4(pokemon -> pokemon.getMoveset().attacks[3].baseAttack.getLocalizedName());

	public final Function<Pokemon, Object> function;

	private EnumPokemonFields(Function<Pokemon, Object> function) {
		this.function = function;
	}

	private static double totalEVs(EVStore evs) {
		return evs.hp + evs.attack + evs.defence + evs.specialAttack + evs.specialDefence + evs.speed;
	}

	private static double totalIVs(IVStore ivs) {
		return ivs.hp + ivs.attack + ivs.defence + ivs.specialAttack + ivs.specialDefence + ivs.speed;
	}
}
