package me.ShermansWorld.raidsperregion.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;

import me.ShermansWorld.raidsperregion.raid.Raid;
import me.ShermansWorld.raidsperregion.raid.Raids;

public class RaidTabCompleters implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> completions = new ArrayList<>();
		if (args.length == 1) {
			// Auto-complete available commands
			completions.add("start");
			completions.add("list");
			completions.add("stop");
			completions.add("help");
			return completions;
		} else if (args.length == 2) {
			// Auto-complete worlds
			if (args[0].equalsIgnoreCase("start")) {
				for (World world : Bukkit.getWorlds()) {
					completions.add(world.getName());
				}
				return emptyListCheck(completions);
			} else if (args[0].equalsIgnoreCase("stop")) {
				for (Raid regionRaid : Raids.activeRegionRaids) {
					completions.add(regionRaid.getName());
				}
				return emptyListCheck(completions);
			}
 		} else if (args.length == 3) {
			// Auto-complete regions based on world arg
			if (args[0].equalsIgnoreCase("start")) {
				RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer()
						.get(new BukkitWorld(Bukkit.getWorld(args[1])));
				for (String regionID : rm.getRegions().keySet()) {
					completions.add(regionID);
				}
				return emptyListCheck(completions);
			}
		} else if (args.length == 4) {
			// Auto-complete tiers
			if (args[0].equalsIgnoreCase("start")) {
				completions.add("1");
				completions.add("2");
				completions.add("3");
				return emptyListCheck(completions);
			}
		}
		return Collections.emptyList();
	}
	
	private List<String> emptyListCheck(List<String> completions) {
		if (completions.isEmpty()) {
			return Collections.emptyList();
		} else {
			return completions;
		}
	}

}
