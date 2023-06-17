package me.ShermansWorld.raidsperregion.towny;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;

import me.ShermansWorld.raidsperregion.RaidsPerRegion;
import me.ShermansWorld.raidsperregion.raid.Raid;
import me.ShermansWorld.raidsperregion.raid.Raids;
import me.ShermansWorld.raidsperregion.util.Helper;

public class RaidTownCommands implements CommandExecutor {

	private boolean isConsole;

	public RaidTownCommands(RaidsPerRegion main) {
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// NEW
		if (sender instanceof Player) {
			isConsole = false;
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
				for (Raid townRaid : Raids.activeTownRaids) {
					if (townRaid.getName().equalsIgnoreCase(stopArg)) {
						townRaid.stopRaid();
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
			String townArg = args[2];
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
				Town raidTown = null;
				boolean validTown = false;
				for (Town town : TownyAPI.getInstance().getTowns()) {
					if (town.getName().equalsIgnoreCase(townArg)) {
						validTown = true;
						raidTown = town;
					}
				}
				if (!validTown) {
					invalidTownMsg(sender, townArg);
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
				if (!Raids.activeTownRaids.isEmpty()) {
					for (Raid townRaid : Raids.activeTownRaids) {
						if (townRaid.getName().equalsIgnoreCase(townArg)) {
							duplicateRaidMsg(sender, townArg);
							return false;
						}
					}
				}
				Raid raid = new TownRaid(sender.getName(), townArg, world, Integer.parseInt(tierArg), raidTown);
				raidStartMsg(sender, worldArg, townArg, tierArg);
				raid.startRaid(sender, isConsole);
				return true;
			} else {
				unknownCommandMsg(sender);
				return false;
			}
		}

		return true;
	}

	private void invalidTownMsg(CommandSender sender, String townArg) {
		if (isConsole) {
			sender.sendMessage(Helper.chatLabelConsole() + "Could not find a town named '" + townArg + "'");
		} else {
			sender.sendMessage(Helper.chatLabelError() + "Could not find a town named '" + townArg + "'");
		}
	}

	private void helpMsg(CommandSender sender) {
		if (isConsole) {
			sender.sendMessage(Helper.chatLabelConsole() + "[-----HELP MENU----]");
			sender.sendMessage("Start a raid: /raidtown start [world] [town] [tier]");
			sender.sendMessage("View active sieges: /raidtown list");
			sender.sendMessage("Reload config: /raidsperregion reload");
		} else {
			sender.sendMessage(Helper.chatLabelNormal() + "[-----HELP MENU----]");
			sender.sendMessage(Helper.color("&fStart a raid: &e/raidtown start [world] [town] [tier]"));
			sender.sendMessage(Helper.color("&fView active raids: &e/raidtown list"));
			sender.sendMessage(Helper.color("&fReload config: &e/raidsperregion reload"));
		}

	}

	private void unknownCommandMsg(CommandSender sender) {
		if (isConsole) {
			sender.sendMessage(Helper.chatLabelConsole() + "Unknown command. /raidtown help");
		} else {
			sender.sendMessage(Helper.chatLabelError() + "Unknown command. " + Helper.color("&e/raidtown help"));
		}
	}

	private void incorrectStartArgs(CommandSender sender) {
		if (isConsole) {
			sender.sendMessage(
					Helper.chatLabelConsole() + "Unknown/missing arguments. /raidtown start [world] [town] [tier]");
		} else {
			sender.sendMessage(Helper.chatLabelError() + "Unknown/missing arguments. "
					+ Helper.color("&e/raidtown start [world] [town] [tier]"));
		}
	}

	private void invalidWorldMsg(CommandSender sender, String worldArg) {
		if (isConsole) {
			sender.sendMessage(Helper.chatLabelConsole() + "Could not find a world named '" + worldArg + "'");
		} else {
			sender.sendMessage(Helper.chatLabelError() + "Could not find a world named '" + worldArg + "'");
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
		if (Raids.activeTownRaids.isEmpty()) {
			if (isConsole) {
				sender.sendMessage(Helper.chatLabelConsole() + "There are currently no active raids");
			} else {
				sender.sendMessage(Helper.chatLabelError() + "There are currently no active raids");
			}
			return;
		}
		sender.sendMessage("===== [ Raid List ] =====");
		for (Raid townRaid : Raids.activeTownRaids) {
			sender.sendMessage("ID: " + townRaid.getID());
			sender.sendMessage("World: " + townRaid.getWorld().getName());
			sender.sendMessage("Town: " + townRaid.getName());
			sender.sendMessage("Tier: " + townRaid.getTier());
			sender.sendMessage("Time Left: " + townRaid.formattedTimeLeft());
			sender.sendMessage("----------------------");
		}
	}

	private void duplicateRaidMsg(CommandSender sender, String townArg) {
		if (isConsole) {
			sender.sendMessage(
					Helper.chatLabelConsole() + "There is an ongoing raid in the town '" + townArg + "'");
		} else {
			sender.sendMessage(Helper.chatLabelError() + "There is an ongoing raid in the town '" + townArg + "'");
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

	private void raidStartMsg(CommandSender sender, String worldArg, String townArg, String tierArg) {
		if (isConsole) {
			sender.sendMessage(Helper.chatLabelConsole() + "A tier " + tierArg + " raid on the town " + "'"
					+ townArg + "'" + "(" + worldArg + ") has been started");
		} else {
			sender.sendMessage(Helper.chatLabelNormal() + "A tier " + tierArg + " raid on the town "
					+ Helper.color("&a" + townArg + "&e ") + "(" + worldArg + ") has been started");
		}
	}

	private void raidStopMsg(CommandSender sender, String townArg) {
		if (isConsole) {
			sender.sendMessage(Helper.chatLabelConsole() + "The raid on the town " + "'" + townArg + "'"
					+ " has been stopped");
		} else {
			sender.sendMessage(Helper.chatLabelNormal() + "The raid on the town "
					+ Helper.color("&a" + townArg + "&e ") + "has been stopped");
		}
	}

	private void difficultyPeacefulMsg(CommandSender sender, String worldArg) {
		if (isConsole) {
			sender.sendMessage(Helper.chatLabelConsole() + "The difficulty in " + worldArg
					+ " is set to PEACEFUL. Raid cancelled!");
		} else {
			sender.sendMessage(
					Helper.chatLabelError() + "The difficulty in " + worldArg + " is set to PEACEFUL. Raid cancelled!");
		}
	}

}
