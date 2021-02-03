package me.ShermansWorld.RaidsPerRegion.commands;

import me.ShermansWorld.RaidsPerRegion.Main;
import me.ShermansWorld.RaidsPerRegion.extras.Title;

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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MobManager;

public class RaidCommands implements CommandExecutor {

	public static Main plugin;

	// private class variables
	private static boolean timeReached = false;
	private static int totalKills;
	private static boolean maxMobsReached = false;
	private static List<String> mMMobNames = new ArrayList<>();
	private static List<Double> chances = new ArrayList<>();
	private static List<Integer> priorities = new ArrayList<>();
	private boolean runOnce = false;
	public int countdown;
	private String tempStr = "";
	private static String tempStr2 = "";
	private static Map<String, String> scoreboardPlayerData = new HashMap<String, String>();
	private int minutes;
	private boolean hasMobsOn;

	// public variables used in Listener Class
	public static List<Player> playersInRegion = new ArrayList<>();
	public static Map<String, Integer> raidKills = new HashMap<String, Integer>();
	public static int otherDeaths = 0;
	public static List<AbstractEntity> MmEntityList = new ArrayList<>();
	public static int mobsSpawned = 0;
	public static boolean bossSpawned = false;
	public static String boss = "NONE";
	public static double mobLevel = 1.0;
	public static AbstractEntity bossEntity;
	public static int tier = 1;

	// regions and towns
	public static ProtectedRegion region;
	public static Town town;

	public RaidCommands(Main plugin) {
		this.plugin = plugin;
		plugin.getCommand("raid").setExecutor((CommandExecutor) this); // command to run in chat
	}

///////////////////////////////////////////////////////CUSTOM METHODS////////////////////////////////////////////////////////////////////////////////

	// spawnMobs Method
	private static void spawnMobs(Random rand, List<Location> regionPlayerLocations, int scoreCounter, MobManager mm,
			List<String> mMMobNames, List<Double> chances, List<Integer> priorities, int maxMobsPerPlayer,
			double mobLevel, Scoreboard board, Objective objective) {

		for (int j = 0; j < playersInRegion.size(); j++) {
			int randomPlayerIdx = rand.nextInt(playersInRegion.size());
			World w = playersInRegion.get(j).getWorld();
			int x = regionPlayerLocations.get(randomPlayerIdx).getBlockX() + rand.nextInt(50) - 25;
			int y = regionPlayerLocations.get(randomPlayerIdx).getBlockY();
			int z = regionPlayerLocations.get(randomPlayerIdx).getBlockZ() + rand.nextInt(50) - 25;
			String mythicMobName;
			int spawnRate = rand.nextInt(3); // 1/3 chance of spawning zombie at this player per cycle. possibilities:
												// 0, 1 or 2
			int numPlayersInRegion = playersInRegion.size();
			int mobsAlive = mobsSpawned - scoreCounter - otherDeaths;

			if (mobsAlive >= numPlayersInRegion * maxMobsPerPlayer) {
				maxMobsReached = true;
			} else {
				maxMobsReached = false;
			}
			if (spawnRate == 2 && !maxMobsReached) {
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

				ActiveMob mob = mm.spawnMob(mythicMobName, mobSpawnLocation, mobLevel); // type, location, level
				if (mob != null) {
					AbstractEntity entityOfMob = mob.getEntity();
					MmEntityList.add(entityOfMob);
					mobsSpawned++;
				}
			}
			board.resetScores(tempStr2);
			totalKills = scoreCounter;
			Score tempTotalScore = objective
					.getScore(ChatColor.AQUA + "Total Kills:      " + String.valueOf(totalKills));
			tempTotalScore.setScore(3);
			tempStr2 = ChatColor.AQUA + "Total Kills:      " + String.valueOf(totalKills);
		}
	}

	// checkPlayersInTown Method
	public static void checkPlayersInTown(Scoreboard board, Objective objective, MobManager mm, List<String> mMMobNames,
			List<Double> chances, List<Integer> priorities, int maxMobsPerPlayer, long conversionSpawnRateMultiplier,
			double mobLevel) {
		int[] id = { 0 };
		Random rand = new Random();
		id[0] = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				if (timeReached == true) {
					Bukkit.getServer().getScheduler().cancelTask(id[0]);
				} else {
					List<Player> playerList = new ArrayList<>(Bukkit.getOnlinePlayers());
					playersInRegion = new ArrayList<>();
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
						if (currentTown == town) {
							playersInRegion.add(playerList.get(i));
							regionPlayerLocations.add(playerList.get(i).getLocation());
						}
					}

					// Add up scores for all players in region
					for (int n = 0; n < playersInRegion.size(); n++) {

						// Make sure the player is mapped in raidKills or it will return a null error

						if (playersInRegion.get(n).getScoreboard() != board) {
							playersInRegion.get(n).setScoreboard(board);
						}

						if (raidKills.containsKey(playersInRegion.get(n).getName())) {
							if (scoreboardPlayerData.containsKey(playersInRegion.get(n).getName())) {
								board.resetScores(scoreboardPlayerData.get(playersInRegion.get(n).getName()));
							}
							Score score = objective.getScore(ChatColor.YELLOW + playersInRegion.get(n).getName()
									+ ":    " + raidKills.get(playersInRegion.get(n).getName()));
							scoreboardPlayerData.put(playersInRegion.get(n).getName(),
									ChatColor.YELLOW + playersInRegion.get(n).getName() + ":    "
											+ raidKills.get(playersInRegion.get(n).getName()));
							score.setScore(0);
							scoreCounter += raidKills.get(playersInRegion.get(n).getName());
						}

					}
					// Spawn mobs for all players in region
					spawnMobs(rand, regionPlayerLocations, scoreCounter, mm, mMMobNames, chances, priorities,
							maxMobsPerPlayer, mobLevel, board, objective);

				}
			}
		}, 0L, 20L / conversionSpawnRateMultiplier);
	}

	// checkPlayersInRegion Method
	public static void checkPlayersInRegion(Scoreboard board, Objective objective, MobManager mm,
			List<String> mMMobNames, List<Double> chances, List<Integer> priorities, int maxMobsPerPlayer,
			long conversionSpawnRateMultiplier, double mobLevel) {
		int[] id = { 0 };
		Random rand = new Random();
		id[0] = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				if (timeReached == true) {
					Bukkit.getServer().getScheduler().cancelTask(id[0]);
				} else {
					List<Player> playerList = new ArrayList<>(Bukkit.getOnlinePlayers());
					playersInRegion = new ArrayList<>();
					List<Location> onlinePlayerLocations = new ArrayList<>();
					List<Location> regionPlayerLocations = new ArrayList<>();
					int scoreCounter = 0;

					for (int i = 0; i < playerList.size(); i++) {
						onlinePlayerLocations.add(playerList.get(i).getLocation());
						if (region.contains(onlinePlayerLocations.get(i).getBlockX(),
								onlinePlayerLocations.get(i).getBlockY(), onlinePlayerLocations.get(i).getBlockZ())) {
							playersInRegion.add(playerList.get(i));
							regionPlayerLocations.add(playerList.get(i).getLocation());
						}
					}

					// Add up scores for all players in region
					for (int n = 0; n < playersInRegion.size(); n++) {

						// Make sure the player is mapped in raidKills or it will return a null error

						if (playersInRegion.get(n).getScoreboard() != board) {
							playersInRegion.get(n).setScoreboard(board);
						}

						if (raidKills.containsKey(playersInRegion.get(n).getName())) {
							if (scoreboardPlayerData.containsKey(playersInRegion.get(n).getName())) {
								board.resetScores(scoreboardPlayerData.get(playersInRegion.get(n).getName()));
							}
							Score score = objective.getScore(ChatColor.YELLOW + playersInRegion.get(n).getName()
									+ ":    " + raidKills.get(playersInRegion.get(n).getName()));
							scoreboardPlayerData.put(playersInRegion.get(n).getName(),
									ChatColor.YELLOW + playersInRegion.get(n).getName() + ":    "
											+ raidKills.get(playersInRegion.get(n).getName()));
							score.setScore(0);
							scoreCounter += raidKills.get(playersInRegion.get(n).getName());

						}

					}

					// Spawn mobs for all players in region
					spawnMobs(rand, regionPlayerLocations, scoreCounter, mm, mMMobNames, chances, priorities,
							maxMobsPerPlayer, mobLevel, board, objective);

				}
			}
		}, 0L, 20L / conversionSpawnRateMultiplier);
	}

	// getMobsFromConfig Method
	private void getMobsFromConfig() {
		Set<String> mmMobs = this.plugin.getConfig().getConfigurationSection("RaidMobs").getKeys(false); // only gets
																											// top keys
		Iterator<String> it = mmMobs.iterator();
		// converts set to arraylist
		while (it.hasNext()) {
			mMMobNames.add(it.next());
		}
		// gets chance and priority data for each mob name
		for (int k = 0; k < mMMobNames.size(); k++) {
			double chance = this.plugin.getConfig().getConfigurationSection("RaidMobs")
					.getDouble(mMMobNames.get(k) + ".Chance");
			int priority = this.plugin.getConfig().getConfigurationSection("RaidMobs")
					.getInt(mMMobNames.get(k) + ".Priority");
			chances.add(chance);
			priorities.add(priority);
		}
	}

	// isCancelledRaid Method
	private boolean isCancelledRaid(String tier, CommandSender sender) {
		if (Main.cancelledRaid) {
			Title title = new Title();
			String raidCancelledTitle = plugin.getConfig().getString("RaidCancelledTitle");
			String raidCancelledSubtitle = plugin.getConfig().getString("RaidCancelledSubtitle");
			if (raidCancelledTitle.contains("@TIER")) {
				raidCancelledTitle = raidCancelledTitle.replaceAll("@TIER", tier);
			}
			if (raidCancelledSubtitle.contains("@TIER")) {
				raidCancelledSubtitle = raidCancelledSubtitle.replaceAll("@TIER", tier);
			}
			if (region != null) {
				if (raidCancelledTitle.contains("@REGION")) {
					raidCancelledTitle = raidCancelledTitle.replaceAll("@REGION", region.getId());
				}
				if (raidCancelledSubtitle.contains("@REGION")) {
					raidCancelledSubtitle = raidCancelledSubtitle.replaceAll("@REGION", region.getId());
				}
			}
			if (town != null) {
				if (raidCancelledTitle.contains("@TOWN")) {
					raidCancelledTitle = raidCancelledTitle.replaceAll("@TOWN", town.getName());
				}
				if (raidCancelledSubtitle.contains("@TOWN")) {
					raidCancelledSubtitle = raidCancelledSubtitle.replaceAll("@TOWN", town.getName());
				}
			}
			if (raidCancelledTitle.contains("@SENDER")) {
				raidCancelledTitle = raidCancelledTitle.replaceAll("@SENDER", sender.getName());
			}
			if (raidCancelledSubtitle.contains("@SENDER")) {
				raidCancelledSubtitle = raidCancelledSubtitle.replaceAll("@SENDER", sender.getName());
			}
			for (int n = 0; n < playersInRegion.size(); n++) { // Broadcasts title to every player in the raiding region
				title.send(playersInRegion.get(n), ((ChatColor.translateAlternateColorCodes('&', raidCancelledTitle))),
						((ChatColor.translateAlternateColorCodes('&', raidCancelledSubtitle))), 10, 60, 10);
			}
			for (int i = 0; i < MmEntityList.size(); i++) {
				if (MmEntityList.get(i).isLiving()) {
					MmEntityList.get(i).remove();
				}
			}
			return true;
		} else {
			return false;
		}
	}

	// isWonRaid Method
	private boolean isWonRaid(String tier, int goal, String boss, MobManager mm, double mobLevel,
			CommandSender sender) {
		if (totalKills >= goal) {
			if (boss.equalsIgnoreCase("NONE")) {
				Title title = new Title();
				String raidWinTitle = plugin.getConfig().getString("RaidWinTitle");
				String raidWinSubtitle = plugin.getConfig().getString("RaidWinSubtitle");
				if (raidWinTitle.contains("@TIER")) {
					raidWinTitle = raidWinTitle.replaceAll("@TIER", tier);
				}
				if (raidWinSubtitle.contains("@TIER")) {
					raidWinSubtitle = raidWinSubtitle.replaceAll("@TIER", tier);
				}
				if (region != null) {
					if (raidWinTitle.contains("@REGION")) {
						raidWinTitle = raidWinTitle.replaceAll("@REGION", region.getId());
					}
					if (raidWinSubtitle.contains("@REGION")) {
						raidWinSubtitle = raidWinSubtitle.replaceAll("@REGION", region.getId());
					}
				}
				if (town != null) {
					if (raidWinTitle.contains("@TOWN")) {
						raidWinTitle = raidWinTitle.replaceAll("@TOWN", town.getName());
					}
					if (raidWinSubtitle.contains("@TOWN")) {
						raidWinSubtitle = raidWinSubtitle.replaceAll("@TOWN", town.getName());
					}
				}
				if (raidWinTitle.contains("@SENDER")) {
					raidWinTitle = raidWinTitle.replaceAll("@SENDER", sender.getName());
				}
				if (raidWinSubtitle.contains("@SENDER")) {
					raidWinSubtitle = raidWinSubtitle.replaceAll("@SENDER", sender.getName());
				}
				for (int n = 0; n < playersInRegion.size(); n++) { // Broadcasts title to every player in the raiding
																	// region
					title.send(playersInRegion.get(n), (ChatColor.translateAlternateColorCodes('&', raidWinTitle)),
							(ChatColor.translateAlternateColorCodes('&', raidWinSubtitle)), 10, 60, 10);
				}
				for (int i = 0; i < MmEntityList.size(); i++) {
					if (MmEntityList.get(i).isLiving()) {
						MmEntityList.get(i).damage(1000);
					}
				}

				if (plugin.getConfig().getBoolean("UseWinLossCommands")) {
					try {
						List<String> globalCommands = plugin.getConfig().getStringList("RaidWinCommands.Global");
						for (int i = 0; i < globalCommands.size(); i++) {
							String command = globalCommands.get(i);
							if (region != null) {
								if (command.contains("@REGION")) {
									command = command.replaceAll("@REGION", region.getId());
								}
							}
							if (town != null) {
								if (command.contains("@TOWN")) {
									command = command.replaceAll("@TOWN", town.getName());
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
						List<String> perPlayerCommands = plugin.getConfig().getStringList("RaidWinCommands.PerPlayer");
						for (int i = 0; i < perPlayerCommands.size(); i++) {
							String command = perPlayerCommands.get(i);
							if (region != null) {
								if (command.contains("@REGION")) {
									command = command.replaceAll("@REGION", region.getId());
								}
							}
							if (town != null) {
								if (command.contains("@TOWN")) {
									command = command.replaceAll("@TOWN", town.getName());
								}
							}
							if (command.contains("@TIER")) {
								command = command.replaceAll("@TIER", tier);
							}
							for (String key : raidKills.keySet()) {
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
			} else if (!bossSpawned) {
				Title title = new Title();
				for (int n = 0; n < playersInRegion.size(); n++) { // Broadcasts title to every player in the raiding
																	// region
					title.send(playersInRegion.get(n),
							(ChatColor.translateAlternateColorCodes('&', "&4&lBoss Spawned!")),
							(ChatColor.translateAlternateColorCodes('&', "&6Kill the boss to win the raid")), 10, 60,
							10);
				}
				bossSpawned = true;
				Random rand = new Random();
				int randIdx = rand.nextInt(playersInRegion.size()); // pick a random idx from playerlist
				int x = playersInRegion.get(randIdx).getLocation().getBlockX();
				int y = playersInRegion.get(randIdx).getLocation().getBlockY();
				int z = playersInRegion.get(randIdx).getLocation().getBlockZ();
				World w = playersInRegion.get(randIdx).getWorld();
				Location spawnLocation = new Location(w, x, y, z);
				ActiveMob mob = mm.spawnMob(boss, spawnLocation, mobLevel);
				if (mob != null) {
					AbstractEntity entityOfMob = mob.getEntity();
					bossEntity = entityOfMob;
					MmEntityList.add(entityOfMob);
					mobsSpawned++;
				} else {
					Bukkit.broadcastMessage(ChatColor.DARK_RED + "[RaidsPerRegion] ERROR WITH BOSS SPAWNED");
				}
				return false;
			} else {
				return false;
			}

		}
		return false;
	}

	// isLostRaid Method
	private boolean isLostRaid(String tier, int goal, int minutes, CommandSender sender) {
		if (countdown == 0 && minutes == 0) {
			Title title = new Title();
			String raidLoseTitle = plugin.getConfig().getString("RaidLoseTitle");
			String raidLoseSubtitle = plugin.getConfig().getString("RaidLoseSubtitle");
			if (raidLoseTitle.contains("@TIER")) {
				raidLoseTitle = raidLoseTitle.replaceAll("@TIER", tier);
			}
			if (raidLoseSubtitle.contains("@TIER")) {
				raidLoseSubtitle = raidLoseSubtitle.replaceAll("@TIER", tier);
			}
			if (region != null) {
				if (raidLoseTitle.contains("@REGION")) {
					raidLoseTitle = raidLoseTitle.replaceAll("@REGION", region.getId());
				}
				if (raidLoseSubtitle.contains("@REGION")) {
					raidLoseSubtitle = raidLoseSubtitle.replaceAll("@REGION", region.getId());
				}
			}
			if (town != null) {
				if (raidLoseTitle.contains("@TOWN")) {
					raidLoseTitle = raidLoseTitle.replaceAll("@TOWN", town.getName());
				}
				if (raidLoseSubtitle.contains("@TOWN")) {
					raidLoseSubtitle = raidLoseSubtitle.replaceAll("@TOWN", town.getName());
				}
			}
			if (raidLoseTitle.contains("@SENDER")) {
				raidLoseTitle = raidLoseTitle.replaceAll("@SENDER", sender.getName());
			}
			if (raidLoseSubtitle.contains("@SENDER")) {
				raidLoseSubtitle = raidLoseSubtitle.replaceAll("@SENDER", sender.getName());
			}
			if (totalKills < goal) {
				// raid lost

				for (int n = 0; n < playersInRegion.size(); n++) { // Broadcasts title to every player in the raiding
																	// region
					title.send(playersInRegion.get(n), (ChatColor.translateAlternateColorCodes('&', raidLoseTitle)),
							(ChatColor.translateAlternateColorCodes('&', raidLoseSubtitle)), 10, 60, 10);
				}
				
				if (!plugin.getConfig().getBoolean("MobsStayOnRaidLoss")) {
					for (int i = 0; i < MmEntityList.size(); i++) {
						if (MmEntityList.get(i).isLiving()) {
							MmEntityList.get(i).remove();
						}
					}
				}

				if (plugin.getConfig().getBoolean("UseWinLossCommands")) {
					try {
						List<String> globalCommands = plugin.getConfig().getStringList("RaidLoseCommands.Global");
						for (int i = 0; i < globalCommands.size(); i++) {
							String command = globalCommands.get(i);
							if (region != null) {
								if (command.contains("@REGION")) {
									command = command.replaceAll("@REGION", region.getId());
								}
							}
							if (town != null) {
								if (command.contains("@TOWN")) {
									command = command.replaceAll("@TOWN", town.getName());
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
						List<String> perPlayerCommands = plugin.getConfig().getStringList("RaidLoseCommands.PerPlayer");
						for (int i = 0; i < perPlayerCommands.size(); i++) {
							String command = perPlayerCommands.get(i);
							if (region != null) {
								if (command.contains("@REGION")) {
									command = command.replaceAll("@REGION", region.getId());
								}
							}
							if (town != null) {
								if (command.contains("@TOWN")) {
									command = command.replaceAll("@TOWN", town.getName());
								}
							}
							if (command.contains("@TIER")) {
								command = command.replaceAll("@TIER", tier);
							}
							for (String key : raidKills.keySet()) {
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

	// resetVariables Method
	private void resetVariables() {
		timeReached = false;
		totalKills = 0;
		mobsSpawned = 0;
		maxMobsReached = false;
		playersInRegion = new ArrayList<>();
		MmEntityList = new ArrayList<>();
		mMMobNames = new ArrayList<>();
		raidKills = new HashMap<String, Integer>();
		Main.cancelledRaid = false;
		runOnce = false;
		priorities = new ArrayList<>();
		chances = new ArrayList<>();
		mMMobNames = new ArrayList<>();
		otherDeaths = 0;
		scoreboardPlayerData = new HashMap<String, String>();
		bossSpawned = false;
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// Player that sends command
	// Command it sends
	// Alias of the command which was used
	// args for Other values within command
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player p;
		World w;

		boolean isConsole;
		if (sender instanceof Player) {
			p = (Player) sender; // Convert sender into player
			w = p.getWorld(); // Get world
			isConsole = false;
		} else {
			ArrayList<Player> online = new ArrayList<Player>(Bukkit.getOnlinePlayers());
			w = online.get(0).getWorld();
			isConsole = true;
			p = null;
		}

		// Initial values for tiers from config
		int goal;
		int maxMobsPerPlayer = 10;
		double spawnRateMultiplier = 1.0;
		long conversionSpawnRateMultiplier = 10;

		// Check arguments

		// --------------------------------------------------------------------------------------------------------------

		if (!isConsole) {
			if (!p.hasPermission("raidsperregion.raid")) {
				p.sendMessage(ChatColor.RED + "[RaidsPerRegion] You do not have permission to do this");
				return false;
			}
		}

		if (args.length == 1 && args[0].equalsIgnoreCase("cancel")) {
			if (region == null && town == null) { // if there is not a raid in progress
				if (isConsole) {
					plugin.getLogger().info("There is not a raid in progress right now");
				} else {
					p.sendMessage("[RaidsPerRegion] There is not a raid in progress right now");
				}
				return false;
			} else {
				if (region != null) {
					if (isConsole) {
						plugin.getLogger().info("Canceled raid on region" + region.getId());
					} else {
						p.sendMessage("[RaidsPerRegion] Canceled raid on region " + region.getId());
					}
				}
				if (town != null) {
					if (isConsole) {
						plugin.getLogger().info("Canceled raid on town " + town.getName());
					} else {
						p.sendMessage("[RaidsPerRegion] Canceled raid on town " + town.getName());
					}

				}
				Main.cancelledRaid = true;
				return false;
			}
		}

		if (args.length != 3) {
			if (isConsole) {
				plugin.getLogger().info("Invalid arguments");
				plugin.getLogger().info("Usage: /raid region [region] [tier] OR /raid town [town] [tier]");

			} else {
				p.sendMessage("[RaidsPerRegion] Invalid arguments");
				p.sendMessage("[RaidsPerRegion] Usage: /raid region [region] [tier] OR /raid town [town] [tier]");
			}
			return false;
		}

		if (region != null) {
			if (isConsole) {
				plugin.getLogger().info("There is already a raid in progress in region " + region.getId());
				plugin.getLogger().info("To cancel this raid type /raid cancel");

			} else {
				p.sendMessage("[RaidsPerRegion] There is already a raid in progress in region " + region.getId());
				p.sendMessage("[RaidsPerRegion] To cancel this raid type /raid cancel");
			}
			return false;
		}

		if (town != null) {
			if (isConsole) {
				plugin.getLogger().info("There is already a raid in progress in town " + town.getName());
				plugin.getLogger().info("To cancel this raid type /raid cancel");

			} else {
				p.sendMessage("[RaidsPerRegion] There is already a raid in progress in town " + town.getName());
				p.sendMessage("[RaidsPerRegion] To cancel this raid type /raid cancel");
			}
			return false;
		}

		if (args[0].equalsIgnoreCase("town")) {
			PluginManager pluginManager = plugin.getServer().getPluginManager();
			if (pluginManager.getPlugin("Towny") == null) {
				if (isConsole) {
					plugin.getLogger().info("You either do not have Towny installed or it is out of date");
				} else {
					p.sendMessage("[RaidsPerRegion] You either do not have Towny installed or it is out of date");
				}
				return false;
			}
			Towny towny = (Towny) (pluginManager.getPlugin("Towny"));
			TownyWorld townyWorld = new TownyWorld("le_monde");
			TownyUniverse uni = towny.getTownyUniverse();
			town = uni.getTown(args[1]);

			if (town == null) {
				if (isConsole) {
					plugin.getLogger().info("Invalid town. Useage: /raid town [town] [tier]");
				} else {
					p.sendMessage("[RaidsPerRegion] Invalid town. Useage: /raid town [town] [tier]");
				}
				return false;
			}
		}

		if (args[0].equalsIgnoreCase("region")) {
			com.sk89q.worldedit.world.World bukkitWorld = BukkitAdapter.adapt(w);
			RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer(); // Put regions on
			RegionManager regions = container.get(bukkitWorld);
			region = regions.getRegion(args[1]);

			Map<String, ProtectedRegion> regionMap = regions.getRegions();
			if (!regionMap.containsKey(args[1])) {
				if (isConsole) {
					plugin.getLogger().info("Invalid region. Useage: /raid region [region] [tier]");
				} else {
					p.sendMessage("[RaidsPerRegion] Invalid region. Useage: /raid region [region] [tier]");
				}
				return false;
			}
		}

		if (args[2].contentEquals("1") || args[2].contentEquals("2") || args[2].contentEquals("3")) {
			tier = Integer.parseInt(args[2]);
			goal = this.plugin.getConfig().getConfigurationSection("Tier" + String.valueOf(tier)).getInt("KillsGoal");
			countdown = this.plugin.getConfig().getConfigurationSection("Tier" + String.valueOf(tier)).getInt("Time");
			maxMobsPerPlayer = this.plugin.getConfig().getConfigurationSection("Tier" + String.valueOf(tier))
					.getInt("MaxMobsPerPlayer");
			spawnRateMultiplier = this.plugin.getConfig().getConfigurationSection("Tier" + String.valueOf(tier))
					.getDouble("SpawnRateMultiplier");
			conversionSpawnRateMultiplier = (long) (spawnRateMultiplier);
			mobLevel = this.plugin.getConfig().getConfigurationSection("Tier" + String.valueOf(tier))
					.getDouble("MobLevel");
			if (this.plugin.getConfig().getString("SpawnBossOnKillGoalReached").equalsIgnoreCase("true")) {
				boss = this.plugin.getConfig().getConfigurationSection("Tier" + String.valueOf(tier)).getString("Boss");
			} else {
				boss = "NONE";
			}
			if (conversionSpawnRateMultiplier == 0) {
				conversionSpawnRateMultiplier = 1;
				if (isConsole) {
					plugin.getLogger().info("SpawnRateMultipiler too low! defaulting to 1.0");
				} else {
					p.sendMessage("[RaidsPerRegion] SpawnRateMultipiler too low! defaulting to 1.0");
				}

			}

		} else {
			if (isConsole) {
				plugin.getLogger().info("Invalid tier. Useage: /raid [region] [tier]");
			} else {
				p.sendMessage("[RaidsPerRegion] Invalid tier. Useage: /raid [region] [tier]");
			}
			return false;
		}

		// -----------------------------------------------------------------------------------------------------------------------

		// variables to be reset for each raid
		resetVariables();

		MobManager mm = MythicMobs.inst().getMobManager();

		if (region != null) {
			if (region.getFlag(Flags.MOB_SPAWNING) == StateFlag.State.ALLOW) {
				hasMobsOn = true;
			} else {
				hasMobsOn = false;
				region.setFlag(Flags.MOB_SPAWNING, StateFlag.State.ALLOW);
			}
		}

		if (town != null) {
			if (town.hasMobs()) {
				hasMobsOn = true;
			} else {
				hasMobsOn = false;
				town.setHasMobs(true);
			}
		}

		ArrayList<Player> online = new ArrayList<Player>(Bukkit.getOnlinePlayers());
		Scoreboard board = online.get(0).getScoreboard();

		Objective objective = board.registerNewObjective("raidKills", "dummy",
				ChatColor.BOLD + "" + ChatColor.DARK_RED + "Raid: " + "Tier " + args[2]);
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		Score goalKills = objective.getScore((ChatColor.GOLD + "Goal:             " + String.valueOf(goal))); // Get a
																												// fake
																												// offline
																												// player
		Score totalScore = objective.getScore(ChatColor.AQUA + "Total Kills:      0"); // Get a fake offline player
		tempStr2 = ChatColor.AQUA + "Total Kills:      0";
		totalScore.setScore(3);
		goalKills.setScore(2);
		Score separater = objective.getScore(ChatColor.DARK_RED + "----------------------");
		separater.setScore(1);

		minutes = countdown / 60;
		countdown = countdown % 60;

		getMobsFromConfig();

		if (region != null) {
			checkPlayersInRegion(board, objective, mm, mMMobNames, chances, priorities, maxMobsPerPlayer,
					conversionSpawnRateMultiplier, mobLevel);
		}
		if (town != null) {
			checkPlayersInTown(board, objective, mm, mMMobNames, chances, priorities, maxMobsPerPlayer,
					conversionSpawnRateMultiplier, mobLevel);
		}

		int[] id = { 0 };
		id[0] = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				if (!playersInRegion.isEmpty() && !runOnce) {
					runOnce = true;
					Title title = new Title();
					String raidAnnoucementTitle = plugin.getConfig().getString("RaidAnnoucementTitle");
					String raidAnnoucementSubtitle = plugin.getConfig().getString("RaidAnnoucementSubtitle");
					if (raidAnnoucementTitle.contains("@TIER")) {
						raidAnnoucementTitle = raidAnnoucementTitle.replaceAll("@TIER", args[2]);
					}
					if (raidAnnoucementSubtitle.contains("@TIER")) {
						raidAnnoucementSubtitle = raidAnnoucementSubtitle.replaceAll("@TIER", args[2]);
					}
					if (region != null) {
						if (raidAnnoucementTitle.contains("@REGION")) {
							raidAnnoucementTitle = raidAnnoucementTitle.replaceAll("@REGION", region.getId());
						}
						if (raidAnnoucementSubtitle.contains("@REGION")) {
							raidAnnoucementSubtitle = raidAnnoucementSubtitle.replaceAll("@REGION", region.getId());
						}
					}
					if (town != null) {
						if (raidAnnoucementTitle.contains("@TOWN")) {
							raidAnnoucementTitle = raidAnnoucementTitle.replaceAll("@TOWN", town.getName());
						}
						if (raidAnnoucementSubtitle.contains("@TOWN")) {
							raidAnnoucementSubtitle = raidAnnoucementSubtitle.replaceAll("@TOWN", town.getName());
						}
					}
					if (raidAnnoucementTitle.contains("@SENDER")) {
						raidAnnoucementTitle = raidAnnoucementTitle.replaceAll("@SENDER", sender.getName());
					}
					if (raidAnnoucementSubtitle.contains("@SENDER")) {
						raidAnnoucementSubtitle = raidAnnoucementSubtitle.replaceAll("@SENDER", sender.getName());
					}
					for (int n = 0; n < playersInRegion.size(); n++) { // Broadcasts title to every player in the
																		// raiding region
						title.send(playersInRegion.get(n),
								(ChatColor.translateAlternateColorCodes('&', raidAnnoucementTitle)),
								(ChatColor.translateAlternateColorCodes('&', raidAnnoucementSubtitle)), 10, 60, 10);
					}
				}

				// check for canceled, won or lost raid and perform task
				if (isCancelledRaid(args[2], sender) || isWonRaid(args[2], goal, boss, mm, mobLevel, sender)
						|| isLostRaid(args[2], goal, minutes, sender)) {
					Bukkit.getServer().getScheduler().cancelTask(id[0]);
					objective.unregister();
					timeReached = true;
					boss = "NONE";
					mobLevel = 1.0;

					if (region != null) {
						if (!hasMobsOn) {
							region.setFlag(Flags.MOB_SPAWNING, StateFlag.State.DENY);
						}
						region = null;
					}
					if (town != null) {
						if (!hasMobsOn) {
							town.setHasMobs(false);
						}
						town = null;
					}

					return;
				}

				// Making a offline player called "Time:"
				// with a green name and adding it to
				// the scoreboard

				if (countdown == 0 && minutes >= 1) {
					minutes--;
					countdown += 60;
				}

				countdown--;

				try {
					board.resetScores(tempStr);
				} catch (NullPointerException e) {
					// first timer
				}
				if (countdown <= 9) {
					Score timer = objective.getScore(ChatColor.GREEN + "Time:             " + String.valueOf(minutes)
							+ ":0" + String.valueOf(countdown));
					tempStr = ChatColor.GREEN + "Time:             " + String.valueOf(minutes) + ":0"
							+ String.valueOf(countdown);
					timer.setScore(4);
				} else {
					Score timer = objective.getScore(ChatColor.GREEN + "Time:             " + String.valueOf(minutes)
							+ ":" + String.valueOf(countdown));
					tempStr = ChatColor.GREEN + "Time:             " + String.valueOf(minutes) + ":"
							+ String.valueOf(countdown);
					timer.setScore(4);
				}

				// timer.setScore(countdown); // Making it so after "Time:" it displays the int
				// countdown(So how long it
				// has left in seconds.)

			}
		}, 0L, 20L); // repeats every second

		return false;
	}

}
