package com.nickimpact.daycare.utils;

import com.google.common.collect.Lists;
import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.DaycareInfo;
import com.nickimpact.daycare.configuration.MsgConfigKeys;
import com.nickimpact.impactor.api.configuration.ConfigKey;
import com.nickimpact.impactor.api.logger.Logger;
import io.github.nucleuspowered.nucleus.api.exceptions.NucleusException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class MessageUtils {

	public static List<Text> genErrorMessage(String label, String... info) {
		List<Text> error = Lists.newArrayList(
				Text.of("========== ", label, " ==========")
		);

		for(String str : info) {
			error.add(Text.of(str));
		}

		return error;
	}

	public static void genAndSendErrorMessage(String label, String... info) {
		DaycarePlugin.getInstance().getLogger().send(Logger.Prefixes.DEBUG, genErrorMessage(label, info));
	}

	public static Text fetchMsg(ConfigKey<String> key) {
		return TextSerializers.FORMATTING_CODE.deserialize(DaycarePlugin.getInstance().getMsgConfig().get(key));
	}

	public static Text fetchMsg(CommandSource source, ConfigKey<String> key) {
		return fetchAndParseMsg(source, key, null, null);
	}

	public static Text fetchAndParseMsg(CommandSource source, ConfigKey<String> key, @Nullable Map<String, Function<CommandSource, Optional<Text>>> tokens, @Nullable Map<String, Object> variables) {
		try {
			return DaycarePlugin.getInstance().getTextParsingUtils().parse(
					DaycarePlugin.getInstance().getMsgConfig().get(key),
					source,
					tokens,
					variables
			);
		} catch (NucleusException e) {
			return Text.of(TextColors.RED, "An error occurred during parsing");
		}
	}

	public static List<Text> fetchMsgs(ConfigKey<List<String>> key) {
		List<Text> output = Lists.newArrayList();
		for(String str : DaycarePlugin.getInstance().getMsgConfig().get(key)) {
			output.add(TextSerializers.FORMATTING_CODE.deserialize(str));
		}
		return output;
	}

	public static List<Text> fetchMsgs(CommandSource source, ConfigKey<List<String>> key) {
		return fetchAndParseMsgs(source, key, null, null);
	}

	public static List<Text> fetchAndParseMsgs(CommandSource source, ConfigKey<List<String>> key, @Nullable Map<String, Function<CommandSource, Optional<Text>>> tokens, @Nullable Map<String, Object> variables) {
		try {
			return DaycarePlugin.getInstance().getTextParsingUtils().parse(
					DaycarePlugin.getInstance().getMsgConfig().get(key),
					source,
					tokens,
					variables
			);
		} catch (NucleusException e) {
			return Lists.newArrayList(Text.of(TextColors.RED, "An error occurred during parsing"));
		}
	}
}
