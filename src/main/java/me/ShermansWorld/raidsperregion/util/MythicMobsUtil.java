package me.ShermansWorld.raidsperregion.util;

import org.bukkit.Location;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.mobs.MobExecutor;
import me.ShermansWorld.raidsperregion.raid.RaidMob;
import me.ShermansWorld.raidsperregion.raid.Raids;

public class MythicMobsUtil {
	
	static MobExecutor mobManager;
	
	public static void init() {
		mobManager = MythicBukkit.inst().getMobManager();
	}
	
	public static ActiveMob spawnMob(String mobName, Location loc) {
		return mobManager.spawnMob(mobName, loc);
	}
	
	public static ActiveMob spawnMob(String mobName, Location loc, double level) {
		return mobManager.spawnMob(mobName, loc, level);
	}
	
	public static ActiveMob spawnRandomMob(Location loc) {
		// random number between 0 and 1
		double random = Math.random();
		
		int topPriority = 1;
		String mobName = Raids.raidMobs.get(0).getName();
		for (RaidMob raidMob : Raids.raidMobs) {
			if (random < raidMob.getChance()) {
				if (raidMob.getPriority() >= topPriority) {
					mobName = raidMob.getName();
				}
			}
		}
		return spawnMob(mobName, loc);
	}
	
	public static ActiveMob spawnRandomMob(Location loc, double level) {
		// random number between 0 and 1
		double random = Math.random();
		
		int topPriority = 0;
		String mobName = Raids.raidMobs.get(0).getName();
		for (RaidMob raidMob : Raids.raidMobs) {
			if (random < raidMob.getChance()) {
				if (raidMob.getPriority() >= topPriority) {
					mobName = raidMob.getName();
				}
			}
		}
		//Bukkit.broadcastMessage(mobName);
		return spawnMob(mobName, loc, level);
	}
	
	public static MobExecutor getMobManager() {
		return mobManager;
	}
}
