package com.nickimpact.daycare.text;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.nickimpact.daycare.SpongeDaycarePlugin;
import com.nickimpact.daycare.api.pens.Pen;
import com.nickimpact.daycare.api.pens.Ranch;
import com.nickimpact.daycare.api.pens.Statistics;
import com.nickimpact.daycare.configuration.MsgConfigKeys;
import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import io.github.nucleuspowered.nucleus.api.exceptions.PluginAlreadyRegisteredException;
import io.github.nucleuspowered.nucleus.api.service.NucleusMessageTokenService;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DaycareTokens implements TokenHolder, NucleusMessageTokenService.TokenParser {

	private static Map<String, Translator> tokens = Maps.newHashMap();

	private static final DecimalFormat df = new DecimalFormat("#0.##");
	public static final DateTimeFormatter base = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss a z").withZone(ZoneId.systemDefault());

	private SpongeDaycarePlugin plugin;

	public Map<String, Translator> getTokens() {
		return tokens;
	}

	public DaycareTokens(SpongeDaycarePlugin plugin) {
		this.plugin = plugin;

		tokens.put("daycare_prefix", (p, v, m) -> Optional.of(TextSerializers.FORMATTING_CODE.deserialize(
					plugin.getMsgConfig().get(MsgConfigKeys.PLUGIN_PREFIX)
		)));
		tokens.put("daycare_error", (p, v, m) -> Optional.of(TextSerializers.FORMATTING_CODE.deserialize(
				plugin.getMsgConfig().get(MsgConfigKeys.PLUGIN_ERROR)
		)));
		tokens.put("stats_egg_ratio", (p, v, m) -> {
			Ranch ranch = getRanchFromVariableIfExists(m);
			return Optional.of(Text.of(Text.of(df.format(ranch.getStats().getEggCollectionRatio()), "% Collected")));
		});
		tokens.put("stats_egg_collected", (p, v, m) -> {
			Ranch ranch = getRanchFromVariableIfExists(m);
			return Optional.of(Text.of(Text.of(ranch.getStats().getStat(Statistics.Stats.EGGS_COLLECTED))));
		});
		tokens.put("stats_egg_dismissed", (p, v, m) -> {
			Ranch ranch = getRanchFromVariableIfExists(m);
			return Optional.of(Text.of(Text.of(ranch.getStats().getStat(Statistics.Stats.EGGS_DELETED))));
		});
		tokens.put("stats_num_gained_lvls", (p, v, m) -> {
			Ranch ranch = getRanchFromVariableIfExists(m);
			if(ranch != null) {
				return Optional.of(Text.of(Text.of(ranch.getStats().getStat(Statistics.Stats.NUM_GAINED_LVLS))));
			}

			return Optional.empty();
		});
		tokens.put("pen_unlock_date", (p, v, m) -> {
			Pen pen = getPenFromVariableIfExists(m);
			if(pen != null) {
				return Optional.of(Text.of(base.format(pen.getDateUnlocked())));
			}

			return Optional.empty();
		});

		try {
			NucleusAPI.getMessageTokenService().register(
					plugin.getPluginContainer(),
					this
			);
			this.getTokenNames().forEach(x -> NucleusAPI.getMessageTokenService().registerPrimaryToken(x.toLowerCase(), plugin.getPluginContainer(), x.toLowerCase()));
		} catch (PluginAlreadyRegisteredException e) {
			e.printStackTrace();
		}
	}

	public boolean register(String key, Translator translator) {
		if(NucleusAPI.getMessageTokenService().registerPrimaryToken(key.toLowerCase(), plugin.getPluginContainer(), key.toLowerCase())) {
			tokens.put(key, translator);
			return true;
		}

		return false;
	}

	public Set<String> getTokenNames() {
		return Sets.newHashSet(tokens.keySet());
	}

	@Nonnull
	@Override
	public Optional<Text> parse(String tokenInput, CommandSource source, Map<String, Object> variables) {
		String[] split = tokenInput.split("\\|", 2);
		String var = "";
		if (split.length == 2) {
			var = split[1];
		}

		return tokens.getOrDefault(split[0].toLowerCase(), (p, v, m) -> Optional.empty()).get(source, var, variables);
	}

	private static Ranch getRanchFromVariableIfExists(Map<String, Object> m) {
		return (Ranch) m.values().stream().filter(val -> val instanceof Ranch).findAny().orElse(null);
	}

	private static Pen getPenFromVariableIfExists(Map<String, Object> m) {
		return (Pen) m.values().stream().filter(val -> val instanceof Pen).findAny().orElse(null);

	}
}
