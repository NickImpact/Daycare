package com.nickimpact.daycare.api.text;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import java.util.Map;
import java.util.Optional;

/**
 * (Some note will appear here)
 *
 * @author NickImpact (Nick DeGruccio)
 */
@FunctionalInterface
public interface Translator {
	Optional<Text> get(CommandSource source, String variableString, Map<String, Object> variables);
}
