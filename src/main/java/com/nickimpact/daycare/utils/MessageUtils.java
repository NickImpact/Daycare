package com.nickimpact.daycare.utils;

import com.google.common.collect.Lists;
import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.PluginInfo;
import org.spongepowered.api.text.Text;

import java.util.List;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class MessageUtils {

	public static List<Text> genErrorMessage(String label, String... info) {
		List<Text> error = Lists.newArrayList(
				Text.of(PluginInfo.ERROR_PREFIX, "========== ", label, " ==========")
		);

		for(String str : info) {
			error.add(Text.of(PluginInfo.ERROR_PREFIX, str));
		}

		return error;
	}

	public static void genAndSendErrorMessage(String label, String... info) {
		DaycarePlugin.getInstance().getConsole().ifPresent(console -> console.sendMessages(genErrorMessage(label, info)));
	}
}
