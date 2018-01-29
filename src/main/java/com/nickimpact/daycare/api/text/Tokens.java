package com.nickimpact.daycare.api.text;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.nickimpact.daycare.DaycarePlugin;
import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import io.github.nucleuspowered.nucleus.api.exceptions.PluginAlreadyRegisteredException;
import io.github.nucleuspowered.nucleus.api.service.NucleusMessageTokenService;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import javax.annotation.Nonnull;
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
		translatorMap.put("", (p, v, m) -> Optional.empty());
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
