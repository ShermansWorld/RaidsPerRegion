package me.ShermansWorld.raidsperregion.util;

import org.bukkit.ChatColor;

public class Helper {
	
	public static String color(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}
	
	public static String chatLabel() {
		return color("&e[&cRaidsPerRegion&e]&r ");
	}
	
	public static String chatLabelConsole() {
		return "[RaidsPerRegion]";
	}
	
	public static String chatLabelError() {
		return color("&e[&cRaidsPerRegion&e]&c ");
	}
	
	public static String chatLabelSuccess() {
		return color("&e[&cRaidsPerRegion&e]&a ");
	}
	
	public static String chatLabelNormal() {
		return color("&e[&cRaidsPerRegion&e]&e ");
	}
	
	public static String formatPlaceholders (String titleStr, String raidName, String owner, int tier, String bossName) {
		if (titleStr.contains("@OWNER")) {
			titleStr = titleStr.replaceAll("@OWNER", owner);
		}
		if (titleStr.contains("@TIER")) {
			titleStr = titleStr.replaceAll("@TIER", String.valueOf(tier));
		}
		if (titleStr.contains("@NAME")) {
			titleStr = titleStr.replaceAll("@NAME", raidName);
		}
		if (titleStr.contains("@BOSS")) {
			titleStr = titleStr.replaceAll("@BOSS", bossName);
		}
		return Helper.color(titleStr);
	}
}
