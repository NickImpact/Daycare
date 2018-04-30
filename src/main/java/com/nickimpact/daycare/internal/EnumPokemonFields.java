package com.nickimpact.daycare.internal;

import com.pixelmonmod.pixelmon.battles.attacks.specialAttacks.basic.HiddenPower;
import com.pixelmonmod.pixelmon.config.PixelmonItemsHeld;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.entities.pixelmon.EnumSpecialTexture;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.EVsStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.IVStore;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
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
		if (pokemon.isEgg) {
			return "Unknown";
		}

		return pokemon.getName();
	}),
	ABILITY(pokemon -> pokemon.getAbility().getName()),
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
		if (!pokemon.getIsShiny())
			return Text.EMPTY;

		return Text.of(TextColors.GRAY, "(", TextColors.GOLD, "Shiny", TextColors.GRAY, ")");
	}),
	HELD_ITEM(pokemon -> {
		ItemStack item = pokemon.heldItem;
		if(item == ItemStack.EMPTY || item == null) {
			return "Nothing";
		}

		return pokemon.getItemHeld().getLocalizedName();
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
	EV_PERCENT(pokemon -> new DecimalFormat("#0.##").format(totalEVs(pokemon.stats.evs) / 510.0 * 100) + "%"),
	IV_PERCENT(pokemon -> new DecimalFormat("#0.##").format(totalIVs(pokemon.stats.ivs) / 186.0 * 100) + "%"),
	EV_TOTAL(pokemon -> (int) totalEVs(pokemon.stats.evs)),
	IV_TOTAL(pokemon -> (int) totalIVs(pokemon.stats.ivs)),
	NICKNAME(pokemon -> TextSerializers.LEGACY_FORMATTING_CODE.deserialize(pokemon.getNickname())),
	EV_HP(pokemon -> pokemon.stats.evs.hp),
	EV_ATK(pokemon -> pokemon.stats.evs.attack),
	EV_DEF(pokemon -> pokemon.stats.evs.defence),
	EV_SPATK(pokemon -> pokemon.stats.evs.specialAttack),
	EV_SPDEF(pokemon -> pokemon.stats.evs.specialDefence),
	EV_SPEED(pokemon -> pokemon.stats.evs.speed),
	IV_HP(pokemon -> pokemon.stats.ivs.HP),
	IV_ATK(pokemon -> pokemon.stats.ivs.Attack),
	IV_DEF(pokemon -> pokemon.stats.ivs.Defence),
	IV_SPATK(pokemon -> pokemon.stats.ivs.SpAtt),
	IV_SPDEF(pokemon -> pokemon.stats.ivs.SpDef),
	IV_SPEED(pokemon -> pokemon.stats.ivs.Speed),
	SPECIAL_TEXTURE(pokemon -> {
		try {
			return EnumSpecialTexture.fromIndex(pokemon.getSpecialTextureIndex()).name();
		} catch (Exception e) {
			return "";
		}
	}),
	HIDDEN_POWER(pokemon -> HiddenPower.getHiddenPowerType(pokemon.stats.ivs)),
	MOVES_1(pokemon -> pokemon.getMoveset().attacks[0].baseAttack.getLocalizedName()),
	MOVES_2(pokemon -> pokemon.getMoveset().attacks[1].baseAttack.getLocalizedName()),
	MOVES_3(pokemon -> pokemon.getMoveset().attacks[2].baseAttack.getLocalizedName()),
	MOVES_4(pokemon -> pokemon.getMoveset().attacks[3].baseAttack.getLocalizedName());

	public final Function<EntityPixelmon, Object> function;

	private EnumPokemonFields(Function<EntityPixelmon, Object> function) {
		this.function = function;
	}

	private static double totalEVs(EVsStore evs) {
		return evs.hp + evs.attack + evs.defence + evs.specialAttack + evs.specialDefence + evs.speed;
	}

	private static double totalIVs(IVStore ivs) {
		return ivs.HP + ivs.Attack + ivs.Defence + ivs.SpAtt + ivs.SpDef + ivs.Speed;
	}
}
