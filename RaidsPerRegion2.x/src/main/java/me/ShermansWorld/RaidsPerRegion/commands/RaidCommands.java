package me.ShermansWorld.RaidsPerRegion.commands;

import me.ShermansWorld.RaidsPerRegion.Main;
import me.ShermansWorld.RaidsPerRegion.Raid.Raid;

import java.util.ArrayList;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

public class RaidCommands implements CommandExecutor {

	public RaidCommands(Main plugin) {
		Main.getInstance().getCommand("raid").setExecutor((CommandExecutor) this); // command to run in chat
	}

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
		
		// Check arguments

		// --------------------------------------------------------------------------------------------------------------

		if (!isConsole) {
			if (!p.hasPermission("raidsperregion.raid")) {
				p.sendMessage(ChatColor.RED + "[RaidsPerRegion] You do not have permission to do this");
				return false;
			}
		}

		if (args.length == 1 && args[0].equalsIgnoreCase("cancel")) {
			if (Raid.region == null && Raid.town == null) { // if there is not a raid in progress
				if (isConsole) {
					Main.getInstance().getLogger().info("There is not a raid in progress right now");
				} else {
					p.sendMessage("[RaidsPerRegion] There is not a raid in progress right now");
				}
				return false;
			} else {
				if (Raid.region != null) {
					if (isConsole) {
						Main.getInstance().getLogger().info("Canceled raid on region" + Raid.region.getId());
					} else {
						p.sendMessage("[RaidsPerRegion] Canceled raid on region " + Raid.region.getId());
					}
				}
				if (Raid.town != null) {
					if (isConsole) {
						Main.getInstance().getLogger().info("Canceled raid on town " + Raid.town.getName());
					} else {
						p.sendMessage("[RaidsPerRegion] Canceled raid on town " + Raid.town.getName());
					}

				}
				Main.cancelledRaid = true;
				if (Raid.isScheduled) {
					Bukkit.getServer().getScheduler().cancelTasks(Main.getInstance()); // cancel all tasks including schedule delay
					Raid.town = null;
					Raid.region = null;
				}
				return false;
			}
		}

		if (args.length < 3 || args.length > 4) {
			if (isConsole) {
				Main.getInstance().getLogger().info("Invalid arguments");
				Main.getInstance().getLogger().info("Usage: /raid region [region] [tier] OR /raid town [town] [tier]");

			} else {
				p.sendMessage("[RaidsPerRegion] Invalid arguments");
				p.sendMessage("[RaidsPerRegion] Usage: /raid region [region] [tier] OR /raid town [town] [tier]");
			}
			return false;
		}

		if (Raid.region != null) {
			if (isConsole) {
				Main.getInstance().getLogger()
						.info("There is already a raid in progress in region " + Raid.region.getId());
				Main.getInstance().getLogger().info("To cancel this raid type /raid cancel");

			} else {
				p.sendMessage("[RaidsPerRegion] There is already a raid in progress in region " + Raid.region.getId());
				p.sendMessage("[RaidsPerRegion] To cancel this raid type /raid cancel");
			}
			return false;
		}

		if (Raid.town != null) {
			if (isConsole) {
				Main.getInstance().getLogger()
						.info("There is already a raid in progress in town " + Raid.town.getName());
				Main.getInstance().getLogger().info("To cancel this raid type /raid cancel");

			} else {
				p.sendMessage("[RaidsPerRegion] There is already a raid in progress in town " + Raid.town.getName());
				p.sendMessage("[RaidsPerRegion] To cancel this raid type /raid cancel");
			}
			return false;
		}

		if (args[0].equalsIgnoreCase("town")) {
			PluginManager pluginManager = Main.getInstance().getServer().getPluginManager();
			if (pluginManager.getPlugin("Towny") == null) {
				if (isConsole) {
					Main.getInstance().getLogger().info("You either do not have Towny installed or it is out of date");
				} else {
					p.sendMessage("[RaidsPerRegion] You either do not have Towny installed or it is out of date");
				}
				return false;
			}

			Raid.town = TownyAPI.getInstance().getTown(args[1]);

			if (Raid.town == null) {
				if (isConsole) {
					Main.getInstance().getLogger().info("Invalid town. Usage: /raid town [town] [tier]");
				} else {
					p.sendMessage("[RaidsPerRegion] Invalid town. Usage: /raid town [town] [tier]");
				}
				return false;
			}
		}

		if (args[0].equalsIgnoreCase("region")) {
			com.sk89q.worldedit.world.World bukkitWorld = BukkitAdapter.adapt(w);
			RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer(); // Put regions on
			RegionManager regions = container.get(bukkitWorld);
			Raid.region = regions.getRegion(args[1]);

			Map<String, ProtectedRegion> regionMap = regions.getRegions();
			if (!regionMap.containsKey(args[1])) {
				if (isConsole) {
					Main.getInstance().getLogger().info("Invalid region. Usage: /raid region [region] [tier]");
				} else {
					p.sendMessage("[RaidsPerRegion] Invalid region. Usage: /raid region [region] [tier]");
				}
				return false;
			}
		}

		if (args[2].contentEquals("1") || args[2].contentEquals("2") || args[2].contentEquals("3")) {
			Raid.tier = Integer.parseInt(args[2]);
			Raid.goal = Main.getInstance().getConfig().getConfigurationSection("Tier" + String.valueOf(Raid.tier))
					.getInt("KillsGoal");
			Raid.countdown = Main.getInstance().getConfig().getConfigurationSection("Tier" + String.valueOf(Raid.tier))
					.getInt("Time");
			Raid.maxMobsPerPlayer = Main.getInstance().getConfig()
					.getConfigurationSection("Tier" + String.valueOf(Raid.tier)).getInt("MaxMobsPerPlayer");
			Raid.spawnRateMultiplier = Main.getInstance().getConfig()
					.getConfigurationSection("Tier" + String.valueOf(Raid.tier)).getDouble("SpawnRateMultiplier");
			Raid.conversionSpawnRateMultiplier = (long) (Raid.spawnRateMultiplier);
			Raid.mobLevel = Main.getInstance().getConfig().getConfigurationSection("Tier" + String.valueOf(Raid.tier))
					.getInt("MobLevel");
			if (Main.getInstance().getConfig().getString("SpawnBossOnKillGoalReached").equalsIgnoreCase("true")) {
				Raid.boss = Main.getInstance().getConfig().getConfigurationSection("Tier" + String.valueOf(Raid.tier))
						.getString("Boss");
			} else {
				Raid.boss = "NONE";
			}
			if (Raid.conversionSpawnRateMultiplier == 0) {
				Raid.conversionSpawnRateMultiplier = 1;
				if (isConsole) {
					Main.getInstance().getLogger().info("SpawnRateMultipiler too low! Defaulting to 1.0");
				} else {
					p.sendMessage("[RaidsPerRegion] SpawnRateMultipiler too low! Defaulting to 1.0");
				}

			}

		} else {
			if (isConsole) {
				Main.getInstance().getLogger().info("Invalid tier. Usage: /raid region [region] [tier] OR /raid town [town] [tier]");
			} else {
				p.sendMessage("[RaidsPerRegion] Invalid tier. Usage: /raid region [region] [tier] OR /raid town [town] [tier]");
			}
			return false;
		}
		
		int delayRaidTicks = 20;
		
		if (args.length == 4) {
			int scheduledMins;
			try {
		        scheduledMins = Integer.parseInt(args[3]);
		    } catch (NumberFormatException nfe) {
		    	if (isConsole) {
					Main.getInstance().getLogger().info("Invalid scheduled time. Usage: /raid region [region] [tier] {Time in mins} OR /raid town [town] [tier] {Time in mins}");
				} else {
					p.sendMessage("Invalid scheduled time. Usage: /raid region [region] [tier] {Time in mins} OR /raid town [town] [tier] {Time in mins}");
				}
		        return false;
		    }
			
			delayRaidTicks = 1200 * scheduledMins; // 1200 ticks per min
			
			Raid.isScheduled = true;
			
			if (Raid.town != null) {
				if (isConsole) {
					Main.getInstance().getLogger().info("[RaidsPerRegion] Raid scheduled for " + Raid.town.getName() + " in " + args[3] + " minutes");
				} else {
					p.sendMessage("[RaidsPerRegion] Raid scheduled for " + Raid.town.getName() + " in " + args[3] + " minutes");
				}
			}
			if (Raid.region != null) {
				if (isConsole) {
					Main.getInstance().getLogger().info("[RaidsPerRegion] Raid scheduled for " + Raid.region.getId() + " in " + args[3] + " minutes");
				} else {
					p.sendMessage("[RaidsPerRegion] Raid scheduled for " + Raid.region.getId() + " in " + args[3] + " minutes");
				}
			}
		}

		// -----------------------------------------------------------------------------------------------------------------------
		
		
		// delay for scheduled task
		int[] id2 = { 0 };
		id2[0] =Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				Raid.resetVariables();
				if (Raid.region != null) {
					if (Raid.region.getFlag(Flags.MOB_SPAWNING) == StateFlag.State.ALLOW) {
						Raid.hasMobsOn = true;
					} else {
						Raid.hasMobsOn = false;
						Raid.region.setFlag(Flags.MOB_SPAWNING, StateFlag.State.ALLOW);
					}
				}

				if (Raid.town != null) {
					if (Raid.town.hasMobs()) {
						Raid.hasMobsOn = true;
					} else {
						Raid.hasMobsOn = false;
						Raid.town.setHasMobs(true);
						for (TownBlock townBlock : Raid.town.getTownBlocks()) {
							if (!townBlock.hasResident() && !townBlock.isChanged()) {
								townBlock.setType(townBlock.getType());
								townBlock.save();
							}
						}
					}
				}
				
				

				ArrayList<Player> online = new ArrayList<Player>(Bukkit.getOnlinePlayers());
				Scoreboard board = online.get(0).getScoreboard();

				Objective objective = board.registerNewObjective("raidKills", "dummy",
						ChatColor.translateAlternateColorCodes('&', "&l&4Raid: " + "Tier " + args[2]));
				Score goalKills = objective.getScore((ChatColor.GOLD + "Goal:             " + String.valueOf(Raid.goal))); // Get a
																														// fake
																														// offline
																														// player
				Score totalScore = objective.getScore(ChatColor.AQUA + "Total Kills:      0"); // Get a fake offline player
				Raid.tempStr2 = ChatColor.AQUA + "Total Kills:      0";
				totalScore.setScore(3);
				goalKills.setScore(2);
				Score separater = objective.getScore(ChatColor.DARK_RED + "----------------------");
				separater.setScore(1);

				Raid.minutes = Raid.countdown / 60;
				Raid.countdown = Raid.countdown % 60;

				Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
					@Override
					public void run() {
						objective.setDisplaySlot(DisplaySlot.SIDEBAR);
					}
				}, 80L); // Do not show scoreboard until title is shown

				Raid.getMobsFromConfig();

				if (Raid.region != null) {
					Raid.checkPlayersInRegion(board, objective, Raid.mMMobNames, Raid.chances, Raid.priorities,
							Raid.maxMobsPerPlayer, Raid.conversionSpawnRateMultiplier, Raid.mobLevel);
				}
				if (Raid.town != null) {
					Raid.checkPlayersInTown(board, objective, Raid.mMMobNames, Raid.chances, Raid.priorities, Raid.maxMobsPerPlayer,
							Raid.conversionSpawnRateMultiplier, Raid.mobLevel);
				}

				int[] id = { 0 };
				id[0] = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), new Runnable() {
					public void run() {
						if (!Raid.playersInRegion.isEmpty() && !Raid.runOnce) {
							Raid.runOnce = true;
							String raidAnnoucementTitle = Main.getInstance().getConfig().getString("RaidAnnoucementTitle");
							String raidAnnoucementSubtitle = Main.getInstance().getConfig()
									.getString("RaidAnnoucementSubtitle");
							if (raidAnnoucementTitle.contains("@TIER")) {
								raidAnnoucementTitle = raidAnnoucementTitle.replaceAll("@TIER", args[2]);
							}
							if (raidAnnoucementSubtitle.contains("@TIER")) {
								raidAnnoucementSubtitle = raidAnnoucementSubtitle.replaceAll("@TIER", args[2]);
							}
							if (Raid.region != null) {
								if (raidAnnoucementTitle.contains("@REGION")) {
									raidAnnoucementTitle = raidAnnoucementTitle.replaceAll("@REGION", Raid.region.getId());
								}
								if (raidAnnoucementSubtitle.contains("@REGION")) {
									raidAnnoucementSubtitle = raidAnnoucementSubtitle.replaceAll("@REGION",
											Raid.region.getId());
								}
							}
							if (Raid.town != null) {
								if (raidAnnoucementTitle.contains("@TOWN")) {
									raidAnnoucementTitle = raidAnnoucementTitle.replaceAll("@TOWN", Raid.town.getName());
								}
								if (raidAnnoucementSubtitle.contains("@TOWN")) {
									raidAnnoucementSubtitle = raidAnnoucementSubtitle.replaceAll("@TOWN", Raid.town.getName());
								}
							}
							if (raidAnnoucementTitle.contains("@SENDER")) {
								raidAnnoucementTitle = raidAnnoucementTitle.replaceAll("@SENDER", sender.getName());
							}
							if (raidAnnoucementSubtitle.contains("@SENDER")) {
								raidAnnoucementSubtitle = raidAnnoucementSubtitle.replaceAll("@SENDER", sender.getName());
							}
							for (int n = 0; n < Raid.playersInRegion.size(); n++) { // Broadcasts title to every player in the
								// raiding region
								Raid.playersInRegion.get(n).sendTitle(
										ChatColor.translateAlternateColorCodes('&', raidAnnoucementTitle),
										ChatColor.translateAlternateColorCodes('&', raidAnnoucementSubtitle), 10, 60, 10);
							}
						}

						// check for canceled, won or lost raid and perform task
						if (Raid.isCancelledRaid(args[2], sender)
								|| Raid.isWonRaid(args[2], Raid.goal, Raid.boss, Raid.mobLevel, sender)
								|| Raid.isLostRaid(args[2], Raid.goal, Raid.minutes, sender)) {
							Bukkit.getServer().getScheduler().cancelTask(id[0]);
							objective.unregister();
							Raid.timeReached = true;
							Raid.boss = "NONE";
							Raid.mobLevel = 1;

							if (Raid.region != null) {
								if (!Raid.hasMobsOn) {
									Raid.region.setFlag(Flags.MOB_SPAWNING, StateFlag.State.DENY);
								}
								Raid.region = null;
							}
							if (Raid.town != null) {
								if (!Raid.hasMobsOn) {
									Raid.town.setHasMobs(false);
									for (TownBlock townBlock : Raid.town.getTownBlocks()) {
										if (!townBlock.hasResident() && !townBlock.isChanged()) {
											townBlock.setType(townBlock.getType());
											townBlock.save();
										}
									}
									// town.getPermissions().mobs = false;
								}
								Raid.town = null;
							}

							return;
						}

						// Making a offline player called "Time:"
						// with a green name and adding it to
						// the scoreboard

						if (Raid.countdown == 0 && Raid.minutes >= 1) {
							Raid.minutes--;
							Raid.countdown += 60;
						}

						Raid.countdown--;

						try {
							board.resetScores(Raid.tempStr);
						} catch (NullPointerException e) {
							// first timer
						}
						if (Raid.countdown <= 9) {
							Score timer = objective.getScore(ChatColor.GREEN + "Time:             "
									+ String.valueOf(Raid.minutes) + ":0" + String.valueOf(Raid.countdown));
							Raid.tempStr = ChatColor.GREEN + "Time:             " + String.valueOf(Raid.minutes) + ":0"
									+ String.valueOf(Raid.countdown);
							timer.setScore(4);
						} else {
							Score timer = objective.getScore(ChatColor.GREEN + "Time:             "
									+ String.valueOf(Raid.minutes) + ":" + String.valueOf(Raid.countdown));
							Raid.tempStr = ChatColor.GREEN + "Time:             " + String.valueOf(Raid.minutes) + ":"
									+ String.valueOf(Raid.countdown);
							timer.setScore(4);
						}

					}
				}, 0L, 20L); // repeats every second
			}
		}, (long) delayRaidTicks); // delay for scheduled raids

		return false;
	}

}
