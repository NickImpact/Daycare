package com.nickimpact.daycare;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.logger.Logger;
import com.nickimpact.impactor.api.plugins.PluginInfo;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.List;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class DaycareInfo implements PluginInfo {

	public static final String ID = "daycare";

	public static final String NAME = "Daycare";
	public static final String VERSION = "S7.1-1.0.2";
	public static final String DESCRIPTION = "A Sponge Representation of the Pokemon Daycare";

	public static final Text PREFIX = Text.of(TextColors.YELLOW, "Daycare ", TextColors.GRAY, "\u00bb ", TextColors.DARK_AQUA);
	public static final Text ERROR_PREFIX = Text.of(
			TextColors.RED, "Daycare ", TextColors.GRAY, "(", TextColors.RED, "Error", TextColors.GRAY, ") ",
			TextColors.DARK_RED
	);
	public static final Text DEBUG_PREFIX = Text.of(
			TextColors.YELLOW, "Daycare ", TextColors.GRAY, "(", TextColors.RED, "Debug", TextColors.GRAY, ") ",
			TextColors.DARK_AQUA
	);

	@Override
	public String getID() {
		return ID;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	public enum Dependencies {
		Pixelmon("pixelmon", "6.x.x+");

		private String dependency;
		private String version;

		private Dependencies(String dependency, String version){
			this.dependency = dependency;
			this.version = version;
		}

		public String getDependency() {
			return dependency;
		}

		public String getVersion() {
			return version;
		}
	}

	static void startup(){
		List<String> banner = Lists.newArrayList(
				StringUtils.center("&eDaycare", 50),
				StringUtils.center("&eRise of the Doof Edition", 50),
				StringUtils.center("&aVersion: " + VERSION, 50),
				StringUtils.center("&aAuthor: NickImpact", 50),
				"",
				StringUtils.center("Now attempting to load internal components...", 50)
		);

		for(String s : banner)
			DaycarePlugin.getInstance().getLogger().send(Logger.Prefixes.NONE, TextSerializers.FORMATTING_CODE.deserialize(s));

		DaycarePlugin.getInstance().getLogger().send(Logger.Prefixes.NONE, Text.EMPTY);
	}

	static boolean dependencyCheck(){
		boolean valid = true;

		for(Dependencies dependency : Dependencies.values()){
			if(!Sponge.getPluginManager().isLoaded(dependency.getDependency())){
				DaycarePlugin.getInstance().getLogger().error(Text.of(TextColors.DARK_RED, "==== Missing Dependency ===="));
				DaycarePlugin.getInstance().getLogger().error(Text.of(TextColors.DARK_RED,  "  Dependency: ", TextColors.RED, dependency.name()));
				DaycarePlugin.getInstance().getLogger().error(Text.of(TextColors.DARK_RED,  "  Version: ", TextColors.RED, dependency.getVersion()));

				valid = false;
			}
		}
		return valid;
	}
}
