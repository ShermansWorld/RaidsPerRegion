package me.ShermansWorld.raidsperregion.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.ShermansWorld.raidsperregion.RaidsPerRegion;
import me.ShermansWorld.raidsperregion.config.Config;
import me.ShermansWorld.raidsperregion.util.Helper;

public class RaidsPerRegionCommands implements CommandExecutor {

	private boolean isConsole;

	public RaidsPerRegionCommands(RaidsPerRegion main) {
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
			return true;
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("reload")) {
				RaidsPerRegion.getInstance().reloadConfig();
				RaidsPerRegion.getInstance().saveDefaultConfig();
				Config.initConfigVals();
				configReloadMsg(sender);
				return true;
			} else if (args[0].equalsIgnoreCase("help")) {
				helpMsg(sender);
				return true;
			} else if (args[0].equalsIgnoreCase("debug")) {
				debuggerMsg(sender);
				return true;
			} else {
				unknownCommandMsg(sender);
				return false;
			}
		} else {
			unknownCommandMsg(sender);
			return false;
		}
	}

	private void unknownCommandMsg(CommandSender sender) {
		if (isConsole) {
			sender.sendMessage(Helper.chatLabelConsole() + "Unknown command. /raidsperregion reload");
		} else {
			sender.sendMessage(
					Helper.chatLabelError() + "Unknown command. " + Helper.color("&e/raidsperregion reload"));
		}
	}

	private void configReloadMsg(CommandSender sender) {
		if (isConsole) {
			sender.sendMessage(Helper.chatLabelConsole() + "Config reloaded");
		} else {
			sender.sendMessage(Helper.chatLabelSuccess() + "Config reloaded");
		}
	}

	private void helpMsg(CommandSender sender) {
		if (isConsole) {
			sender.sendMessage(Helper.chatLabelConsole() + "[-----HELP MENU----]");
			sender.sendMessage("Reload config: /raidsperregion reload");
			sender.sendMessage("Raid commands: /raid help");
		} else {
			sender.sendMessage(Helper.chatLabelNormal() + "[-----HELP MENU----]");
			sender.sendMessage(Helper.color("&fReload config: &e/raidsperregion reload"));
			sender.sendMessage(Helper.color("&fRaid commands: &e/raid help"));
		}

	}

	private void debuggerMsg(CommandSender sender) {
		if (isConsole) {
			if (!RaidsPerRegion.isInDebugMode) {
				sender.sendMessage(Helper.chatLabelConsole() + "Debugger mode enabled");
				RaidsPerRegion.isInDebugMode = true;
			} else {
				sender.sendMessage(Helper.chatLabelConsole() + "Debugger mode disabled");
				RaidsPerRegion.isInDebugMode = false;
			}
		} else {
			if (!RaidsPerRegion.isInDebugMode) {
				sender.sendMessage(Helper.chatLabelSuccess() + "Debugger mode enabled");
				RaidsPerRegion.isInDebugMode = true;
			} else {
				sender.sendMessage(Helper.chatLabelError() + "Debugger mode disabled");
				RaidsPerRegion.isInDebugMode = false;
			}
		}
	}

}
