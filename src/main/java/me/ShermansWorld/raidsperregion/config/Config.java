package me.ShermansWorld.raidsperregion.config;

import org.bukkit.configuration.file.FileConfiguration;

import me.ShermansWorld.raidsperregion.RaidsPerRegion;
import me.ShermansWorld.raidsperregion.raid.RaidMob;
import me.ShermansWorld.raidsperregion.raid.Raids;

public class Config {
	
	private static FileConfiguration config;
	
	// config version
	public static int configVersion = 1;
	
	// titles
	public static String raidStartTitle;
	public static String raidStartSubtitle;
	public static String raidWinTitle;
	public static String raidWinSubtitle;
	public static String raidLoseTitle;
	public static String raidLoseSubtitle;
	public static String raidStopTitle;
	public static String raidStopSubtitle;
	public static String raidBossTitle;
	public static String raidBossSubtitle;
	
	// tiers
	public static int tier1MaxMobs;
	public static int tier1KillsGoal;
	public static int tier1TimeLimit;
	public static int tier1MobSpawnsPerCycle;
	public static double tier1SpawnRate;
	public static double tier1MobLevel;
	public static boolean tier1HasBoss;
	public static String tier1BossName;
	public static int tier2MaxMobs;
	public static int tier2KillsGoal;
	public static int tier2TimeLimit;
	public static int tier2MobSpawnsPerCycle;
	public static double tier2SpawnRate;
	public static double tier2MobLevel;
	public static boolean tier2HasBoss;
	public static String tier2BossName;
	public static int tier3MaxMobs;
	public static int tier3KillsGoal;
	public static int tier3TimeLimit;
	public static int tier3MobSpawnsPerCycle;
	public static double tier3SpawnRate;
	public static double tier3MobLevel;
	public static boolean tier3HasBoss;
	public static String tier3BossName;
	
	// settings
	public static boolean preventVanillaMobsSpawningInRaids;
	public static boolean forceMobSpawningInRaids;
	public static boolean keepInventoryInRaids;
	public static boolean keepXPInRaids;
	public static boolean disablePvPInRaids;
	public static boolean keepMobsOnRaidLoss;
	
	
	public static void initConfigVals() {
		//init config
		config = RaidsPerRegion.getInstance().getConfig();
		
		//init variables
		configVersion = config.getInt("config-version");
		
		raidStartTitle = config.getString("RaidStartTitle");
		raidStartSubtitle = config.getString("RaidStartSubtitle");
		raidWinTitle = config.getString("RaidWinTitle");
		raidWinSubtitle = config.getString("RaidWinSubtitle");
		raidLoseTitle = config.getString("RaidLoseTitle");
		raidLoseSubtitle = config.getString("RaidLoseSubtitle");
		raidStopTitle = config.getString("RaidStopTitle");
		raidStopSubtitle = config.getString("RaidStopSubtitle");
		raidBossTitle = config.getString("RaidBossTitle");
		raidBossSubtitle = config.getString("RaidBossSubtitle");
		
		tier1MaxMobs = config.getInt("Tier1.MaxMobsAtOnce");
		tier1KillsGoal = config.getInt("Tier1.KillsGoal");
		tier1TimeLimit = config.getInt("Tier1.TimeLimit");
		tier1MobSpawnsPerCycle = config.getInt("Tier1.MobSpawnsPerCycle");
		tier1SpawnRate = config.getDouble("Tier1.SpawnRate");
		tier1MobLevel = config.getDouble("Tier1.MobLevel");
		tier1HasBoss = config.getBoolean("Tier1.HasBoss");
		tier1BossName = config.getString("Tier1.Boss");
		
		tier2MaxMobs = config.getInt("Tier2.MaxMobsAtOnce");
		tier2KillsGoal = config.getInt("Tier2.KillsGoal");
		tier2TimeLimit = config.getInt("Tier2.TimeLimit");
		tier2MobSpawnsPerCycle = config.getInt("Tier2.MobSpawnsPerCycle");
		tier2SpawnRate = config.getDouble("Tier2.SpawnRate");
		tier2MobLevel = config.getDouble("Tier2.MobLevel");
		tier2HasBoss = config.getBoolean("Tier2.HasBoss");
		tier2BossName = config.getString("Tier2.Boss");
		
		tier3MaxMobs = config.getInt("Tier3.MaxMobsAtOnce");
		tier3KillsGoal = config.getInt("Tier3.KillsGoal");
		tier3TimeLimit = config.getInt("Tier3.TimeLimit");
		tier3MobSpawnsPerCycle = config.getInt("Tier3.MobSpawnsPerCycle");
		tier3SpawnRate = config.getDouble("Tier3.SpawnRate");
		tier3MobLevel = config.getDouble("Tier3.MobLevel");
		tier3HasBoss = config.getBoolean("Tier3.HasBoss");
		tier3BossName = config.getString("Tier3.Boss");
	
		preventVanillaMobsSpawningInRaids = config.getBoolean("PreventVanillaMobsSpawningInRaids");
		forceMobSpawningInRaids = config.getBoolean("ForceMobSpawningInRaids");
		keepInventoryInRaids = config.getBoolean("KeepInventoryInRaids");
		keepXPInRaids = config.getBoolean("KeepXPInRaids");
		disablePvPInRaids = config.getBoolean("DisablePvPInRaids");
		keepMobsOnRaidLoss = config.getBoolean("KeepMobsOnRaidLoss");
		
		
		//get raid mobs from config
		Raids.raidMobs.clear();
		for (String mob : config.getConfigurationSection("RaidMobs").getKeys(false)) {
			// name, level, priority, chance
			double chance = config.getDouble("RaidMobs." + mob + ".Chance");
			int priority = config.getInt("RaidMobs." + mob + ".Priority");
			// name, priority, chance
			Raids.raidMobs.add(new RaidMob(mob, priority, chance));
		}
		
	}
}
