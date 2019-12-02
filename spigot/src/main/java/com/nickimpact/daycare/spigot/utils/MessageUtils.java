package com.nickimpact.daycare.spigot.utils;

import org.bukkit.ChatColor;

import java.util.List;

public class MessageUtils {

	public static String parse(String input, boolean message, boolean error) {
		if(message) {
			if (error) {
				return ChatColor.YELLOW + "Daycare " + ChatColor.GRAY + "(" + ChatColor.RED + "Error" + ChatColor.GRAY + ") " + ChatColor.translateAlternateColorCodes('&', input);
			} else {
				return ChatColor.YELLOW + "Daycare " + ChatColor.GRAY + "\u00bb " + ChatColor.translateAlternateColorCodes('&', input);
			}
		} else {
			return ChatColor.translateAlternateColorCodes('&', input);
		}
	}

	public static String[] asArray(List<String> input) {
		return input.toArray(new String[]{});
	}

	public static String asSingleWithNewlines(List<String> list) {
		StringBuilder sb = new StringBuilder();
		if(list.size() > 0) {
			sb.append(list.get(0));
			for (int i = 1; i < list.size(); i++) {
				sb.append("\n").append(list.get(i));
			}
		}

		return sb.toString();
	}
}