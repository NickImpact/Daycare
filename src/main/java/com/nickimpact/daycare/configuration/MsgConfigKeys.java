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
import com.nickimpact.impactor.api.configuration.IConfigKeys;
import com.nickimpact.impactor.api.configuration.keys.ListKey;
import com.nickimpact.impactor.api.configuration.keys.StringKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * (Some note will appear here)
 *
 * @author NickImpact (Nick DeGruccio)
 */
public class MsgConfigKeys implements IConfigKeys {

	// General
	public static final ConfigKey<String> PLUGIN_PREFIX = StringKey.of("general.plugin.prefix", "&eDaycare &7\u00bb");
	public static final ConfigKey<String> PLUGIN_ERROR = StringKey.of("general.plugin.error-prefix", "&eDaycare &7(&cError&7)");
	public static final ConfigKey<String> UNLOCK_PEN = StringKey.of("general.pen.unlock", "{{daycare_prefix}} &7You unlocked &ePen {{pen}} &7for &a{{price}}&7!");
	public static final ConfigKey<List<String>> STATISTICS = ListKey.of("general.player.stats", Lists.newArrayList(
			"&7Eggs Collected: &e{{stats_eggs_collected}}",
			"&7Eggs Dismissed: &e{{stats_eggs_dismissed}}",
			"&7Ratio: &e{{stats_eggs_ratio}}"
	));
	public static final ConfigKey<String> MUST_COLLECT_EGG_FIRST = StringKey.of("general.pen.must-claim-egg", "{{daycare_error}} &7You must collect the available egg before collecting your pokemon...");
	public static final ConfigKey<List<String>> EVOLVE = ListKey.of("general.ranch.evolve", Lists.newArrayList(
			"{{daycare_prefix}} &7Your &e{{pokemon_before_evo}} &7has evolved into a &e{{pokemon_after_evo}}&7 after leveling up in the daycare!"
	));
	public static final ConfigKey<List<String>> LEARN_MOVE = ListKey.of("general.ranch.learn-move", Lists.newArrayList(
			"{{daycare_prefix}} &7Your &e{{pokemon}} &7leveled up, and ended up learning &e{{pokemon_new_move}}!"
	));
	public static final ConfigKey<List<String>> LEARN_MOVE_REPLACE = ListKey.of("general.ranch.learn-move-and-replace-old", Lists.newArrayList(
			"{{daycare_prefix}} &7Your &e{{pokemon}} &7leveled up, and ended up learning &e{{pokemon_new_move}} &7in replacement of &c{{pokemon_old_move}}&7!"
	));
	public static final ConfigKey<List<String>> EGGS_AVAILABLE = ListKey.of("general.ranch.eggs-available", Collections.singletonList(
			"{{daycare_prefix}} &7New eggs are now available in your ranch!"
	));
	public static final ConfigKey<List<String>> NPC_INTERACT_DIALOGUE = ListKey.of("general.npcs.dialogue.start", Lists.newArrayList(
			"Welcome to the Pokemon Daycare {{player}}!",
			"Here, you may leave your pokemon to be leveled up, or even breed some fresh new babies!"
	));
	public static final ConfigKey<List<String>> NPC_INTERACT_DIALOGUE_EGGS = ListKey.of("general.npcs.dialogue.eggs-available", Collections.singletonList(
			"Hmm... It seems that you have an egg waiting for you!"
	));
	public static final ConfigKey<String> NPC_INTERACT_DIALOGUE_ACTION = StringKey.of("general.npcs.dialogue.interact.question", "How would you like to proceed?");
	public static final ConfigKey<String> NPC_INTERACT_DIALOGUE_ACTION_YES = StringKey.of("general.npcs.dialogue.interact.yes", "Open the Daycare");
	public static final ConfigKey<String> NPC_INTERACT_DIALOGUE_ACTION_NO = StringKey.of("general.npcs.dialogue.interact.no", "I'll come back later");
	public static final ConfigKey<List<String>> NPC_INTERACT_DIALOGUE_NO = ListKey.of("general.npcs.dialogue.decline-open", Collections.singletonList(
			"Alrighty, catch you later!"
	));

	// Start Up
	public static final ConfigKey<List<String>> STARTUP_NO_ECONOMY_SERVICE = ListKey.of("startup.error.no-economy-service", Lists.newArrayList(
			"&e==============================================================",
			"&eWARNING: Daycare detected that an Economy Service was not present on the server.",
			"&eFor Daycare to run properly, or at all, an Economy Service is required.",
			"&eAs such, Daycare will now be disabled to avoid any potential issues...",
			"&e=============================================================="
	));
	public static final ConfigKey<String> STARTUP_INIT_NUCLEUS = StringKey.of("startup.phase.init.nucleus", "&7Integrating with Nucleus Token Service...");
	public static final ConfigKey<String> STARTUP_INIT_COMMANDS = StringKey.of("startup.phase.init.commands", "&7Initializing commands...");
	public static final ConfigKey<String> STARTUP_INIT_LISTENERS = StringKey.of("startup.phase.init.listeners", "&7Registering listeners...");
	public static final ConfigKey<String> STARTUP_INIT_STORAGE= StringKey.of("startup.phase.init.storage.load", "&7Loading data storage...");
	public static final ConfigKey<String> STARTUP_STORAGE_PROVIDER = StringKey.of("startup.phase.init.storage.type", "Loading storage provider... [{{storage_type}}]");
	public static final ConfigKey<String> STARTUP_INIT_COMPLETE = StringKey.of("startup.phase.init.complete", "Initialization complete!");

	public static final ConfigKey<String> STARTUP_STARTED_PHASE = StringKey.of("startup.phase.server-started.begin", "&7Beginning startup procedures...");
	public static final ConfigKey<String> STARTUP_STARTED_TASKS = StringKey.of("startup.phase.server-started.tasks", "&7Queuing running tasks...");
	public static final ConfigKey<String> STARTUP_STARTED_COMPLETE = StringKey.of("startup.phase.server-started.complete", "&7Startup procedures complete!");
	public static final ConfigKey<String> STARTUP_COMPLETE = StringKey.of("startup.complete", "&7Daycare is now fully initialized!");

	// Commands
	public static final ConfigKey<String> CMD_NON_PLAYER = StringKey.of("commands.error.non-player", "{{daycare_error}} &7You must be a player to use this command...");
	public static final ConfigKey<String> CMD_ADDNPC_RIGHTCLICK_NOTICE = StringKey.of("commands.addnpc.right-click-notice", "{{daycare_prefix}} &7Right click on a chatting NPC to set them as a Daycare Representative!");
	public static final ConfigKey<String> CMD_REMOVENPC_RIGHTCLICK_NOTICE = StringKey.of("commands.removenpc.right-click-notice", "{{daycare_prefix}} &7Right click on a registered Daycare NPC to clear their functionality!");
	public static final ConfigKey<String> CMD_PRICING_NO_ECONOMY_SERVICE = StringKey.of("commands.pricing.error.no-economy-service", "{{daycare_error}} &7There is no economy service available to perform that action...");


	// Admin
	public static final ConfigKey<List<String>> NPC_REGISTERED = ListKey.of("admin.npcs.register", Collections.singletonList(
			"{{daycare_prefix}} &7You've added this NPC as a Daycare Representative!"
	));
	public static final ConfigKey<List<String>> NPC_DELETED = ListKey.of("admin.npcs.remove", Collections.singletonList(
			"{{daycare_prefix}} &7You've removed this NPC of its role as a Daycare Representative!"
	));

	// Pens
	public static final ConfigKey<String> POKEMON_TITLE_PEN = StringKey.of("pens.pen-title", "&e{{pokemon}} {{shiny:s}}&7| &bLvl {{calced_lvl}}");
	public static final ConfigKey<List<String>> POKEMON_LORE_PEN = ListKey.of("pens.pen-lore", Lists.newArrayList(
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
	public static final ConfigKey<List<String>> POKEMON_LORE_SELECT = ListKey.of("pens.select-lore", Lists.newArrayList(
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
	public static final ConfigKey<String> ITEM_BACK = StringKey.of("ui.common.items.back.title", "&c\u2190 Go Back \u2190");
	public static final ConfigKey<String> EGG = StringKey.of("ui.common.items.egg", "&eEgg");

	// Ranch UI
	public static final ConfigKey<String> RANCH_UI_TITLE = StringKey.of("ui.main-menu.title", "&cDaycare &7\u00bb &3Main Menu");
	public static final ConfigKey<String> RANCH_UI_PLAYER_INFO = StringKey.of("ui.main-menu.player-info", "&ePlayer Info");
	public static final ConfigKey<String> RANCH_UI_SETTINGS = StringKey.of("ui.main-menu.settings", "&eSettings");
	public static final ConfigKey<String> RANCH_UI_STATS = StringKey.of("ui.main-menu.statistics", "&eStatistics");
	public static final ConfigKey<String> RANCH_UI_EGG_AVAILABLE = StringKey.of("ui.main-menu.pen.egg-available", "&e\u2730 &aEgg Available &e\u2730");
	public static final ConfigKey<String> RANCH_UI_PEN_ID = StringKey.of("ui.main-menu.pen.id", "&ePen {{pen_id}}");
	public static final ConfigKey<String> RANCH_UI_PEN_INFO = StringKey.of("ui.main-menu.pen.slots", "&7Slot {{slot_id}}&7: &e{{pokemon}} {{gender_icon:s}}&7(&aLvl {{calced_lvl}}&7)");
	public static final ConfigKey<String> RANCH_UI_PEN_EMPTY = StringKey.of("ui.main-menu.pen.slot-empty", "&7Slot {{slot_id}}&7: &cEmpty...");
	public static final ConfigKey<List<String>> RANCH_UI_PEN_LOCKED = ListKey.of("ui.main-menu.pen.locked", Lists.newArrayList(
			"&cCurrently locked...",
			""
	));
	public static final ConfigKey<String> RANCH_UI_PEN_INSUFFICIENT_FUNDS = StringKey.of("ui.main-menu.pen.insufficient-funds", "{{daycare_error}} &cInsufficient funds...");

	// Pen UI
	public static final ConfigKey<String> PEN_UI_TITLE = StringKey.of("ui.pen.title", "&cDaycare &7\u00bb &3Pen {{pen_id}}");
	public static final ConfigKey<String> PEN_EMPTY_SLOT = StringKey.of("ui.pen.empty-slot", "&cEmpty &7(Click to Add a Pokemon)");
	public static final ConfigKey<String> PEN_NO_EGG = StringKey.of("ui.pen.items.no-egg", "&cNo Egg Available...");
	public static final ConfigKey<List<String>> PEN_EGG_PRESENT = ListKey.of("ui.pen.items.egg-present", Lists.newArrayList(
			"&7Click to claim!"
	));
	public static final ConfigKey<List<String>> PEN_EGG_CLAIM = ListKey.of("ui.pen.actions.claim-egg", Lists.newArrayList(
			"{{daycare_prefix}} &7You've collected an egg!"
	));
	public static final ConfigKey<List<String>> PEN_EGG_DISMISSED = ListKey.of("ui.pen.actions.dismiss-egg", Lists.newArrayList(
			"{{daycare_prefix}} &7You've allowed the daycare representatives to hold onto the egg!"
	));
	public static final ConfigKey<String> PEN_TITLES_BREEDING = StringKey.of("ui.pen.border.breeding-title", "&aBreeding...");
	public static final ConfigKey<String> PEN_TITLES_EGG_AVAILABLE = StringKey.of("ui.pen.border.egg-available", "&6Egg Available!");
	public static final ConfigKey<String> PEN_TITLES_UNABLE = StringKey.of("ui.pen.border.unable-to-breed", "&cUnable to breed...");
	public static final ConfigKey<String> PEN_HISTORY = StringKey.of("ui.pen.pen-info.title", "&ePen Info");
	public static final ConfigKey<List<String>> PEN_HISTORY_LORE = ListKey.of("ui.pen.pen-info.lore", Lists.newArrayList(
			"&7Date Unlocked: &e{{date_unlocked}}",
			"&7Eggs Forged: &e{{total_eggs_produced}}"
	));

	// Selection UI
	public static final ConfigKey<String> SELECT_TITLE = StringKey.of("ui.selection.title", "&cDaycare &7\u00bb &3{{pokemon}}");
	public static final ConfigKey<String> SELECT_RETRIEVE_TITLE = StringKey.of("ui.selection.retrieve.title", "&aRetrieve {{pokemon}}?");
	public static final ConfigKey<List<String>> SELECT_RETRIEVE_LORE = ListKey.of("ui.selection.retrieve.lore", Lists.newArrayList(
			"&7Retrieval Cost: &a{{price}}"
	));
	public static final ConfigKey<List<String>> SELECT_RETRIEVE_PRICE_CHANGE = ListKey.of("ui.selection.actions.retrieve.price-change", Collections.singletonList(
			"{{daycare_prefix}} &7The price to to retrieve youor pokemon has changed recently, make sure you are ok with this new price!"
	));
	public static final ConfigKey<List<String>> SELECT_RETRIEVE = ListKey.of("ui.selection.actions.retrieve.success", Collections.singletonList(
			"{{daycare_prefix}} &7You've retrieved your &e{{pokemon}} &7for &a{{price}}&7!"
	));
	public static final ConfigKey<List<String>> SELECT_RETRIEVE_EVOLVED = ListKey.of("ui.selection.actions.retrieve.evolved", Collections.singletonList(
			"{{daycare_prefix}} &7Whilst you were making your decision, your &e{{pokemon_old}} &7evolved into &e{{pokemon}}&7!"
	));

	// Party UI
	public static final ConfigKey<String> PARTY_TITLE = StringKey.of("ui.party.title", "&cDaycare &7\u00bb &3Party");
	public static final ConfigKey<String> PARTY_CONFIRM = StringKey.of("ui.party.items.confirm", "&aConfirm Selection");
	public static final ConfigKey<String> PARTY_PC = StringKey.of("ui.party.items.pc", "&eOpen PC");
	public static final ConfigKey<String> PARTY_NO_SELECTION = StringKey.of("ui.party.items.no-selection", "&cPlease Pick a Pokemon");

	// PC UI
	public static final ConfigKey<String> PC_TITLE = StringKey.of("ui.pc.title", "&cDaycare &7\u00bb &3PC");

	// Settings UI
	public static ConfigKey<String> SETTINGS_UI_TITLE = StringKey.of("ui.settings.title", "&cDaycare &7\u00bb &3Settings");
	public static ConfigKey<String> SETINGS_MOVES_TITLE = StringKey.of("ui.settings.items.moves-title", "&eLearn Moves?");
	public static final ConfigKey<List<String>> SETTINGS_MOVES_LORE = ListKey.of("ui.settings.items.moves-lore", Lists.newArrayList(
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
	public static ConfigKey<String> SETINGS_LEVEL_TITLE = StringKey.of("ui.settings.items.leveling-title", "&eAllow leveling?");
	public static final ConfigKey<List<String>> SETTINGS_LEVEL_LORE = ListKey.of("ui.settings.items.leveling-lore", Lists.newArrayList(
			"&7Should pokemon be able",
			"&7to level up as they spend",
			"&7time in the daycare?"
	));
	public static ConfigKey<String> SETINGS_EVOLVE_TITLE = StringKey.of("ui.settings.items.evolve-title", "&eAllow Pokemon to Evolve?");
	public static final ConfigKey<List<String>> SETTINGS_EVOLVE_LORE = ListKey.of("ui.settings.items.evolve-lore", Lists.newArrayList(
			"&7Should pokemon be able",
			"&7to evolve from leveling up",
			"&7in the daycare?",
			"",
			"&a&lNOTE:",
			"&7This setting depends on",
			"&7the leveling setting to",
			"&7be enabled!"
	));
	public static final ConfigKey<List<String>> SETTINGS_APPLY = ListKey.of("ui.settings.apply-changes", Lists.newArrayList(
			"{{daycare_prefix}} &7Settings applied!"
	));

	private static Map<String, ConfigKey<?>> KEYS = null;
	@Override
	public Map<String, ConfigKey<?>> getAllKeys() {
		if(KEYS == null) {
			Map<String, ConfigKey<?>> keys = new LinkedHashMap<>();

			try {
				Field[] values = MsgConfigKeys.class.getFields();
				for(Field f : values) {
					if(!Modifier.isStatic(f.getModifiers()))
						continue;

					Object val = f.get(null);
					if(val instanceof ConfigKey<?>)
						keys.put(f.getName(), (ConfigKey<?>) val);
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			KEYS = ImmutableMap.copyOf(keys);
		}

		return KEYS;
	}
}
