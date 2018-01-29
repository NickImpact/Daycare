package com.nickimpact.daycare.configuration;

import com.google.common.collect.Lists;
import com.nickimpact.daycare.api.configuration.ConfigKey;
import com.nickimpact.daycare.api.configuration.keys.ListKey;
import com.nickimpact.daycare.api.configuration.keys.StringKey;

import java.util.List;

/**
 * (Some note will appear here)
 *
 * @author NickImpact (Nick DeGruccio)
 */
public class MsgConfigKeys {

	public static final ConfigKey<String> POKEMON_TITLE_PEN = StringKey.of("", "&e{{pokemon}} {{shiny:s}}&7| &bLvl {{level}}");
	public static final ConfigKey<List<String>> POKEMON_LORE_PEN = ListKey.of("", Lists.newArrayList(
			"&7Ability: &e{{ability}}",
			"&7Gender: &e{{gender}}",
			"&7Nature: &e{{nature}}",
			"&7Size: &e{{growth}}",
			"",
			"&7IVs: &e{{ivs_total}}&7/&e186 &7(&a{{ivs_percent}}&7)",
			"&7IVs: &e{{ivhp}}&7/&e{{ivatk}}&7/&e{{ivdef}}&7/&e{{ivspatk}}&7/&e{{ivspdef}}&7/&e{{ivspeed}}"
	));
	public static final ConfigKey<List<String>> STATISTICS = ListKey.of("", Lists.newArrayList());
}
