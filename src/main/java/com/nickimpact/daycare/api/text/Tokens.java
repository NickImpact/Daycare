package com.nickimpact.daycare.api.text;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.configuration.MsgConfigKeys;
import com.nickimpact.daycare.internal.PokemonTokens;
import com.nickimpact.daycare.ranch.Ranch;
import com.nickimpact.daycare.stats.Statistics;
import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import io.github.nucleuspowered.nucleus.api.exceptions.PluginAlreadyRegisteredException;
import io.github.nucleuspowered.nucleus.api.service.NucleusMessageTokenService;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * (Some note will appear here)
 *
 * @author NickImpact (Nick DeGruccio)
 */
public class Tokens implements NucleusMessageTokenService.TokenParser {

	private final Map<String, Translator> translatorMap = Maps.newHashMap();

	public Tokens() {
		translatorMap.put("stats_eggs_ratio", (p, v, m) -> {
			Optional<Ranch> ranch = DaycarePlugin.getInstance().getRanches().stream().filter(r ->
				r.getOwnerUUID().equals(((Player) p).getUniqueId())
			).findAny();

			DecimalFormat df = new DecimalFormat("#0.##");

			return ranch.map(ranch1 -> Optional.of(Text.of(df.format(ranch1.getStats().getEggCollectionRatio()), "% Collected"))).orElseGet(() -> Optional.of(Text.of(df.format(0), "% Collected")));
		});
		translatorMap.put("stats_eggs_collected", (p, v, m) -> {
			Optional<Ranch> ranch = DaycarePlugin.getInstance().getRanches().stream().filter(r ->
					r.getOwnerUUID().equals(((Player) p).getUniqueId())
			).findAny();

			return ranch.map(ranch1 -> Optional.of(Text.of(ranch1.getStats().getStat(Statistics.Stats.EGGS_COLLECTED)))).orElseGet(() -> Optional.of(Text.of(0)));
		});
		translatorMap.put("stats_eggs_dismissed", (p, v, m) -> {
			Optional<Ranch> ranch = DaycarePlugin.getInstance().getRanches().stream().filter(r ->
					r.getOwnerUUID().equals(((Player) p).getUniqueId())
			).findAny();

			return ranch.map(ranch1 -> Optional.of(Text.of(ranch1.getStats().getStat(Statistics.Stats.EGGS_DELETED)))).orElseGet(() -> Optional.of(Text.of(0)));
		});
		translatorMap.put("stats_num_gained_lvls", (p, v, m) -> {
			Optional<Ranch> ranch = DaycarePlugin.getInstance().getRanches().stream().filter(r ->
					r.getOwnerUUID().equals(((Player) p).getUniqueId())
			).findAny();

			return ranch.map(ranch1 -> Optional.of(Text.of(ranch1.getStats().getStat(Statistics.Stats.NUM_GAINED_LVLS)))).orElseGet(() -> Optional.of(Text.of(0)));
		});
		translatorMap.put("daycare_prefix", (p, v, m) -> Optional.of(TextSerializers.FORMATTING_CODE.deserialize(DaycarePlugin.getInstance().getMsgConfig().get(MsgConfigKeys.PLUGIN_PREFIX))));
		translatorMap.put("daycare_error", (p, v, m) -> Optional.of(TextSerializers.FORMATTING_CODE.deserialize(DaycarePlugin.getInstance().getMsgConfig().get(MsgConfigKeys.PLUGIN_ERROR))));
		translatorMap.putAll(PokemonTokens.getTokens());
		try {
			NucleusAPI.getMessageTokenService().register(
					DaycarePlugin.getInstance().getPluginContainer(),
					this
			);
			this.getTokenNames().forEach(x -> NucleusAPI.getMessageTokenService().registerPrimaryToken(x.toLowerCase(), DaycarePlugin.getInstance().getPluginContainer(), x.toLowerCase()));
		} catch (PluginAlreadyRegisteredException e) {
			e.printStackTrace();
		}
	}

	private static CommandSource getSourceFromVariableIfExists(CommandSource source, String v, Map<String, Object> m) {
		if (m.containsKey(v) && m.get(v) instanceof CommandSource) {
			return (CommandSource)m.get(v);
		}

		return source;
	}

	private Set<String> getTokenNames() {
		return Sets.newHashSet(translatorMap.keySet());
	}

	@Nonnull
	@Override
	public Optional<Text> parse(String tokenInput, CommandSource source, Map<String, Object> variables) {
		String[] split = tokenInput.split("\\|", 2);
		String var = "";
		if (split.length == 2) {
			var = split[1];
		}

		return translatorMap.getOrDefault(split[0].toLowerCase(), (p, v, m) -> Optional.empty()).get(source, var, variables);
	}
}
