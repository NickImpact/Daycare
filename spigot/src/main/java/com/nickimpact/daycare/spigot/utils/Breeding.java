package com.nickimpact.daycare.spigot.utils;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.RandomHelper;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import com.pixelmonmod.pixelmon.config.PixelmonItemsHeld;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import net.minecraft.item.Item;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Breeding {

	public static EnumSpecies getPokemonInEggName(Pokemon p1, Pokemon p2) {
		boolean inherit = !p1.isPokemon(EnumSpecies.Ditto) && p2.isPokemon(EnumSpecies.Ditto) || p2.getGender() == Gender.Male;
		Pokemon parent = inherit ? p1 : p2;
		Item ip = parent.getHeldItem().getItem();
		Item other = inherit ? p2.getHeldItem().getItem() : p1.getHeldItem().getItem();

		EnumSpecies[] eggForms;
		if(parent.isPokemon(EnumSpecies.Nidoranfemale, EnumSpecies.Nidoranmale, EnumSpecies.Nidorino, EnumSpecies.Nidoking)) {
			eggForms = new EnumSpecies[]{EnumSpecies.Nidoranfemale, EnumSpecies.Nidoranmale};
			return RandomHelper.getRandomElementFromArray(eggForms);
		} else if(parent.isPokemon(EnumSpecies.Illumise, EnumSpecies.Volbeat)) {
			eggForms = new EnumSpecies[]{EnumSpecies.Illumise, EnumSpecies.Volbeat};
			return RandomHelper.getRandomElementFromArray(eggForms);
		} else if (checkIncense(ip, other, parent, PixelmonItemsHeld.seaIncense, EnumSpecies.Azurill, EnumSpecies.Marill, EnumSpecies.Azumarill)) {
			return EnumSpecies.Marill;
		} else if (checkIncense(ip, other, parent, PixelmonItemsHeld.laxIncense, EnumSpecies.Wynaut, EnumSpecies.Wobbuffet)) {
			return EnumSpecies.Wobbuffet;
		} else if (checkIncense(ip, other, parent, PixelmonItemsHeld.roseIncense, EnumSpecies.Budew, EnumSpecies.Roselia, EnumSpecies.Roserade)) {
			return EnumSpecies.Roselia;
		} else if (checkIncense(ip, other, parent, PixelmonItemsHeld.pureIncense, EnumSpecies.Chingling, EnumSpecies.Chimecho)) {
			return EnumSpecies.Chimecho;
		} else if (checkIncense(ip, other, parent, PixelmonItemsHeld.rockIncense, EnumSpecies.Bonsly, EnumSpecies.Sudowoodo)) {
			return EnumSpecies.Sudowoodo;
		} else if (checkIncense(ip, other, parent, PixelmonItemsHeld.oddIncense, EnumSpecies.MimeJr, EnumSpecies.MrMime)) {
			return EnumSpecies.MrMime;
		} else if (checkIncense(ip, other, parent, PixelmonItemsHeld.luckIncense, EnumSpecies.Happiny, EnumSpecies.Chansey, EnumSpecies.Blissey)) {
			return EnumSpecies.Chansey;
		} else if (checkIncense(ip, other, parent, PixelmonItemsHeld.waveIncense, EnumSpecies.Mantyke, EnumSpecies.Mantine)) {
			return EnumSpecies.Mantine;
		} else {
			return checkIncense(ip, other, parent, PixelmonItemsHeld.fullIncense, EnumSpecies.Munchlax, EnumSpecies.Snorlax) ? EnumSpecies.Snorlax : parent.getSpecies().getBaseSpecies();
		}
	}

	private static boolean checkIncense(Item itemTypeParent, Item itemTypeOther, Pokemon parentForEggLine, Item neededItem, EnumSpecies baby, EnumSpecies... parents) {
		if (!parentForEggLine.isPokemon(parents)) {
			return false;
		} else if (itemTypeParent != neededItem && itemTypeOther != neededItem) {
			return true;
		} else {
			return PixelmonConfig.isGenerationEnabled(parentForEggLine.getSpecies().getGeneration()) && !PixelmonConfig.isGenerationEnabled(baby.getGeneration());
		}
	}

	public static List<Integer> getValidAbilitySlots(String[] abilities) {
		List<Integer> out = Lists.newArrayList(0);
		if(abilities.length > 1 && abilities[1] != null) {
			out.add(1);
		}

		if(abilities.length > 2 && abilities[2] != null) {
			out.add(2);
		}

		return out;
	}

	public static boolean shouldBeShiny(UUID owner, Pokemon parent1, Pokemon parent2) {
		float intDifferentTrainerFactor = 1.0F;
		if (!Objects.equals(parent1.getOriginalTrainerUUID(), parent2.getOriginalTrainerUUID())) {
			intDifferentTrainerFactor = 2.0F;
		}

		if(PixelmonConfig.allowShinyCharmFromPokedex) {
			if (Pixelmon.storageManager.getParty(owner).getShinyCharm().isActive()) {
				intDifferentTrainerFactor *= 3.0F;
			}
		}

		int dimension = 0;
		if (parent1.getPixelmonWrapperIfExists() != null) {
			dimension = parent1.getPixelmonWrapperIfExists().getWorld().provider.getDimension();
		}

		return PixelmonConfig.getShinyRate(dimension) != 0.0F && RandomHelper.rand.nextFloat() < intDifferentTrainerFactor / PixelmonConfig.getShinyRate(dimension);
	}
}
