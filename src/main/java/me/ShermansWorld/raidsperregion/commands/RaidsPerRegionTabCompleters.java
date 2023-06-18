package me.ShermansWorld.raidsperregion.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class RaidsPerRegionTabCompleters implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> completions = new ArrayList<>();
		if (args.length == 1) {
			// Auto-complete available commands
			completions.add("reload");
			completions.add("help");
			return completions;
		} 
		return Collections.emptyList();
	}

}
