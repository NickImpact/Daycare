package com.nickimpact.daycare;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.regex.Pattern;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class PluginInfo {

	public static final String ID = "daycare";

	public static final String NAME = "Daycare";
	public static final String VERSION = "0.0.1-S7.1";
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
		String logo = "\n" +
				"    &e  _______ .___________.    _______.\n" +
				"    &e /  _____||           |   /       |\n" +
				"    &e|  |  __  `---|  |----`  |   (----`\n" +
				"    &e|  | |_ |     |  |        \\   \\    \n" +
				"    &e|  |__| |     |  |    .----)   |   \n" +
				"    &e \\______|     |__|    |_______/    \n ";

		for(String s : logo.split(Pattern.quote("\n")))
			DaycarePlugin.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(
					TextSerializers.FORMATTING_CODE.deserialize(s))));

		DaycarePlugin.getInstance().getConsole().ifPresent(console -> console.sendMessages(
				Text.of("    ", TextColors.YELLOW, "Rise of the Doof Edition"),
				Text.EMPTY,
				Text.of("    ", TextColors.GREEN, "Author:  ", TextColors.AQUA, "NickImpact"),
				Text.of("    ", TextColors.GREEN, "Version: ", TextColors.AQUA, VERSION),
				Text.EMPTY
		));

		DaycarePlugin.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.EMPTY));
	}

	static boolean dependencyCheck(){
		boolean valid = true;

		for(Dependencies dependency : Dependencies.values()){
			if(!Sponge.getPluginManager().isLoaded(dependency.getDependency())){
				DaycarePlugin.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(ERROR_PREFIX, Text.of(TextColors.DARK_RED, "==== Missing Dependency ===="))));
				DaycarePlugin.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(ERROR_PREFIX, Text.of(TextColors.DARK_RED, "  Dependency: ", TextColors.RED, dependency.name()))));
				DaycarePlugin.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(ERROR_PREFIX, Text.of(TextColors.DARK_RED, "  Version: ", TextColors.RED, dependency.getVersion()))));

				valid = false;
			}
		}
		return valid;
	}
}
