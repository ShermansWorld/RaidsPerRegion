package me.ShermansWorld.raidsperregion.commands;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import me.ShermansWorld.raidsperregion.RaidsPerRegion;
import me.ShermansWorld.raidsperregion.raid.Raid;
import me.ShermansWorld.raidsperregion.raid.Raids;
import me.ShermansWorld.raidsperregion.raid.RegionRaid;
import me.ShermansWorld.raidsperregion.util.Helper;

public class RaidCommands implements CommandExecutor {

	private boolean isConsole;

	public RaidCommands(RaidsPerRegion main) {
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			isConsole = false;
			Player p = (Player) sender;
			if (!p.hasPermission("RaidsPerRegion.admin")) {
				p.sendMessage(Helper.chatLabelError() + "You do not have permission to run this command!");
				return false;
			}
		} else {
			isConsole = true;
		}
		if (args.length == 0) {
			helpMsg(sender);
			return false;
		} else if (args.length == 1) {
			String cmdArg = args[0];
			if (cmdArg.equalsIgnoreCase("help")) {
				helpMsg(sender);
				return false;
			} else if (cmdArg.equalsIgnoreCase("start")) {
				incorrectStartArgs(sender);
				return false;
			} else if (cmdArg.equalsIgnoreCase("list")) {
				raidListMsg(sender);
				return true;
			} else if (cmdArg.equalsIgnoreCase("stop")) {
				incorrectStopArgs(sender);
				return false;
			} else {
				unknownCommandMsg(sender);
				return false;
			}
		} else if (args.length == 2) {
			String cmdArg = args[0];
			if (cmdArg.equalsIgnoreCase("start")) {
				incorrectStartArgs(sender);
				return false;
			} else if (cmdArg.equalsIgnoreCase("stop")) {
				String stopArg = args[1];
				for (Raid regionRaid : Raids.activeRegionRaids) {
					if (regionRaid.getName().equalsIgnoreCase(stopArg)) {
						regionRaid.stopRaid();
						raidStopMsg(sender, stopArg);
						return true;
					}
				}
				invalidRaidMsg(sender, stopArg);
				return false;
			} else {
				unknownCommandMsg(sender);
				return false;
			}
		} else if (args.length == 3) {
			String cmdArg = args[0];
			if (cmdArg.equalsIgnoreCase("start")) {
				incorrectStartArgs(sender);
				return false;
			} else {
				unknownCommandMsg(sender);
				return false;
			}
		} else if (args.length == 4) {
			String cmdArg = args[0];
			String worldArg = args[1];
			String regionArg = args[2];
			String tierArg = args[3];
			if (cmdArg.equalsIgnoreCase("start")) {
				boolean validWorld = false;
				for (World world : Bukkit.getWorlds()) {
					if (world.getName().equalsIgnoreCase(worldArg)) {
						validWorld = true;
						break;
					}
				}
				if (!validWorld) {
					invalidWorldMsg(sender, worldArg);
					return false;
				}
				World world = Bukkit.getWorld(worldArg);
				if (world.getDifficulty().equals(Difficulty.PEACEFUL)) {
					difficultyPeacefulMsg(sender, worldArg);
					return false;
				}
				RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer()
						.get(new BukkitWorld(world));
				ProtectedRegion region = null;
				boolean validRegion = false;
				for (String regionID : rm.getRegions().keySet()) {
					if (regionArg.equalsIgnoreCase(regionID)) {
						validRegion = true;
						region = rm.getRegions().get(regionID);
					}
				}
				if (!validRegion) {
					invalidRegionMsg(sender, regionArg);
					return false;
				}
				boolean validTier = false;
				if (tierArg.contentEquals("1") || tierArg.contentEquals("2") || tierArg.contentEquals("3")) {
					validTier = true;
				}
				if (!validTier) {
					invalidTierMsg(sender);
					return false;
				}
				if (!Raids.activeRegionRaids.isEmpty()) {
					for (Raid regionRaid : Raids.activeRegionRaids) {
						if (regionRaid.getName().equalsIgnoreCase(regionArg)) {
							duplicateRaidMsg(sender, regionArg);
							return false;
						}
					}
				}
				Raid raid = new RegionRaid(sender.getName(), regionArg, world, Integer.parseInt(tierArg), region);
				raidStartMsg(sender, worldArg, regionArg, tierArg);
				raid.startRaid(sender, isConsole);
				return true;
			} else {
				unknownCommandMsg(sender);
				return false;
			}
		}

		return true;
	}

	private void helpMsg(CommandSender sender) {
		if (isConsole) {
			sender.sendMessage(Helper.chatLabelConsole() + "[-----HELP MENU----]");
			sender.sendMessage("Start a raid: /raid start [world] [region] [tier]");
			sender.sendMessage("View active sieges: /raid list");
			sender.sendMessage("Reload config: /raidsperregion reload");
		} else {
			sender.sendMessage(Helper.chatLabelNormal() + "[-----HELP MENU----]");
			sender.sendMessage(Helper.color("&fStart a raid: &e/raid start [world] [region] [tier]"));
			sender.sendMessage(Helper.color("&fView active raids: &e/raid list"));
			sender.sendMessage(Helper.color("&fReload config: &e/raidsperregion reload"));
		}

	}

	private void unknownCommandMsg(CommandSender sender) {
		if (isConsole) {
			sender.sendMessage(Helper.chatLabelConsole() + "Unknown command. /raid help");
		} else {
			sender.sendMessage(Helper.chatLabelError() + "Unknown command. " + Helper.color("&e/raid help"));
		}
	}

	private void incorrectStartArgs(CommandSender sender) {
		if (isConsole) {
			sender.sendMessage(
					Helper.chatLabelConsole() + "Unknown/missing arguments. /raid start [world] [region] [tier]");
		} else {
			sender.sendMessage(Helper.chatLabelError() + "Unknown/missing arguments. "
					+ Helper.color("&e/raid start [world] [region] [tier]"));
		}
	}

	private void invalidWorldMsg(CommandSender sender, String worldArg) {
		if (isConsole) {
			sender.sendMessage(Helper.chatLabelConsole() + "Could not find a world named '" + worldArg + "'");
		} else {
			sender.sendMessage(Helper.chatLabelError() + "Could not find a world named '" + worldArg + "'");
		}
	}

	private void invalidRegionMsg(CommandSender sender, String regionArg) {
		if (isConsole) {
			sender.sendMessage(Helper.chatLabelConsole() + "Could not find a region named '" + regionArg + "'");
		} else {
			sender.sendMessage(Helper.chatLabelError() + "Could not find a region named '" + regionArg + "'");
		}
	}

	private void invalidTierMsg(CommandSender sender) {
		if (isConsole) {
			sender.sendMessage(Helper.chatLabelConsole() + "Invalid raid tier. It must be 1-3");
		} else {
			sender.sendMessage(Helper.chatLabelError() + "Invalid raid tier. It must be 1-3");
		}
	}

	private void raidListMsg(CommandSender sender) {
		if (Raids.activeRegionRaids.isEmpty()) {
			if (isConsole) {
				sender.sendMessage(Helper.chatLabelConsole() + "There are currently no active raids");
			} else {
				sender.sendMessage(Helper.chatLabelError() + "There are currently no active raids");
			}
			return;
		}
		sender.sendMessage("===== [ Raid List ] =====");
		for (Raid regionRaid : Raids.activeRegionRaids) {
			sender.sendMessage("ID: " + regionRaid.getID());
			sender.sendMessage("World: " + regionRaid.getWorld().getName());
			sender.sendMessage("Region: " + regionRaid.getName());
			sender.sendMessage("Tier: " + regionRaid.getTier());
			sender.sendMessage("Time Left: " + regionRaid.formattedTimeLeft());
			sender.sendMessage("----------------------");
		}
	}

	private void duplicateRaidMsg(CommandSender sender, String regionArg) {
		if (isConsole) {
			sender.sendMessage(
					Helper.chatLabelConsole() + "There is an ongoing raid in the region '" + regionArg + "'");
		} else {
			sender.sendMessage(Helper.chatLabelError() + "There is an ongoing raid in the region '" + regionArg + "'");
		}
	}

	private void incorrectStopArgs(CommandSender sender) {
		if (isConsole) {
			sender.sendMessage(Helper.chatLabelConsole() + "Unkown/missing arguments. /raid stop [name]");
		} else {
			sender.sendMessage(
					Helper.chatLabelError() + "Unknown/missing arguments. " + Helper.color("&e/raid stop [name]"));
		}
	}
	
	private void invalidRaidMsg(CommandSender sender, String stopArg) {
		if (isConsole) {
			sender.sendMessage(Helper.chatLabelConsole() + "Could not find a raid named '" + stopArg + "'");
		} else {
			sender.sendMessage(Helper.chatLabelError() + "Could not find a raid named '" + stopArg + "'");
		}
	}
	
	private void raidStartMsg(CommandSender sender, String worldArg, String regionArg, String tierArg) {
		if (isConsole) {
			sender.sendMessage(Helper.chatLabelConsole() + "A tier " + tierArg + " raid on the region " + "'" + regionArg + "'" + "(" + worldArg + ") has been started");
		} else {
			sender.sendMessage(Helper.chatLabelNormal() + "A tier " + tierArg + " raid on the region " + Helper.color("&a" + regionArg + "&e ") + "(" + worldArg + ") has been started");
		}
	}
	
	private void raidStopMsg(CommandSender sender, String regionArg) {
		if (isConsole) {
			sender.sendMessage(Helper.chatLabelConsole() + "The raid on the region " + "'" + regionArg + "'" + " has been stopped");
		} else {
			sender.sendMessage(Helper.chatLabelNormal() + "The raid on the region " + Helper.color("&a" + regionArg + "&e ") + "has been stopped");
		}
	}
	
	private void difficultyPeacefulMsg(CommandSender sender, String worldArg) {
		if (isConsole) {
			sender.sendMessage(Helper.chatLabelConsole() + "The difficulty in " + worldArg + " is set to PEACEFUL. Raid cancelled!");
		} else {
			sender.sendMessage(Helper.chatLabelError() + "The difficulty in " + worldArg + " is set to PEACEFUL. Raid cancelled!");
		}
	}

}
