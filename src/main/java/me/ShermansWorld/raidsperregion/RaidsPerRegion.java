package me.ShermansWorld.raidsperregion;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import me.ShermansWorld.raidsperregion.commands.RaidCommands;
import me.ShermansWorld.raidsperregion.commands.RaidTabCompleters;
import me.ShermansWorld.raidsperregion.commands.RaidsPerRegionCommands;
import me.ShermansWorld.raidsperregion.commands.RaidsPerRegionTabCompleters;
import me.ShermansWorld.raidsperregion.config.Config;
import me.ShermansWorld.raidsperregion.listeners.MobSpawnListener;
import me.ShermansWorld.raidsperregion.listeners.PlayerDeathListener;
import me.ShermansWorld.raidsperregion.listeners.PvPListener;
import me.ShermansWorld.raidsperregion.listeners.RaidKillListener;
import me.ShermansWorld.raidsperregion.towny.RaidTownCommands;
import me.ShermansWorld.raidsperregion.towny.RaidTownTabCompleters;
import me.ShermansWorld.raidsperregion.towny.TownyUtil;
import me.ShermansWorld.raidsperregion.util.MythicMobsUtil;

public class RaidsPerRegion extends JavaPlugin {

	public static RaidsPerRegion instance = null;
	public static boolean isUsingTowny = false;
	public static boolean isInDebugMode = false;

	private PluginManager pm = Bukkit.getPluginManager();

	
	public static RaidsPerRegion getInstance() {
		return instance;
	}

	private void registerCommands() {
		getCommand("raid").setExecutor(new RaidCommands(this));
		getCommand("raid").setTabCompleter(new RaidTabCompleters());
		getCommand("raidsperregion").setExecutor(new RaidsPerRegionCommands(this));
		getCommand("raidsperregion").setTabCompleter(new RaidsPerRegionTabCompleters());
	}

	private void registerTabCompletion() {
	}

	private void registerEvents() {
		this.pm.registerEvents(new RaidKillListener(), this);
		this.pm.registerEvents(new MobSpawnListener(), this);
		this.pm.registerEvents(new PvPListener(), this);
		this.pm.registerEvents(new PlayerDeathListener(), this);
	}

	private void initHooks() {
		if (pm.getPlugin("Towny") != null) {
			isUsingTowny = true;
			Bukkit.getLogger().info("[RaidsPerRegion] Towny detected! Enabling support...");
			getCommand("raidtown").setExecutor(new RaidTownCommands(this));
			getCommand("raidtown").setTabCompleter(new RaidTownTabCompleters());
			TownyUtil.init();
		}
		MythicMobsUtil.init();
	}

	public void onEnable() {
		instance = this;
		saveDefaultConfig();
		Config.initConfigVals();
		registerEvents();
		registerCommands();
		registerTabCompletion();
		initHooks();
	}
}
