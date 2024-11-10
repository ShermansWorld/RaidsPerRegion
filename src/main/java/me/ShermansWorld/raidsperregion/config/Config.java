package me.ShermansWorld.raidsperregion.config;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.ShermansWorld.raidsperregion.RaidsPerRegion;
import me.ShermansWorld.raidsperregion.raid.RaidMob;
import me.ShermansWorld.raidsperregion.raid.Raids;

public class Config {

	private static FileConfiguration config;
	private static String CONFIG_FOLDER = "./configFiles";

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
	public static int raidTiers;
//	public static int tier1MaxMobs;
//	public static int tier1KillsGoal;
//	public static int tier1TimeLimit;
//	public static int tier1MobSpawnsPerCycle;
//	public static double tier1SpawnRate;
//	public static double tier1MobLevel;
//	public static boolean tier1HasBoss;
//	public static String tier1BossName;
//	public static int tier2MaxMobs;
//	public static int tier2KillsGoal;
//	public static int tier2TimeLimit;
//	public static int tier2MobSpawnsPerCycle;
//	public static double tier2SpawnRate;
//	public static double tier2MobLevel;
//	public static boolean tier2HasBoss;
//	public static String tier2BossName;
//	public static int tier3MaxMobs;
//	public static int tier3KillsGoal;
//	public static int tier3TimeLimit;
//	public static int tier3MobSpawnsPerCycle;
//	public static double tier3SpawnRate;
//	public static double tier3MobLevel;
//	public static boolean tier3HasBoss;
//	public static String tier3BossName;

	// TODO
	public static int[] tierMaxMobs;
	public static int[] tierKillsGoal;
	public static int[] tierTimeLimit;
	public static int[] tierMobSpawnsPerCycle;
	public static double[] tierSpawnRate;
	public static double[] tierMobLevel;
	public static boolean[] tierHasBoss;
	public static String[] tierBossName;


	// other settings
	public static boolean preventVanillaMobsSpawningInRaids;
	public static boolean forceMobSpawningInRaids;
	public static boolean keepInventoryInRaids;
	public static boolean keepXPInRaids;
	public static boolean disablePvPInRaids;
	public static boolean keepMobsOnRaidLoss;

	// commands
	public static boolean useWinLossCommands;
	public static ArrayList<String> globalWinCommands;
	public static ArrayList<String> globalLossCommands;
	public static ArrayList<String> perPlayerWinCommands;
	public static ArrayList<String> perPlayerLossCommands;


	public static void initConfigVals(String configFileName) {
		try {
			if (configFileName != null) {
				File currConfig = new File(CONFIG_FOLDER, configFileName + ".yml");
				FileConfiguration newConfig = YamlConfiguration.loadConfiguration(currConfig);
				config = newConfig;
			} else {
				//init config
				config =  RaidsPerRegion.getInstance().getConfig();
			}
		} catch (Exception e) {
			// TODO
			System.out.println("Invalid Config");
		}

		// config version
		configVersion = config.getInt("config-version");
		
		// title settings
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
		
		// Initialize Arrays
		tierMaxMobs = new int[raidTiers];
		tierKillsGoal = new int[raidTiers];;
		tierTimeLimit = new int[raidTiers];;
		tierMobSpawnsPerCycle = new int[raidTiers];;
		tierSpawnRate = new double[raidTiers];;
		tierMobLevel = new double[raidTiers];;
		tierHasBoss = new boolean[raidTiers];;
		tierBossName = new String[raidTiers];;
		
		// tier settings
		// TODO
		raidTiers = config.getInt("RaidTierAmount");
		for (int i = 0; i < raidTiers; i++) {
			String tierString = "Tier" + i;
			tierMaxMobs[i] = config.getInt(tierString + ".MaxMobsAtOnce");
			tierKillsGoal[i] = config.getInt(tierString + ".KillsGoal");
			tierTimeLimit[i] = config.getInt(tierString + ".TimeLimit");
			tierMobSpawnsPerCycle[i] = config.getInt(tierString + ".MobSpawnsPerCycle");
			tierSpawnRate[i] = config.getDouble(tierString + ".SpawnRate");
			tierMobLevel[i] = config.getDouble(tierString + ".MobLevel");
			tierHasBoss[i] = config.getBoolean(tierString + ".HasBoss");
			tierBossName[i] = config.getString(tierString + ".Boss");			
		}
		
		// tier settings
//		tier1MaxMobs = config.getInt("Tier1.MaxMobsAtOnce");
//		tier1KillsGoal = config.getInt("Tier1.KillsGoal");
//		tier1TimeLimit = config.getInt("Tier1.TimeLimit");
//		tier1MobSpawnsPerCycle = config.getInt("Tier1.MobSpawnsPerCycle");
//		tier1SpawnRate = config.getDouble("Tier1.SpawnRate");
//		tier1MobLevel = config.getDouble("Tier1.MobLevel");
//		tier1HasBoss = config.getBoolean("Tier1.HasBoss");
//		tier1BossName = config.getString("Tier1.Boss");
//		
//		tier2MaxMobs = config.getInt("Tier2.MaxMobsAtOnce");
//		tier2KillsGoal = config.getInt("Tier2.KillsGoal");
//		tier2TimeLimit = config.getInt("Tier2.TimeLimit");
//		tier2MobSpawnsPerCycle = config.getInt("Tier2.MobSpawnsPerCycle");
//		tier2SpawnRate = config.getDouble("Tier2.SpawnRate");
//		tier2MobLevel = config.getDouble("Tier2.MobLevel");
//		tier2HasBoss = config.getBoolean("Tier2.HasBoss");
//		tier2BossName = config.getString("Tier2.Boss");
//		
//		tier3MaxMobs = config.getInt("Tier3.MaxMobsAtOnce");
//		tier3KillsGoal = config.getInt("Tier3.KillsGoal");
//		tier3TimeLimit = config.getInt("Tier3.TimeLimit");
//		tier3MobSpawnsPerCycle = config.getInt("Tier3.MobSpawnsPerCycle");
//		tier3SpawnRate = config.getDouble("Tier3.SpawnRate");
//		tier3MobLevel = config.getDouble("Tier3.MobLevel");
//		tier3HasBoss = config.getBoolean("Tier3.HasBoss");
//		tier3BossName = config.getString("Tier3.Boss");
		
		// Other settings
		preventVanillaMobsSpawningInRaids = config.getBoolean("PreventVanillaMobsSpawningInRaids");
		forceMobSpawningInRaids = config.getBoolean("ForceMobSpawningInRaids");
		keepInventoryInRaids = config.getBoolean("KeepInventoryInRaids");
		keepXPInRaids = config.getBoolean("KeepXPInRaids");
		disablePvPInRaids = config.getBoolean("DisablePvPInRaids");
		keepMobsOnRaidLoss = config.getBoolean("KeepMobsOnRaidLoss");
		
		// commands
		useWinLossCommands = config.getBoolean("UseWinLossCommands");
		globalWinCommands = (ArrayList<String>) config.getStringList("RaidWinCommands.Global");
		globalLossCommands = (ArrayList<String>) config.getStringList("RaidLossCommands.Global");
		perPlayerWinCommands = (ArrayList<String>) config.getStringList("RaidWinCommands.PerPlayer");
		perPlayerLossCommands = (ArrayList<String>) config.getStringList("RaidLossCommands.PerPlayer");
		
		// raid mobs
		
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
