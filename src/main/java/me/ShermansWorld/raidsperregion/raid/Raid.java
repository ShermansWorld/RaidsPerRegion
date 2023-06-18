package me.ShermansWorld.raidsperregion.raid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.mrmicky.fastboard.FastBoard;
import io.lumine.mythic.core.mobs.ActiveMob;
import me.ShermansWorld.raidsperregion.RaidsPerRegion;
import me.ShermansWorld.raidsperregion.config.Config;
import me.ShermansWorld.raidsperregion.util.Helper;
import me.ShermansWorld.raidsperregion.util.ScoreboardUtil;

public abstract class Raid {

	// -----Raid parameters-----

	// Used to identify raids
	private int raidID;

	// Derived from region/town being raided
	private String name;

	// The person who started the raid
	private String owner;

	// The world that the raid is happening in
	private World world;

	// 3 Default tiers. Tiers define how 'strong' the raids will be
	private int tier;

	// Defined in seconds
	private int timeLimit;

	// Defined in seconds
	private int timeLeft;

	// The total amount of mobs allowed to be spawned at one point
	private int maxTotalMobs;

	// The Mythicmobs Mob level of each mob that spawns
	private double mobLevel;

	// How many mobs spawn (based on random player location) per cycle
	private double mobSpawnsPerCycle;

	// How long until the next spawn cycle (in seconds)
	private double spawnRate;

	// The amount of mobs that need to be killed to win the raid
	private int killsGoal;

	// The amount of mobs killed so far by players in the raid
	private int kills;

	// If the raid will spawn a boss once the kill limit has been reached
	private boolean hasBoss;

	// The name of the boss (if applicable). Must be a valid mythicmobs name
	private String bossName;

	// The activemob (essentially entity) of the boss. Set when the boss is spawned
	private ActiveMob bossMob;

	// If the boss has been spawned (if applicable)
	private boolean isBossSpawned;

	// If the boss entity has been killed
	private boolean isBossKilled;
	
	// Raid area has mob spawning enabled
	private boolean hasMobSpawning;

	// Scoreboard map (scoreboards are per player)
	private static HashMap<UUID, FastBoard> scoreboards = new HashMap<UUID, FastBoard>();

	// List of players who participate in a raid (defined as entering the raid
	// region).
	// Integer is the amount of mob kills by the player
	private HashMap<UUID, Integer> participantsKillsMap = new HashMap<UUID, Integer>();

	// List of players who are actively partcipating in the raid. Same as
	// particpants except when they leave the region they are removed from the list.
	private ArrayList<UUID> activeParticipants = new ArrayList<UUID>();

	// Mobs added as they spawn
	private HashSet<ActiveMob> mobs = new HashSet<ActiveMob>();

	public Raid(String owner, String name, World world, int tier) {
		this.owner = owner;
		this.world = world;
		this.name = name;
		this.tier = tier;

		// defaults
		hasBoss = false;
		bossName = "NULL";

		// get raid params based on the tier
		switch (tier) {
		case 1:
			maxTotalMobs = Config.tier1MaxMobs;
			killsGoal = Config.tier1KillsGoal;
			timeLimit = Config.tier1TimeLimit;
			mobSpawnsPerCycle = Config.tier1MobSpawnsPerCycle;
			spawnRate = Config.tier1SpawnRate;
			mobLevel = Config.tier1MobLevel;
			hasBoss = Config.tier1HasBoss;
			bossName = Config.tier1BossName;
			break;
		case 2:
			maxTotalMobs = Config.tier2MaxMobs;
			killsGoal = Config.tier2KillsGoal;
			timeLimit = Config.tier2TimeLimit;
			mobSpawnsPerCycle = Config.tier2MobSpawnsPerCycle;
			spawnRate = Config.tier2SpawnRate;
			mobLevel = Config.tier2MobLevel;
			hasBoss = Config.tier2HasBoss;
			bossName = Config.tier2BossName;
			break;
		case 3:
			maxTotalMobs = Config.tier3MaxMobs;
			killsGoal = Config.tier3KillsGoal;
			timeLimit = Config.tier3TimeLimit;
			mobSpawnsPerCycle = Config.tier3MobSpawnsPerCycle;
			spawnRate = Config.tier3SpawnRate;
			mobLevel = Config.tier3MobLevel;
			hasBoss = Config.tier3HasBoss;
			bossName = Config.tier3BossName;
			break;
		}

		timeLeft = timeLimit;
		kills = 0;

		isBossSpawned = false;
		isBossKilled = false;

	}

	// Methods
	public void sendTitleToParticipants(String titleFromConfig, String subtitleFromConfig) {
		String titleMsg = Helper.formatPlaceholders(titleFromConfig, this.getName(), this.getOwner(), this.getTier(),
				this.getBossName());
		String subtitleMsg = Helper.formatPlaceholders(subtitleFromConfig, this.getName(), this.getOwner(),
				this.getTier(), this.getBossName());
		for (UUID playerUUID : participantsKillsMap.keySet()) {
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
			if (offlinePlayer.isOnline()) {
				Player player = (Player) offlinePlayer;
				player.sendTitle(titleMsg, subtitleMsg, 10, 60, 10);
			}
		}
	}

	public void sendTitleToPlayer(Player player, String titleFromConfig, String subtitleFromConfig) {
		String titleMsg = Helper.formatPlaceholders(titleFromConfig, this.getName(), this.getOwner(), this.getTier(),
				this.getBossName());
		String subtitleMsg = Helper.formatPlaceholders(subtitleFromConfig, this.getName(), this.getOwner(),
				this.getTier(), this.getBossName());
		player.sendTitle(titleMsg, subtitleMsg, 10, 60, 10);
	}

	public void updateScoreboard(Player player) {
		ScoreboardUtil.updateBoard(ScoreboardUtil.getScoreboard(player, this), "&4&l----------------",
				"&aTime Left:  " + formattedTimeLeft(), "&6Kills Goal:  " + getKillLimit(),
				"&6Total Kills: " + getKills(), "&4&l----------------",
				"&eYour Kills: " + getParticipantsKillsMap().get(player.getUniqueId()));
	}

	public void createScoreboard(Player player) {
		ScoreboardUtil.createNewScoreboard(player, this,
				Helper.color("&4&lTier " + getTier() + " Raid - " + getName()));
	}

	public String formattedTimeLeft() {
		int hours = timeLeft / 3600;
		int minutes = (timeLeft % 3600) / 60;
		int seconds = timeLeft % 60;

		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}

	public void spawnMobsForAllActiveParticipants(int distanceFactor) {
		for (UUID playerUUID : activeParticipants) {
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
			if (offlinePlayer.isOnline()) {
				Player player = (Player) offlinePlayer;
				spawnMobForSpecficParticipant(distanceFactor, player);
			}
		}
	}

	public void spawnMobsForCycle(int distanceFactor) {
		for (int i = 0; i < mobSpawnsPerCycle; i++) {
			if (activeParticipants.isEmpty()) {
				return;
			}
			int randIndex = (int) (Math.random() * activeParticipants.size());
			UUID playerUUID = activeParticipants.get(randIndex);
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
			if (offlinePlayer.isOnline()) {
				Player player = (Player) offlinePlayer;
				spawnMobForSpecficParticipant(distanceFactor, player);
			}
		}
	}
	
	public void cleanStoredMobs() {
		Iterator<ActiveMob> iterator = mobs.iterator();
		while (iterator.hasNext()) {
			ActiveMob mob = iterator.next();
			if (mob == null) {
				iterator.remove();
				if (RaidsPerRegion.isInDebugMode) {
					Bukkit.broadcastMessage(Helper.color("&cnull mob removed from active raid mobs"));
				}
			} else if (mob.isDead()) {
				if (RaidsPerRegion.isInDebugMode) {
					Bukkit.broadcastMessage(Helper.color("&edead mob removed from active raid mobs"));
				}
				iterator.remove();
			}
		}
	}
	

	// Abstract methods
	public abstract void startRaid(CommandSender sender, boolean isConsole);

	public abstract void stopRaid();

	public abstract void findParticipants();

	public abstract void spawnMobForSpecficParticipant(int distanceFactor, Player player);

	public abstract void onRaidTimer();

	public abstract void onRaidWin();

	public abstract void onRaidLoss();

	public abstract boolean spawnBoss(int distanceFactor);
	
	public abstract void forceMobsSpawning();
	
	public abstract void resetMobsSpawning();

	// Getters and Setters

	public  String getName() {
		return name;
	}

	public String getOwner() {
		return owner;
	}

	public World getWorld() {
		return world;
	}

	public int getID() {
		return raidID;
	}

	public void setID(int id) {
		this.raidID = id;
	}

	public int getTier() {
		return tier;
	}

	public int getTimeLimit() {
		return timeLimit;
	}

	public int getTimeLeft() {
		return timeLeft;
	}

	public void setTimeLeft(int timeLeft) {
		this.timeLeft = timeLeft;
	}

	public int getMaxTotalMobs() {
		return maxTotalMobs;
	}

	public double getMobLevel() {
		return mobLevel;
	}

	public double mobSpawnsPerCycle() {
		return mobSpawnsPerCycle;
	}

	public double getSpawnRate() {
		return spawnRate;
	}

	public int getKillLimit() {
		return killsGoal;
	}

	public int getKills() {
		return kills;
	}

	public void setKills(int kills) {
		this.kills = kills;
	}

	public boolean hasBoss() {
		return hasBoss;
	}
	
	public boolean setHasBoss(boolean hasBoss) {
		return this.hasBoss = hasBoss;
	}

	public String getBossName() {
		return bossName;
	}

	public ActiveMob getBossMob() {
		return bossMob;
	}

	public void setBossMob(ActiveMob bossMob) {
		this.bossMob = bossMob;
	}

	public boolean isBossSpawned() {
		return isBossSpawned;
	}

	public void setBossSpawned(boolean isBossSpawned) {
		this.isBossSpawned = isBossSpawned;
	}

	public boolean hasBossSpawned() {
		return isBossSpawned;
	}

	public boolean isBossKilled() {
		return isBossKilled;
	}
	
	public boolean getMobSpawning() {
		return hasMobSpawning;
	}
	
	public void setMobSpawning(boolean hasMobSpawning) {
		this.hasMobSpawning = hasMobSpawning;
	}

	public void setBossKilled(boolean isBossKilled) {
		this.isBossKilled = isBossKilled;
	}

	public HashMap<UUID, Integer> getParticipantsKillsMap() {
		return participantsKillsMap;
	}

	public HashMap<UUID, FastBoard> getScoreboards() {
		return scoreboards;
	}

	public ArrayList<UUID> getActiveParticipants() {
		return activeParticipants;
	}

	public HashSet<ActiveMob> getMobs() {
		return mobs;
	}

}
