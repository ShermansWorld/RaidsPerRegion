package me.ShermansWorld.RaidsPerRegion.tabCompletion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class RaidsPerRegionTabCompletion implements TabCompleter{

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> completions = new ArrayList<>();

		if (args.length == 1 && sender.hasPermission("raidsperregion.reload")) {
			completions.add("source");
			completions.add("version");
			completions.add("reload");

			return completions;
		}else if(args.length == 1) {
			completions.add("source");
			completions.add("version");
		}

		return Collections.emptyList();
	}
}
