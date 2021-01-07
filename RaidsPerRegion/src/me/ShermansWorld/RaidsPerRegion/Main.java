package me.ShermansWorld.RaidsPerRegion;

import org.bukkit.plugin.java.JavaPlugin;

import me.ShermansWorld.RaidsPerRegion.commands.Listeners;
import me.ShermansWorld.RaidsPerRegion.commands.RaidCommands;
import me.ShermansWorld.RaidsPerRegion.commands.RaidsPerRegionCommands;

public class Main extends JavaPlugin {
	
	public static boolean cancelledRaid = false;
	
	@Override
	public void onEnable() { //What runs when you start server
		
		this.saveDefaultConfig();
		getServer().getPluginManager().registerEvents(new Listeners(), this);
		//this.getConfig().options().copyDefaults(false);
		
		//initialize commands
		new RaidsPerRegionCommands(this);
		new RaidCommands(this);
	}
	
}
