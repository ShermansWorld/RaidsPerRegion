package me.ShermansWorld.RaidsPerRegion.Raid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import me.ShermansWorld.RaidsPerRegion.Main;

public class Raid {
	
	// Raid command variables
	public static boolean timeReached = false;
	public static int totalKills;
	public static boolean maxMobsReached = false;
	public static List<String> mMMobNames = new ArrayList<>();
	public static List<Double> chances = new ArrayList<>();
	public static List<Integer> priorities = new ArrayList<>();
	public static boolean runOnce = false;
	public static int countdown;
	public static String tempStr = "";
	public static String tempStr2 = "";
	public static Map<String, String> scoreboardPlayerData = new HashMap<String, String>();
	public static int minutes;
	public static boolean hasMobsOn;
	public static boolean isScheduled = false;

	// public variables used in Listener Class
	public static List<Player> playersInRegion = new ArrayList<>();
	public static Map<String, Integer> raidKills = new HashMap<String, Integer>();
	public static int otherDeaths = 0;
	public static List<AbstractEntity> MmEntityList = new ArrayList<>();
	public static int mobsSpawned = 0;
	public static boolean bossSpawned = false;
	public static String boss = "NONE";
	public static int mobLevel = 1;
	public static AbstractEntity bossEntity;
	public static int tier = 1;

	// regions and towns
	public static ProtectedRegion region;
	public static Town town;

	// other (previous local) - Initial values for tiers from config
	public static int goal;
	public static int maxMobsPerPlayer = 10;
	public static double spawnRateMultiplier = 1.0;
	public static long conversionSpawnRateMultiplier = 10;
	
	
	// spawnMobs Method
		public static void spawnMobs(Random rand, List<Location> regionPlayerLocations, int scoreCounter,
				List<String> mMMobNames, List<Double> chances, List<Integer> priorities, int maxMobsPerPlayer, int mobLevel,
				Scoreboard board, Objective objective) {

			for (int j = 0; j < Raid.playersInRegion.size(); j++) {
				int randomPlayerIdx = rand.nextInt(Raid.playersInRegion.size());
				World w = Raid.playersInRegion.get(j).getWorld();
				int x = regionPlayerLocations.get(randomPlayerIdx).getBlockX() + rand.nextInt(50) - 25;
				int y = regionPlayerLocations.get(randomPlayerIdx).getBlockY();
				int z = regionPlayerLocations.get(randomPlayerIdx).getBlockZ() + rand.nextInt(50) - 25;
				String mythicMobName;
				int spawnRate = rand.nextInt(3); // 1/3 chance of spawning zombie at this player per cycle. possibilities:
													// 0, 1 or 2
				int numPlayersInRegion = Raid.playersInRegion.size();
				int mobsAlive = Raid.mobsSpawned - scoreCounter - Raid.otherDeaths;

				if (mobsAlive >= numPlayersInRegion * maxMobsPerPlayer) {
					Raid.maxMobsReached = true;
				} else {
					Raid.maxMobsReached = false;
				}
				if (spawnRate == 2 && !Raid.maxMobsReached) {
					List<Integer> hitIdxs = new ArrayList<>();
					for (int k = 0; k < mMMobNames.size(); k++) {
						int randomNum = rand.nextInt(1000) + 1; // generates number between 1 and 1000
						if (randomNum <= chances.get(k) * 1000) { // test for hit
							hitIdxs.add(k); // add hit index
						}
					}
					int maxPriority = 0;
					int maxPriorityIdx = 0;
					for (int n = 0; n < hitIdxs.size(); n++) {
						if (priorities.get(hitIdxs.get(n)) > maxPriority) { // does not account for same priority
							maxPriority = priorities.get(hitIdxs.get(n));
							maxPriorityIdx = hitIdxs.get(n);
						}
					}

					mythicMobName = mMMobNames.get(maxPriorityIdx); // set to first idx if nothing else hits
					// Fix mob spawning in ground or in air and taking fall damage
					if (w.getBlockAt(x, y, z).getType() == Material.AIR) {
						while (w.getBlockAt(x, y, z).getType() == Material.AIR) {
							y--;
						}
						y += 2; // fixes mobs spawning half in a block
					} else {
						while (w.getBlockAt(x, y, z).getType() != Material.AIR) {
							y++;
						}
						y++; // fixes mobs spawning half in a block
					}

					Location mobSpawnLocation = new Location(w, x, y, z);
					
					ActiveMob mob = MythicBukkit.inst().getMobManager().spawnMob(mythicMobName, mobSpawnLocation, mobLevel);
					try {
						AbstractEntity entityOfMob = mob.getEntity();
						Raid.MmEntityList.add(entityOfMob);
						Raid.mobsSpawned++;

					} catch (NullPointerException e) {
					}

				}
				board.resetScores(Raid.tempStr2);
				Raid.totalKills = scoreCounter;
				Score tempTotalScore = objective
						.getScore(ChatColor.AQUA + "Total Kills:      " + String.valueOf(Raid.totalKills));
				tempTotalScore.setScore(3);
				Raid.tempStr2 = ChatColor.AQUA + "Total Kills:      " + String.valueOf(Raid.totalKills);
			}
		}
		
		// checkPlayersInTown Method
		public static void checkPlayersInTown(Scoreboard board, Objective objective, List<String> mMMobNames,
				List<Double> chances, List<Integer> priorities, int maxMobsPerPlayer, long conversionSpawnRateMultiplier,
				int mobLevel) {
			int[] id = { 0 };
			Random rand = new Random();
			id[0] = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), new Runnable() {
				public void run() {
					if (Raid.timeReached == true) {
						Bukkit.getServer().getScheduler().cancelTask(id[0]);
					} else {
						List<Player> playerList = new ArrayList<>(Bukkit.getOnlinePlayers());
						Raid.playersInRegion = new ArrayList<>();
						List<Location> regionPlayerLocations = new ArrayList<>();
						int scoreCounter = 0;
						Town currentTown = null; // placeholder
						for (int i = 0; i < playerList.size(); i++) {
							try {
								currentTown = WorldCoord.parseWorldCoord(playerList.get(i).getLocation()).getTownBlock()
										.getTown();
							} catch (NotRegisteredException e) {
								// e.printStackTrace();
								// playerList.get(i).sendMessage("You are not in a town!");
							}
							if (currentTown == Raid.town) {
								Raid.playersInRegion.add(playerList.get(i));
								regionPlayerLocations.add(playerList.get(i).getLocation());
							}
						}

						// Add up scores for all players in region
						for (int n = 0; n < Raid.playersInRegion.size(); n++) {

							// Make sure the player is mapped in raidKills or it will return a null error

							if (Raid.playersInRegion.get(n).getScoreboard() != board) {
								Raid.playersInRegion.get(n).setScoreboard(board);
							}

							if (Raid.raidKills.containsKey(Raid.playersInRegion.get(n).getName())) {
								if (Raid.scoreboardPlayerData.containsKey(Raid.playersInRegion.get(n).getName())) {
									board.resetScores(Raid.scoreboardPlayerData.get(Raid.playersInRegion.get(n).getName()));
								}
								Score score = objective.getScore(ChatColor.YELLOW + Raid.playersInRegion.get(n).getName()
										+ ":    " + Raid.raidKills.get(Raid.playersInRegion.get(n).getName()));
								Raid.scoreboardPlayerData.put(Raid.playersInRegion.get(n).getName(),
										ChatColor.YELLOW + Raid.playersInRegion.get(n).getName() + ":    "
												+ Raid.raidKills.get(Raid.playersInRegion.get(n).getName()));
								score.setScore(0);
								scoreCounter += Raid.raidKills.get(Raid.playersInRegion.get(n).getName());
							}

						}
						// Spawn mobs for all players in region
						Raid.spawnMobs(rand, regionPlayerLocations, scoreCounter, mMMobNames, chances, priorities,
								maxMobsPerPlayer, mobLevel, board, objective);

					}
				}
			}, 0L, 20L / conversionSpawnRateMultiplier);
		}
		
		// checkPlayersInRegion Method
		public static void checkPlayersInRegion(Scoreboard board, Objective objective, List<String> mMMobNames,
				List<Double> chances, List<Integer> priorities, int maxMobsPerPlayer, long conversionSpawnRateMultiplier,
				int mobLevel) {
			int[] id = { 0 };
			Random rand = new Random();
			id[0] = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), new Runnable() {
				public void run() {
					if (Raid.timeReached == true) {
						Bukkit.getServer().getScheduler().cancelTask(id[0]);
					} else {
						List<Player> playerList = new ArrayList<>(Bukkit.getOnlinePlayers());
						Raid.playersInRegion = new ArrayList<>();
						List<Location> onlinePlayerLocations = new ArrayList<>();
						List<Location> regionPlayerLocations = new ArrayList<>();
						int scoreCounter = 0;

						for (int i = 0; i < playerList.size(); i++) {
							onlinePlayerLocations.add(playerList.get(i).getLocation());
							if (Raid.region.contains(onlinePlayerLocations.get(i).getBlockX(),
									onlinePlayerLocations.get(i).getBlockY(), onlinePlayerLocations.get(i).getBlockZ())) {
								Raid.playersInRegion.add(playerList.get(i));
								regionPlayerLocations.add(playerList.get(i).getLocation());
							}
						}

						// Add up scores for all players in region
						for (int n = 0; n < Raid.playersInRegion.size(); n++) {

							// Make sure the player is mapped in raidKills or it will return a null error

							if (Raid.playersInRegion.get(n).getScoreboard() != board) {
								Raid.playersInRegion.get(n).setScoreboard(board);
							}

							if (Raid.raidKills.containsKey(Raid.playersInRegion.get(n).getName())) {
								if (Raid.scoreboardPlayerData.containsKey(Raid.playersInRegion.get(n).getName())) {
									board.resetScores(Raid.scoreboardPlayerData.get(Raid.playersInRegion.get(n).getName()));
								}
								Score score = objective.getScore(ChatColor.YELLOW + Raid.playersInRegion.get(n).getName()
										+ ":    " + Raid.raidKills.get(Raid.playersInRegion.get(n).getName()));
								Raid.scoreboardPlayerData.put(Raid.playersInRegion.get(n).getName(),
										ChatColor.YELLOW + Raid.playersInRegion.get(n).getName() + ":    "
												+ Raid.raidKills.get(Raid.playersInRegion.get(n).getName()));
								score.setScore(0);
								scoreCounter += Raid.raidKills.get(Raid.playersInRegion.get(n).getName());

							}

						}

						// Spawn mobs for all players in region
						Raid.spawnMobs(rand, regionPlayerLocations, scoreCounter, mMMobNames, chances, priorities,
								maxMobsPerPlayer, mobLevel, board, objective);

					}
				}
			}, 0L, 20L / conversionSpawnRateMultiplier);
		}
		
		// resetVariables Method
		public static void resetVariables() {
			Raid.timeReached = false;
			Raid.totalKills = 0;
			Raid.mobsSpawned = 0;
			Raid.maxMobsReached = false;
			Raid.playersInRegion = new ArrayList<>();
			Raid.MmEntityList = new ArrayList<>();
			Raid.mMMobNames = new ArrayList<>();
			Raid.raidKills = new HashMap<String, Integer>();
			Main.cancelledRaid = false;
			Raid.runOnce = false;
			Raid.priorities = new ArrayList<>();
			Raid.chances = new ArrayList<>();
			Raid.mMMobNames = new ArrayList<>();
			Raid.otherDeaths = 0;
			Raid.scoreboardPlayerData = new HashMap<String, String>();
			Raid.bossSpawned = false;
			Raid.isScheduled = false;
		}
		
		// getMobsFromConfig Method
		public static void getMobsFromConfig() {
			Set<String> mmMobs = Main.getInstance().getConfig().getConfigurationSection("RaidMobs").getKeys(false); // only
																													// gets
			// top keys
			Iterator<String> it = mmMobs.iterator();
			// converts set to arraylist
			while (it.hasNext()) {
				Raid.mMMobNames.add(it.next());
			}
			// gets chance and priority data for each mob name
			for (int k = 0; k < Raid.mMMobNames.size(); k++) {
				double chance = Main.getInstance().getConfig().getConfigurationSection("RaidMobs")
						.getDouble(Raid.mMMobNames.get(k) + ".Chance");
				int priority = Main.getInstance().getConfig().getConfigurationSection("RaidMobs")
						.getInt(Raid.mMMobNames.get(k) + ".Priority");
				Raid.chances.add(chance);
				Raid.priorities.add(priority);
			}
		}
		
		
		// isCancelledRaid Method
		public static boolean isCancelledRaid(String tier, CommandSender sender) {
			if (Main.cancelledRaid) {
				String raidCancelledTitle = Main.getInstance().getConfig().getString("RaidCancelledTitle");
				String raidCancelledSubtitle = Main.getInstance().getConfig().getString("RaidCancelledSubtitle");
				if (raidCancelledTitle.contains("@TIER")) {
					raidCancelledTitle = raidCancelledTitle.replaceAll("@TIER", tier);
				}
				if (raidCancelledSubtitle.contains("@TIER")) {
					raidCancelledSubtitle = raidCancelledSubtitle.replaceAll("@TIER", tier);
				}
				if (Raid.region != null) {
					if (raidCancelledTitle.contains("@REGION")) {
						raidCancelledTitle = raidCancelledTitle.replaceAll("@REGION", Raid.region.getId());
					}
					if (raidCancelledSubtitle.contains("@REGION")) {
						raidCancelledSubtitle = raidCancelledSubtitle.replaceAll("@REGION", Raid.region.getId());
					}
				}
				if (Raid.town != null) {
					if (raidCancelledTitle.contains("@TOWN")) {
						raidCancelledTitle = raidCancelledTitle.replaceAll("@TOWN", Raid.town.getName());
					}
					if (raidCancelledSubtitle.contains("@TOWN")) {
						raidCancelledSubtitle = raidCancelledSubtitle.replaceAll("@TOWN", Raid.town.getName());
					}
				}
				if (raidCancelledTitle.contains("@SENDER")) {
					raidCancelledTitle = raidCancelledTitle.replaceAll("@SENDER", sender.getName());
				}
				if (raidCancelledSubtitle.contains("@SENDER")) {
					raidCancelledSubtitle = raidCancelledSubtitle.replaceAll("@SENDER", sender.getName());
				}
				for (int n = 0; n < Raid.playersInRegion.size(); n++) { // Broadcasts title to every player in the raiding region
					Raid.playersInRegion.get(n).sendTitle(ChatColor.translateAlternateColorCodes('&', raidCancelledTitle),
							ChatColor.translateAlternateColorCodes('&', raidCancelledSubtitle), 10, 60, 10);

				}
				for (int i = 0; i < Raid.MmEntityList.size(); i++) {
					if (Raid.MmEntityList.get(i).isLiving()) {
						Raid.MmEntityList.get(i).remove();
					}
				}
				return true;
			} else {
				return false;
			}
		}
		
		
		// isWonRaid Method
		public static boolean isWonRaid(String tier, int goal, String boss, int mobLevel, CommandSender sender) {
			if (Raid.totalKills >= goal) {
				if (boss.equalsIgnoreCase("NONE")) {
					String raidWinTitle = Main.getInstance().getConfig().getString("RaidWinTitle");
					String raidWinSubtitle = Main.getInstance().getConfig().getString("RaidWinSubtitle");
					if (raidWinTitle.contains("@TIER")) {
						raidWinTitle = raidWinTitle.replaceAll("@TIER", tier);
					}
					if (raidWinSubtitle.contains("@TIER")) {
						raidWinSubtitle = raidWinSubtitle.replaceAll("@TIER", tier);
					}
					if (Raid.region != null) {
						if (raidWinTitle.contains("@REGION")) {
							raidWinTitle = raidWinTitle.replaceAll("@REGION", Raid.region.getId());
						}
						if (raidWinSubtitle.contains("@REGION")) {
							raidWinSubtitle = raidWinSubtitle.replaceAll("@REGION", Raid.region.getId());
						}
					}
					if (Raid.town != null) {
						if (raidWinTitle.contains("@TOWN")) {
							raidWinTitle = raidWinTitle.replaceAll("@TOWN", Raid.town.getName());
						}
						if (raidWinSubtitle.contains("@TOWN")) {
							raidWinSubtitle = raidWinSubtitle.replaceAll("@TOWN", Raid.town.getName());
						}
					}
					if (raidWinTitle.contains("@SENDER")) {
						raidWinTitle = raidWinTitle.replaceAll("@SENDER", sender.getName());
					}
					if (raidWinSubtitle.contains("@SENDER")) {
						raidWinSubtitle = raidWinSubtitle.replaceAll("@SENDER", sender.getName());
					}
					for (int n = 0; n < Raid.playersInRegion.size(); n++) { // Broadcasts title to every player in the raiding
																		// region
						Raid.playersInRegion.get(n).sendTitle(ChatColor.translateAlternateColorCodes('&', raidWinTitle), ChatColor.translateAlternateColorCodes('&', raidWinSubtitle), 10, 60, 10);
					}
					for (int i = 0; i < Raid.MmEntityList.size(); i++) {
						if (Raid.MmEntityList.get(i).isLiving()) {
							Raid.MmEntityList.get(i).damage(1000);
						}
					}

					if (Main.getInstance().getConfig().getBoolean("UseWinLossCommands")) {
						try {
							List<String> globalCommands = Main.getInstance().getConfig()
									.getStringList("RaidWinCommands.Global");
							for (int i = 0; i < globalCommands.size(); i++) {
								String command = globalCommands.get(i);
								if (Raid.region != null) {
									if (command.contains("@REGION")) {
										command = command.replaceAll("@REGION", Raid.region.getId());
									}
								}
								if (Raid.town != null) {
									if (command.contains("@TOWN")) {
										command = command.replaceAll("@TOWN", Raid.town.getName());
									}
								}
								if (command.contains("@TIER")) {
									command = command.replaceAll("@TIER", tier);
								}
								Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
							}
						} catch (NullPointerException e) {
							e.printStackTrace();
						}

						try {
							List<String> perPlayerCommands = Main.getInstance().getConfig()
									.getStringList("RaidWinCommands.PerPlayer");
							for (int i = 0; i < perPlayerCommands.size(); i++) {
								String command = perPlayerCommands.get(i);
								if (Raid.region != null) {
									if (command.contains("@REGION")) {
										command = command.replaceAll("@REGION", Raid.region.getId());
									}
								}
								if (Raid.town != null) {
									if (command.contains("@TOWN")) {
										command = command.replaceAll("@TOWN", Raid.town.getName());
									}
								}
								if (command.contains("@TIER")) {
									command = command.replaceAll("@TIER", tier);
								}
								for (String key : Raid.raidKills.keySet()) {
									String playerCommand = command;
									if (command.contains("@PLAYER")) {
										playerCommand = playerCommand.replaceAll("@PLAYER", key);
										Bukkit.dispatchCommand(Bukkit.getConsoleSender(), playerCommand);
									}
								}
							}
						} catch (NullPointerException e) {
							e.printStackTrace();
						}
					}
					return true;
				} else if (!Raid.bossSpawned) {
					for (int n = 0; n < Raid.playersInRegion.size(); n++) { // Broadcasts title to every player in the raiding
																		// region
						Raid.playersInRegion.get(n).sendTitle(ChatColor.translateAlternateColorCodes('&', "&4&lBoss Spawned!"), ChatColor.translateAlternateColorCodes('&', "&6Kill the boss to win the raid"), 10, 60, 10);
					}
					Raid.bossSpawned = true;
					Random rand = new Random();
					int randIdx = rand.nextInt(Raid.playersInRegion.size()); // pick a random idx from playerlist
					int x = Raid.playersInRegion.get(randIdx).getLocation().getBlockX();
					int y = Raid.playersInRegion.get(randIdx).getLocation().getBlockY();
					int z = Raid.playersInRegion.get(randIdx).getLocation().getBlockZ();
					World w = Raid.playersInRegion.get(randIdx).getWorld();
					Location spawnLocation = new Location(w, x, y, z);

					ActiveMob mob = MythicBukkit.inst().getMobManager().spawnMob(boss, spawnLocation, mobLevel);
					try {
						AbstractEntity entityOfMob = mob.getEntity();
						Raid.bossEntity = entityOfMob;
						Raid.MmEntityList.add(entityOfMob);
						Raid.mobsSpawned++;
					} catch (NullPointerException e) {	
					}
					return false;
				} else {
					return false;
				}

			}
			return false;
		}
		
		// isLostRaid Method
		public static boolean isLostRaid(String tier, int goal, int minutes, CommandSender sender) {
			if (Raid.countdown == 0 && minutes == 0) {
				String raidLoseTitle = Main.getInstance().getConfig().getString("RaidLoseTitle");
				String raidLoseSubtitle = Main.getInstance().getConfig().getString("RaidLoseSubtitle");
				if (raidLoseTitle.contains("@TIER")) {
					raidLoseTitle = raidLoseTitle.replaceAll("@TIER", tier);
				}
				if (raidLoseSubtitle.contains("@TIER")) {
					raidLoseSubtitle = raidLoseSubtitle.replaceAll("@TIER", tier);
				}
				if (Raid.region != null) {
					if (raidLoseTitle.contains("@REGION")) {
						raidLoseTitle = raidLoseTitle.replaceAll("@REGION", Raid.region.getId());
					}
					if (raidLoseSubtitle.contains("@REGION")) {
						raidLoseSubtitle = raidLoseSubtitle.replaceAll("@REGION", Raid.region.getId());
					}
				}
				if (Raid.town != null) {
					if (raidLoseTitle.contains("@TOWN")) {
						raidLoseTitle = raidLoseTitle.replaceAll("@TOWN", Raid.town.getName());
					}
					if (raidLoseSubtitle.contains("@TOWN")) {
						raidLoseSubtitle = raidLoseSubtitle.replaceAll("@TOWN", Raid.town.getName());
					}
				}
				if (raidLoseTitle.contains("@SENDER")) {
					raidLoseTitle = raidLoseTitle.replaceAll("@SENDER", sender.getName());
				}
				if (raidLoseSubtitle.contains("@SENDER")) {
					raidLoseSubtitle = raidLoseSubtitle.replaceAll("@SENDER", sender.getName());
				}
				if (Raid.totalKills < goal) {
					// raid lost

					for (int n = 0; n < Raid.playersInRegion.size(); n++) { // Broadcasts title to every player in the raiding
																		// region
						Raid.playersInRegion.get(n).sendTitle(ChatColor.translateAlternateColorCodes('&', raidLoseTitle), ChatColor.translateAlternateColorCodes('&', raidLoseSubtitle), 10, 60, 10);
					}

					if (!Main.getInstance().getConfig().getBoolean("MobsStayOnRaidLoss")) {
						for (int i = 0; i < Raid.MmEntityList.size(); i++) {
							if (Raid.MmEntityList.get(i).isLiving()) {
								Raid.MmEntityList.get(i).remove();
							}
						}
					}

					if (Main.getInstance().getConfig().getBoolean("UseWinLossCommands")) {
						try {
							List<String> globalCommands = Main.getInstance().getConfig()
									.getStringList("RaidLoseCommands.Global");
							for (int i = 0; i < globalCommands.size(); i++) {
								String command = globalCommands.get(i);
								if (Raid.region != null) {
									if (command.contains("@REGION")) {
										command = command.replaceAll("@REGION", Raid.region.getId());
									}
								}
								if (Raid.town != null) {
									if (command.contains("@TOWN")) {
										command = command.replaceAll("@TOWN", Raid.town.getName());
									}
								}
								if (command.contains("@TIER")) {
									command = command.replaceAll("@TIER", tier);
								}
								Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
							}
						} catch (NullPointerException e) {
							e.printStackTrace();
						}

						try {
							List<String> perPlayerCommands = Main.getInstance().getConfig()
									.getStringList("RaidLoseCommands.PerPlayer");
							for (int i = 0; i < perPlayerCommands.size(); i++) {
								String command = perPlayerCommands.get(i);
								if (Raid.region != null) {
									if (command.contains("@REGION")) {
										command = command.replaceAll("@REGION", Raid.region.getId());
									}
								}
								if (Raid.town != null) {
									if (command.contains("@TOWN")) {
										command = command.replaceAll("@TOWN", Raid.town.getName());
									}
								}
								if (command.contains("@TIER")) {
									command = command.replaceAll("@TIER", tier);
								}
								for (String key : Raid.raidKills.keySet()) {
									String playerCommand = command;
									if (command.contains("@PLAYER")) {
										playerCommand = playerCommand.replaceAll("@PLAYER", key);
										Bukkit.dispatchCommand(Bukkit.getConsoleSender(), playerCommand);
									}
								}
							}
						} catch (NullPointerException e) {
							e.printStackTrace();
						}
					}

				}

				return true;
			} else {
				return false;
			}
		}


}
