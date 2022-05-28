package me.ShermansWorld.RaidsPerRegion;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;

import me.ShermansWorld.RaidsPerRegion.commands.RaidCommands;
import me.ShermansWorld.RaidsPerRegion.commands.RaidsPerRegionCommands;
import me.ShermansWorld.RaidsPerRegion.listeners.MobListener;
import me.ShermansWorld.RaidsPerRegion.tabCompletion.RaidTabCompletion;
import me.ShermansWorld.RaidsPerRegion.tabCompletion.RaidsPerRegionTabCompletion;

public class Main extends JavaPlugin {
	
	public static Main instance = null;
	public static boolean cancelledRaid = false;
	
	@Override
	public void onEnable() { //What runs when you start server
		instance = this;
		this.saveDefaultConfig();
		getServer().getPluginManager().registerEvents(new MobListener(), this);
		//this.getConfig().options().copyDefaults(false);
		
		//initialize commands
		new RaidsPerRegionCommands(this);
		this.getCommand("raidsperregion").setTabCompleter(new RaidsPerRegionTabCompletion());//Tab completer for raidspreregion command
		new RaidCommands(this);
		this.getCommand("raid").setTabCompleter(new RaidTabCompletion());//Tab completer for raid command
		
		
		// clear scoreboard (lingers if a reload occurs in the middle of a raid)
		for (Objective objective : Bukkit.getScoreboardManager().getMainScoreboard().getObjectives()) {
			if (objective.getName().equalsIgnoreCase("raidKills")) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				    @Override
				    public void run() {
				    	Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "scoreboard objectives remove raidKills");
				    }
				}, 100L); //5 secs after loading, clear scoreboard
				break;
			}
		}
	}
	
	public static Main getInstance() {
		return instance;
	}
	
}
