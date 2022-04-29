package me.ShermansWorld.RaidsPerRegion.tabCompletion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;

public class RaidTabCompletion implements TabCompleter{

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		Player p;
		World w;
		if (sender instanceof Player) {
			p = (Player) sender; // Convert sender into player
			w = p.getWorld(); // Get world
		} else {
			ArrayList<Player> online = new ArrayList<Player>(Bukkit.getOnlinePlayers());
			w = online.get(0).getWorld();
			p = null;
		}
		
		com.sk89q.worldedit.world.World bukkitWorld = BukkitAdapter.adapt(w);
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionManager regions = container.get(bukkitWorld);
		Object[] regionHolder = regions.getRegions().keySet().toArray();
		
		
		List<String> completions = new ArrayList<>();

		if (args.length == 1 && sender.hasPermission("raidsperregion.raid")) {
			completions.add("region");
			completions.add("town");
			completions.add("cancel");

			return completions;
		}
		
		if (args.length == 2 && sender.hasPermission("raidsperregion.raid") && args[0].equalsIgnoreCase("region")) {
			for(int i = 0; i < regionHolder.length; i++) {
				completions.add(regionHolder[i].toString());
			}

			return completions;
		}
		
		if (args.length == 2 && sender.hasPermission("raidsperregion.raid") && args[0].equalsIgnoreCase("town")) {
			
			List <Town> towns = TownyAPI.getInstance().getTowns();
			
			for(int i = 0; i < towns.size(); i++) {
				completions.add(towns.get(i).getName());
			}
			
			return completions;
		}
		
		if (args.length == 3 && sender.hasPermission("raidsperregion.raid") && ((args[0].equalsIgnoreCase("region")) || (args[0].equalsIgnoreCase("town")))) {
			completions.add("1");
			completions.add("2");
			completions.add("3");

			return completions;
		}
		

		return Collections.emptyList();
	}
}
