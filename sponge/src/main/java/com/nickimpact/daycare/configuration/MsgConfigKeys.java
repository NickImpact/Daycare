/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.nickimpact.daycare.configuration;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.configuration.ConfigKey;
import com.nickimpact.impactor.api.configuration.ConfigKeyHolder;
import com.nickimpact.impactor.api.configuration.keys.BaseConfigKey;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.nickimpact.impactor.api.configuration.ConfigKeyTypes.listKey;
import static com.nickimpact.impactor.api.configuration.ConfigKeyTypes.stringKey;

/**
 * (Some note will appear here)
 *
 * @author NickImpact (Nick DeGruccio)
 */
public class MsgConfigKeys implements ConfigKeyHolder {

	// General
	public static final ConfigKey<String> PLUGIN_PREFIX = stringKey("general.plugin.prefix", "&eDaycare &7\u00bb");
	public static final ConfigKey<String> PLUGIN_ERROR = stringKey("general.plugin.error-prefix", "&eDaycare &7(&cError&7)");
	public static final ConfigKey<String> UNLOCK_PEN = stringKey("general.pen.unlock", "{{daycare_prefix}} &7You unlocked &ePen {{pen}} &7for &a{{daycare_price}}&7!");
	public static final ConfigKey<List<String>> STATISTICS = listKey("general.player.stats", Lists.newArrayList(
			"&7Eggs Collected: &e{{stats_eggs_collected}}",
			"&7Eggs Dismissed: &e{{stats_eggs_dismissed}}",
			"&7Ratio: &e{{stats_eggs_ratio}}"
	));
	public static final ConfigKey<String> MUST_COLLECT_EGG_FIRST = stringKey("general.pen.must-claim-egg", "{{daycare_error}} &7You must collect the available egg before collecting your pokemon...");
	public static final ConfigKey<List<String>> EVOLVE = listKey("general.ranch.evolve", Lists.newArrayList(
			"{{daycare_prefix}} &7Your &e{{pokemon_before_evo}} &7has evolved into a &e{{pokemon_after_evo}}&7 after leveling up in the daycare!"
	));
	public static final ConfigKey<List<String>> LEARN_MOVE = listKey("general.ranch.learn-move", Lists.newArrayList(
			"{{daycare_prefix}} &7Your &e{{pokemon}} &7leveled up, and ended up learning &e{{pokemon_new_move}}!"
	));
	public static final ConfigKey<List<String>> LEARN_MOVE_REPLACE = listKey("general.ranch.learn-move-and-replace-old", Lists.newArrayList(
			"{{daycare_prefix}} &7Your &e{{pokemon}} &7leveled up, and ended up learning &e{{pokemon_new_move}} &7in replacement of &c{{pokemon_old_move}}&7!"
	));
	public static final ConfigKey<List<String>> EGGS_AVAILABLE = listKey("general.ranch.eggs-available", Collections.singletonList(
			"{{daycare_prefix}} &7New eggs are now available in your ranch!"
	));
	public static final ConfigKey<List<String>> NPC_INTERACT_DIALOGUE = listKey("general.npcs.dialogue.start", Lists.newArrayList(
			"Welcome to the Pokemon Daycare {{player}}!",
			"Here, you may leave your pokemon to be leveled up, or even breed some fresh new babies!"
	));
	public static final ConfigKey<List<String>> NPC_INTERACT_DIALOGUE_EGGS = listKey("general.npcs.dialogue.eggs-available", Collections.singletonList(
			"Hmm... It seems that you have an egg waiting for you!"
	));
	public static final ConfigKey<String> NPC_INTERACT_DIALOGUE_ACTION = stringKey("general.npcs.dialogue.interact.question", "How would you like to proceed?");
	public static final ConfigKey<String> NPC_INTERACT_DIALOGUE_ACTION_YES = stringKey("general.npcs.dialogue.interact.yes", "Open the Daycare");
	public static final ConfigKey<String> NPC_INTERACT_DIALOGUE_ACTION_NO = stringKey("general.npcs.dialogue.interact.no", "I'll come back later");
	public static final ConfigKey<List<String>> NPC_INTERACT_DIALOGUE_NO = listKey("general.npcs.dialogue.decline-open", Collections.singletonList(
			"Alrighty, catch you later!"
	));

	// Start Up
	public static final ConfigKey<List<String>> STARTUP_NO_ECONOMY_SERVICE = listKey("startup.error.no-economy-service", Lists.newArrayList(
			"&e==============================================================",
			"&eWARNING: Daycare detected that an Economy Service was not present on the server.",
			"&eFor Daycare to run properly, or at all, an Economy Service is required.",
			"&eAs such, Daycare will now be disabled to avoid any potential issues...",
			"&e=============================================================="
	));
	public static final ConfigKey<String> STARTUP_INIT_NUCLEUS = stringKey("startup.phase.init.nucleus", "&7Integrating with Nucleus Token Service...");
	public static final ConfigKey<String> STARTUP_INIT_COMMANDS = stringKey("startup.phase.init.commands", "&7Initializing commands...");
	public static final ConfigKey<String> STARTUP_INIT_LISTENERS = stringKey("startup.phase.init.listeners", "&7Registering listeners...");
	public static final ConfigKey<String> STARTUP_INIT_STORAGE= stringKey("startup.phase.init.storage.load", "&7Loading data storage...");
	public static final ConfigKey<String> STARTUP_STORAGE_PROVIDER = stringKey("startup.phase.init.storage.type", "Loading storage provider... [{{storage_type}}]");
	public static final ConfigKey<String> STARTUP_INIT_COMPLETE = stringKey("startup.phase.init.complete", "Initialization complete!");

	public static final ConfigKey<String> STARTUP_STARTED_PHASE = stringKey("startup.phase.server-started.begin", "&7Beginning startup procedures...");
	public static final ConfigKey<String> STARTUP_STARTED_TASKS = stringKey("startup.phase.server-started.tasks", "&7Queuing running tasks...");
	public static final ConfigKey<String> STARTUP_STARTED_COMPLETE = stringKey("startup.phase.server-started.complete", "&7Startup procedures complete!");
	public static final ConfigKey<String> STARTUP_COMPLETE = stringKey("startup.complete", "&7Daycare is now fully initialized!");

	// Commands
	public static final ConfigKey<String> CMD_NON_PLAYER = stringKey("commands.error.non-player", "{{daycare_error}} &7You must be a player to use this command...");
	public static final ConfigKey<String> CMD_ADDNPC_RIGHTCLICK_NOTICE = stringKey("commands.addnpc.right-click-notice", "{{daycare_prefix}} &7Right click on a chatting NPC to set them as a Daycare Representative!");
	public static final ConfigKey<String> CMD_REMOVENPC_RIGHTCLICK_NOTICE = stringKey("commands.removenpc.right-click-notice", "{{daycare_prefix}} &7Right click on a registered Daycare NPC to clear their functionality!");
	public static final ConfigKey<String> CMD_PRICING_NO_ECONOMY_SERVICE = stringKey("commands.pricing.error.no-economy-service", "{{daycare_error}} &7There is no economy service available to perform that action...");


	// Admin
	public static final ConfigKey<List<String>> NPC_REGISTERED = listKey("admin.npcs.register", Collections.singletonList(
			"{{daycare_prefix}} &7You've added this NPC as a Daycare Representative!"
	));
	public static final ConfigKey<List<String>> NPC_DELETED = listKey("admin.npcs.remove", Collections.singletonList(
			"{{daycare_prefix}} &7You've removed this NPC of its role as a Daycare Representative!"
	));

	// Pens
	public static final ConfigKey<String> POKEMON_TITLE_PEN = stringKey("pens.pen-title.pokemon", "&e{{pokemon}} {{shiny:s}}&7| &bLvl {{calced_lvl}}");
	public static final ConfigKey<String> POKEMON_EGG_TITLE_PEN = stringKey("pens.pen-title.egg", "&e{{pokemon}} {{shiny}}");
	public static final ConfigKey<List<String>> POKEMON_LORE_PEN = listKey("pens.pen-lore", Lists.newArrayList(
			"&7Nickname: &e{{nickname}}",
			"&7Gained Levels: &e{{gained_lvls}}",
			"&7Ability: &e{{ability}}",
			"&7Gender: &e{{gender}}",
			"&7Nature: &e{{nature}}",
			"&7Size: &e{{growth}}",
			"&7Holding: &e{{held_item}}",
			"",
			"&7IVs: &e{{ivs_total}}&7/&e186 &7(&a{{ivs_percent}}&7)",
			"&7IVs: &e{{ivhp}}&7/&e{{ivatk}}&7/&e{{ivdef}}&7/&e{{ivspatk}}&7/&e{{ivspdef}}&7/&e{{ivspeed}}"
	));
	public static final ConfigKey<List<String>> POKEMON_LORE_SELECT = listKey("pens.select-lore", Lists.newArrayList(
			"&7Nickname: &e{{nickname}}",
			"&7Ability: &e{{ability}}",
			"&7Gender: &e{{gender}}",
			"&7Nature: &e{{nature}}",
			"&7Size: &e{{growth}}",
			"&7Holding: &e{{held_item}}",
			"",
			"&7IVs: &e{{ivs_total}}&7/&e186 &7(&a{{ivs_percent}}&7)",
			"&7IVs: &e{{ivhp}}&7/&e{{ivatk}}&7/&e{{ivdef}}&7/&e{{ivspatk}}&7/&e{{ivspdef}}&7/&e{{ivspeed}}"
	));

	// Common Items
	public static final ConfigKey<String> ITEM_BACK = stringKey("ui.common.common.back.title", "&c\u2190 Go Back \u2190");
	public static final ConfigKey<String> EGG = stringKey("ui.common.common.egg", "&eEgg");

	// Ranch UI
	public static final ConfigKey<String> RANCH_UI_TITLE = stringKey("ui.main-menu.title", "&cDaycare &7\u00bb &3Main Menu");
	public static final ConfigKey<String> RANCH_UI_PLAYER_INFO = stringKey("ui.main-menu.player-info", "&ePlayer Info");
	public static final ConfigKey<String> RANCH_UI_SETTINGS = stringKey("ui.main-menu.settings", "&eSettings");
	public static final ConfigKey<String> RANCH_UI_STATS = stringKey("ui.main-menu.statistics", "&eStatistics");
	public static final ConfigKey<String> RANCH_PRICE_TAG = stringKey("ui.main-menu.price-tag", "&7Price: &e{{daycare_price}}");
	public static final ConfigKey<String> RANCH_UI_EGG_AVAILABLE = stringKey("ui.main-menu.pen.egg-available", "&e\u2730 &aEgg Available &e\u2730");
	public static final ConfigKey<String> RANCH_UI_PEN_ID = stringKey("ui.main-menu.pen.id", "&ePen {{pen_id}}");
	public static final ConfigKey<String> RANCH_UI_PEN_INFO = stringKey("ui.main-menu.pen.slots", "&7Slot {{slot_id}}&7: &e{{pokemon}} {{gender_icon:s}}&7(&aLvl {{calced_lvl}}&7)");
	public static final ConfigKey<String> RANCH_UI_PEN_EMPTY = stringKey("ui.main-menu.pen.slot-empty", "&7Slot {{slot_id}}&7: &cEmpty...");
	public static final ConfigKey<List<String>> RANCH_UI_PEN_LOCKED = listKey("ui.main-menu.pen.locked", Lists.newArrayList(
			"&cCurrently locked...",
			""
	));
	public static final ConfigKey<String> RANCH_UI_PEN_INSUFFICIENT_FUNDS = stringKey("ui.main-menu.pen.insufficient-funds", "{{daycare_error}} &cInsufficient funds...");

	// Pen UI
	public static final ConfigKey<String> PEN_UI_TITLE = stringKey("ui.pen.title", "&cDaycare &7\u00bb &3Pen {{pen_id}}");
	public static final ConfigKey<String> CONFIRM_UI_TITLE = stringKey("ui.pen.title", "&cDaycare &7\u00bb &3Confirm Purchase");
	public static final ConfigKey<String> PEN_EMPTY_SLOT = stringKey("ui.pen.empty-slot", "&cEmpty &7(Click to Add a Pokemon)");
	public static final ConfigKey<String> PEN_NO_EGG = stringKey("ui.pen.common.no-egg", "&cNo Egg Available...");
	public static final ConfigKey<List<String>> PEN_EGG_PRESENT = listKey("ui.pen.common.egg-present", Lists.newArrayList(
			"&7Click to claim!"
	));
	public static final ConfigKey<List<String>> PEN_EGG_CLAIM = listKey("ui.pen.actions.claim-egg", Lists.newArrayList(
			"{{daycare_prefix}} &7You've collected an egg!"
	));
	public static final ConfigKey<List<String>> PEN_EGG_DISMISSED = listKey("ui.pen.actions.dismiss-egg", Lists.newArrayList(
			"{{daycare_prefix}} &7You've allowed the daycare representatives to hold onto the egg!"
	));
	public static final ConfigKey<String> PEN_TITLES_BREEDING = stringKey("ui.pen.border.breeding-title", "&aBreeding...");
	public static final ConfigKey<String> PEN_TITLES_EGG_AVAILABLE = stringKey("ui.pen.border.egg-available", "&6Egg Available!");
	public static final ConfigKey<String> PEN_TITLES_UNABLE = stringKey("ui.pen.border.unable-to-breed", "&cUnable to breed...");
	public static final ConfigKey<String> PEN_HISTORY = stringKey("ui.pen.pen-info.title", "&ePen Info");
	public static final ConfigKey<List<String>> PEN_HISTORY_LORE = listKey("ui.pen.pen-info.lore", Lists.newArrayList(
			"&7Date Unlocked: &e{{date_unlocked}}",
			"&7Eggs Forged: &e{{total_eggs_produced}}"
	));

	// Selection UI
	public static final ConfigKey<String> SELECT_TITLE = stringKey("ui.selection.title", "&cDaycare &7\u00bb &3{{pokemon}}");
	public static final ConfigKey<String> SELECT_RETRIEVE_TITLE = stringKey("ui.selection.retrieve.title", "&aRetrieve {{pokemon}}?");
	public static final ConfigKey<List<String>> SELECT_RETRIEVE_LORE = listKey("ui.selection.retrieve.lore", Lists.newArrayList(
			"&7Retrieval Cost: &a{{price}}"
	));
	public static final ConfigKey<List<String>> SELECT_RETRIEVE_PRICE_CHANGE = listKey("ui.selection.actions.retrieve.price-change", Collections.singletonList(
			"{{daycare_prefix}} &7The price to to retrieve youor pokemon has changed recently, make sure you are ok with this new price!"
	));
	public static final ConfigKey<List<String>> SELECT_RETRIEVE = listKey("ui.selection.actions.retrieve.success", Collections.singletonList(
			"{{daycare_prefix}} &7You've retrieved your &e{{pokemon}} &7for &a{{price}}&7!"
	));
	public static final ConfigKey<List<String>> SELECT_RETRIEVE_EVOLVED = listKey("ui.selection.actions.retrieve.evolved", Collections.singletonList(
			"{{daycare_prefix}} &7Whilst you were making your decision, your &e{{pokemon_old}} &7evolved into &e{{pokemon}}&7!"
	));

	// Party UI
	public static final ConfigKey<String> PARTY_TITLE = stringKey("ui.party.title", "&cDaycare &7\u00bb &3Party");
	public static final ConfigKey<String> PARTY_PC = stringKey("ui.party.common.pc", "&eOpen PC");
	public static final ConfigKey<String> PARTY_NO_SELECTION = stringKey("ui.party.common.no-selection", "&cPlease Pick a Pokemon");

	// PC UI
	public static final ConfigKey<String> PC_TITLE = stringKey("ui.pc.title", "&cDaycare &7\u00bb &3PC");

	// Settings UI
	public static ConfigKey<String> SETTINGS_UI_TITLE = stringKey("ui.settings.title", "&cDaycare &7\u00bb &3Settings");
	public static ConfigKey<String> SETINGS_MOVES_TITLE = stringKey("ui.settings.common.moves-title", "&eLearn Moves?");
	public static final ConfigKey<List<String>> SETTINGS_MOVES_LORE = listKey("ui.settings.common.moves-lore", Lists.newArrayList(
			"&7Should pokemon learn moves",
			"&7as they level up in the",
			"&7daycare?",
			"",
			"&a&lNOTE:",
			"&7This setting depends on",
			"&7the leveling setting to",
			"&7be enabled!",
			"",
			"&cREQUIRES PIXELMON 6.3.X AND UP!"
	));
	public static ConfigKey<String> SETINGS_LEVEL_TITLE = stringKey("ui.settings.common.leveling-title", "&eAllow leveling?");
	public static final ConfigKey<List<String>> SETTINGS_LEVEL_LORE = listKey("ui.settings.common.leveling-lore", Lists.newArrayList(
			"&7Should pokemon be able",
			"&7to level up as they spend",
			"&7time in the daycare?"
	));
	public static ConfigKey<String> SETINGS_EVOLVE_TITLE = stringKey("ui.settings.common.evolve-title", "&eAllow Pokemon to Evolve?");
	public static final ConfigKey<List<String>> SETTINGS_EVOLVE_LORE = listKey("ui.settings.common.evolve-lore", Lists.newArrayList(
			"&7Should pokemon be able",
			"&7to evolve from leveling up",
			"&7in the daycare?",
			"",
			"&a&lNOTE:",
			"&7This setting depends on",
			"&7the leveling setting to",
			"&7be enabled!"
	));
	public static final ConfigKey<List<String>> SETTINGS_APPLY = listKey("ui.settings.apply-changes", Lists.newArrayList(
			"{{daycare_prefix}} &7Settings applied!"
	));

	public static final ConfigKey<String> TRANSLATIONS_YES = stringKey("translations.yes", "Yes");
	public static final ConfigKey<String> TRANSLATIONS_NO = stringKey("translations.no", "No");
	public static final ConfigKey<String> SHINY_TRANSLATION = stringKey("translations.shiny", "Shiny");
	public static final ConfigKey<String> BREEDABLE_TRANSLATION = stringKey("translations.breedable", "Breedable");
	public static final ConfigKey<String> UNBREEDABLE_TRANSLATION = stringKey("translations.unbreedable", "Unbreedable");
	public static final ConfigKey<String> POKERUS_TRANSLATION = stringKey("translations.pokerus", "PKRS");

	public static final ConfigKey<String> CONFIRM = stringKey("ui.common.confirm", "&aConfirm");
	public static final ConfigKey<String> CANCEL = stringKey("ui.common.cancel", "&cCancel");

	public static final ConfigKey<List<String>> CONFIRM_PEN_DETAILS = listKey("ui.confirm.pen-details", Lists.newArrayList(
			"&7Price to Unlock: &e{{daycare_price}}"
	));
	public static final ConfigKey<List<String>> CONFIRM_PEN_BUTTON = listKey("ui.confirm.button", Lists.newArrayList(
			"&7By clicking here, you agree to",
			"&7pay the price of: &e{{daycare_price}}"
	));
	public static final ConfigKey<List<String>> CONFIRM_RETRIEVAL_POKEMON = listKey("ui.confirm.retrieve-pokemon", Lists.newArrayList(
			"&7By clicking here, you agree to",
			"&7pay the price of &e{{daycare_price}}",
			"&7in order to retrieve your &a{{pokemon}}"
	));

	private static final Map<String, ConfigKey<?>> KEYS;
	private static final int SIZE;

	static {
		Map<String, ConfigKey<?>> keys = new LinkedHashMap<>();
		Field[] values = MsgConfigKeys.class.getFields();
		int i = 0;

		for (Field f : values) {
			// ignore non-static fields
			if (!Modifier.isStatic(f.getModifiers())) {
				continue;
			}

			// ignore fields that aren't configkeys
			if (!ConfigKey.class.equals(f.getType())) {
				continue;
			}

			try {
				// get the key instance
				BaseConfigKey<?> key = (BaseConfigKey<?>) f.get(null);
				// set the ordinal value of the key.
				key.ordinal = i++;
				// add the key to the return map
				keys.put(f.getName(), key);
			} catch (Exception e) {
				throw new RuntimeException("Exception processing field: " + f, e);
			}
		}

		KEYS = ImmutableMap.copyOf(keys);
		SIZE = i;
	}

	@Override
	public Map<String, ConfigKey<?>> getKeys() {
		return KEYS;
	}

	@Override
	public int getSize() {
		return SIZE;
	}
}
