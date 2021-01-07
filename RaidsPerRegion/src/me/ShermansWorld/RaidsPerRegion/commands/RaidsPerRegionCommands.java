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
		Player p = (Player) sender; // Convert sender into player
		
		if (args.length == 0) {
			p.sendMessage("Invalid arguments");
			p.sendMessage("Cancel Raid: /raidsperregion reload");
			p.sendMessage("Reload Config: /raidsperregion source");
			return false;
		} else {
			if (args[0].equalsIgnoreCase("source")) {
				p.sendMessage("RaidsPerRegion is an open source project developed by ShermansWorld and KristOJa");
				p.sendMessage("Link to source code: https://github.com/ShermansWorld/RaidsPerRegion/");
			} else if (args[0].equalsIgnoreCase("version")) {
				p.sendMessage("Your server is running RaidsPerRegion Version 1.0 for Minecraft 1.16.4");
			
		    } else if (args[0].equalsIgnoreCase("reload")) {
		    	if (!p.hasPermission("raidsperregion.reload")) {
		    		p.sendMessage(ChatColor.RED + "[RaidsPerRegion] You do not have permission to do this");
		    	} else {
		    		plugin.reloadConfig();
			    	plugin.saveDefaultConfig();
					p.sendMessage("RaidsPerRegion config.yml reloaded");
		    	}
				return false;
			} else {
				p.sendMessage("Invalid arguments");
				p.sendMessage("Cancel Raid: /raidsperregion reload");
				p.sendMessage("Reload Config: /raidsperregion source");
				
				return false;
			}
		}
		return false;
		
	}
	
}
