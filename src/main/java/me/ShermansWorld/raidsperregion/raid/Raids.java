package me.ShermansWorld.raidsperregion.raid;

import java.util.ArrayList;

import me.ShermansWorld.raidsperregion.towny.TownRaid;

// This is a data (static) class for all raids

public class Raids {
	
	public static int globalRaidID = 0;
	public static ArrayList<RegionRaid> activeRegionRaids = new ArrayList<RegionRaid>();
	public static ArrayList<TownRaid> activeTownRaids = new ArrayList<TownRaid>();
	public static ArrayList<RaidMob> raidMobs = new ArrayList<RaidMob>();
	
}
