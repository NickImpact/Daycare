package com.nickimpact.daycare.generations.pokemon;

import com.nickimpact.daycare.sponge.SpongeDaycarePlugin;
import com.nickimpact.daycare.api.configuration.ConfigKeys;
import com.nickimpact.daycare.sponge.configuration.MsgConfigKeys;
import com.nickimpact.impactor.api.configuration.Config;
import com.pixelmonmod.pixelmon.battles.attacks.specialAttacks.basic.HiddenPower;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.entities.pixelmon.EnumSpecialTexture;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.EVsStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.IVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.text.DecimalFormat;
import java.util.function.Function;

public enum EnumPokemonFields {

	NAME(Entity::getName),
	ABILITY(pokemon -> pokemon.getAbility().getLocalizedName()),
	NATURE(pokemon -> pokemon.getNature().name()),
	NATURE_INCREASED(pokemon -> "+" + toRep(pokemon.getNature().increasedStat)),
	NATURE_DECREASED(pokemon -> "-" + toRep(pokemon.getNature().decreasedStat)),
	GENDER(pokemon -> {
		switch(pokemon.getGender()) {
			case Male:
				return Text.of(TextColors.AQUA, Gender.Male.name());
			case Female:
				return Text.of(TextColors.LIGHT_PURPLE, Gender.Female.name());
			default:
				return Text.of(TextColors.WHITE, Gender.None.name());
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
		if(!pokemon.getIsShiny())
			return Text.EMPTY;

		return Text.of(TextColors.GRAY, "(", TextColors.GOLD, SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsg(null, MsgConfigKeys.SHINY_TRANSLATION, null, null), TextColors.GRAY, ")");
	}),

	GROWTH(pokemon -> pokemon.getGrowth().name()),
	LEVEL(pokemon -> {
		if(pokemon.isEgg) {
			return 1;
		}

		return pokemon.getLvl().getLevel();
	}),
	FORM(EntityPixelmon::getForm),
	FORM_NAME(pokemon -> {
		String form = pokemon.getFormEnum().getFormSuffix();
		if(form.startsWith("-")) {
			return form.substring(1);
		} else {
			return form;
		}
	}),
	CLONES(pokemon -> {
		if(pokemon.getSpecies().equals(EnumPokemon.Mew)) {
			NBTTagCompound nbt = new NBTTagCompound();
			pokemon.writeToNBT(nbt);
			return nbt.getShort(NbtKeys.STATS_NUM_CLONED);
		}
		return 0;
	}),
	ENCHANTED(pokemon -> {
		switch (pokemon.getSpecies()) {
			case Mesprit:
			case Azelf:
			case Uxie:
				NBTTagCompound nbt = new NBTTagCompound();
				pokemon.writeToNBT(nbt);
				return nbt.getShort(NbtKeys.STATS_NUM_ENCHANTED);
			default:
				return 0;
		}
	}),
	EV_PERCENT(pokemon -> new DecimalFormat("#0.##").format(totalEVs(pokemon.stats.EVs) / 510.0 * 100) + "%"),
	IV_PERCENT(pokemon -> new DecimalFormat("#0.##").format(totalIVs(pokemon.stats.IVs) / 186.0 * 100) + "%"),
	EV_TOTAL(pokemon -> (int)totalEVs(pokemon.stats.EVs)),
	IV_TOTAL(pokemon -> (int)totalIVs(pokemon.stats.IVs)),
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
	TEXTURE(pokemon -> {
		NBTTagCompound nbt = new NBTTagCompound();
		pokemon.writeToNBT(nbt);

		String texture = nbt.getString(NbtKeys.CUSTOM_TEXTURE);
		if(!texture.isEmpty()) {
			Config config = SpongeDaycarePlugin.getSpongeInstance().getConfig();
			if(config.get(ConfigKeys.TEXTUREFLAG_CAPITALIZE)) {
				StringBuilder sb = new StringBuilder();
				String[] split = texture.split("\\s+");

				boolean first = true;
				for(String word : split) {
					if(!first) {
						sb.append(" ");
					}
					sb.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase());
					first = false;
				}

				texture = sb.toString();
			}

			if(config.get(ConfigKeys.TEXTUREFLAG_TRIM_TRAILING_NUMS)) {
				texture = texture.replaceAll("\\d*$", "");
			}

			return texture;
		}

		return pokemon.getIsShiny() ? SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsg(null, SpongeDaycarePlugin.getSpongeInstance().getMsgConfig(), MsgConfigKeys.SHINY_TRANSLATION, null, null).toPlain() : "";
	}),
	SPECIAL_TEXTURE(pokemon -> {
		return EnumSpecialTexture.fromIndex(pokemon.getSpecialTextureIndex()).name();
	}),
	HIDDEN_POWER(pokemon -> HiddenPower.getHiddenPowerType(pokemon.stats.IVs).name()),
	MOVES_1(pokemon -> pokemon.getMoveset().attacks[0].baseAttack.getLocalizedName()),
	MOVES_2(pokemon -> pokemon.getMoveset().attacks[1].baseAttack.getLocalizedName()),
	MOVES_3(pokemon -> pokemon.getMoveset().attacks[2].baseAttack.getLocalizedName()),
	MOVES_4(pokemon -> pokemon.getMoveset().attacks[3].baseAttack.getLocalizedName()),
	SHINY_STATE(pokemon -> SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsg(null, pokemon.getIsShiny() ? MsgConfigKeys.TRANSLATIONS_YES : MsgConfigKeys.TRANSLATIONS_NO, null, null)),
//	POKERUS_STATE(pokemon -> pokemon.getPokerus().isPresent() ? "Yes" : "No"),
//	POKERUS(pokemon -> pokemon.getPokerus().isPresent() ? "PKRS" : null),
	UNBREEDABLE(pokemon -> {
//		if(Flags.UNBREEDABLE.matches(pokemon)){
//			return SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsg(null, SpongeDaycarePlugin.getSpongeInstance().getMsgConfig(), MsgConfigKeys.UNBREEDABLE_TRANSLATION, null, null);
//		}else{
			return SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsg(null, SpongeDaycarePlugin.getSpongeInstance().getMsgConfig(), MsgConfigKeys.BREEDABLE_TRANSLATION, null, null);
//		}
	}),
	POKE_BALL_NAME(pokemon ->{
		return pokemon.caughtBall.name();
	}),
	HELD_ITEM(pokemon -> {
		ItemStack item = pokemon.heldItem;
		if(item == null || item == ItemStack.EMPTY) {
			return "Nothing";
		}

		return pokemon.heldItem.getDisplayName();
	}),;


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

	private static String toRep(StatsType stat) {
		switch(stat) {
			case HP:
				return "HP";
			case Attack:
				return "Atk";
			case Defence:
				return "Def";
			case SpecialAttack:
				return "SpAtk";
			case SpecialDefence:
				return "SpDef";
			case Speed:
				return "Speed";
			default:
				return "???";
		}
	}
}
