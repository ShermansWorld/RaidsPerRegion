package me.ShermansWorld.RaidsPerRegion.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.ShermansWorld.RaidsPerRegion.Main;


public class RaidsPerRegionCommands implements CommandExecutor {
	
	private Main plugin;

	public RaidsPerRegionCommands(Main plugin) {
		this.plugin = plugin;
		plugin.getCommand("raidsperregion").setExecutor((CommandExecutor) this); // command to run in chat
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		Player p; // Convert sender into player
		boolean isConsole = false;
		
		if (sender instanceof Player) {
			p = (Player) sender;
		} else {
			isConsole = true;
			p = null;
		}
		
		if (args.length == 0) {
			if (isConsole) {
				plugin.getLogger().info("Invalid arguments");
				plugin.getLogger().info("Reload Config: /raidsperregion reload");
				plugin.getLogger().info("View Source Code: /raidsperregion source");
			} else {
				p.sendMessage("[RaidsPerRegion] Invalid arguments");
				p.sendMessage("[RaidsPerRegion] Reload Config: /raidsperregion reload");
				p.sendMessage("[RaidsPerRegion] View Source Code: /raidsperregion source");
			}
			return false;
		} else {
			if (args[0].equalsIgnoreCase("source")) {
				if (isConsole) {
					plugin.getLogger().info("This plugin is an open source project developed by ShermansWorld and KristOJa");
					plugin.getLogger().info("Link to source code: https://github.com/ShermansWorld/RaidsPerRegion/");
				} else {
					p.sendMessage("[RaidsPerRegion] This plugin is an open source project developed by ShermansWorld and KristOJa");
					p.sendMessage("[RaidsPerRegion] Link to source code: https://github.com/ShermansWorld/RaidsPerRegion/");
				}
			} else if (args[0].equalsIgnoreCase("version")) {
				if (isConsole) {
					plugin.getLogger().info("Your server is running RaidsPerRegion Version 2.1 for Minecraft 1.18.2");
				} else {
					p.sendMessage("[RaidsPerRegion] Your server is running RaidsPerRegion Version 2.1 for Minecraft 1.18.2");
				}
			
		    } else if (args[0].equalsIgnoreCase("reload")) {
		    	if (!isConsole) {
		    		if (!p.hasPermission("raidsperregion.reload")) {
			    		p.sendMessage(ChatColor.RED + "[RaidsPerRegion] You do not have permission to do this");
			    		return false;
			    	}
		    	}
		    		plugin.reloadConfig();
			    	plugin.saveDefaultConfig();
			    	if (isConsole) {
						plugin.getLogger().info("config.yml reloaded");
					} else {
						p.sendMessage("[RaidsPerRegion] config.yml reloaded");
					}
				return false;
			} else {
				
				if (isConsole) {
					plugin.getLogger().info("Invalid arguments");
					plugin.getLogger().info("Reload Config: /raidsperregion reload");
					plugin.getLogger().info("View Source Code: /raidsperregion source");
				} else {
					p.sendMessage("[RaidsPerRegion] Invalid arguments");
					p.sendMessage("[RaidsPerRegion] Reload Config: /raidsperregion reload");
					p.sendMessage("[RaidsPerRegion] View Source Code: /raidsperregion source");
				}
				
				return false;
			}
		}
		return false;
		
	}
	
}
