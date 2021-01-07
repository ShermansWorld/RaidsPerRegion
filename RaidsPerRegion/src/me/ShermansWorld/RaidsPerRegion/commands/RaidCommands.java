package me.ShermansWorld.RaidsPerRegion.commands;
import me.ShermansWorld.RaidsPerRegion.Main;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.logging.log4j.core.net.Priority;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MobManager;

public class RaidCommands implements CommandExecutor {
	
	private static Main plugin;
	
	private static boolean timeReached = false;
	private static int totalKills;
	private static boolean maxMobsReached = false;
	private static List<String> mMMobNames = new ArrayList<>();
	private static List<Double> chances = new ArrayList<>();
	private static List<Integer> priorities = new ArrayList<>();
	private boolean runOnce = false;
	private int tier = 1;
	public int countdown;
	
	
	// public variables used in Listener Class
	public static List<Player> playersInRegion = new ArrayList<>();
	public static Map<String, Integer> raidKills = new HashMap<String, Integer>();
	public static ProtectedRegion region;
	public static int otherDeaths = 0;
	public static List<AbstractEntity> MmEntityList = new ArrayList<>();
	public static int mobsSpawned = 0;
	

	public RaidCommands(Main plugin) {
		this.plugin = plugin;
		plugin.getCommand("raid").setExecutor((CommandExecutor) this); // command to run in chat
	}

	// Player that sends command
	// Command it sends
	// Alias of the command which was used
	// args for Other values within command
	
	public void sendPacket(Player player, Object packet) {
	    try {
	        Object handle = player.getClass().getMethod("getHandle").invoke(player);
	        Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
	        playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	public Class<?> getNMSClass(String name) {
	    try {
	        return Class.forName("net.minecraft.server."
	                + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + "." + name);
	    } catch (ClassNotFoundException e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	
	public void send(Player player, String title, String subtitle, int fadeInTime, int showTime, int fadeOutTime) {
	    try {
	        Object chatTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class)
	                .invoke(null, "{\"text\": \"" + title + "\"}");
	        Constructor<?> titleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(
	                getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"),
	                int.class, int.class, int.class);
	        Object packet = titleConstructor.newInstance(
	                getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE").get(null), chatTitle,
	                fadeInTime, showTime, fadeOutTime);

	        Object chatsTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class)
	                .invoke(null, "{\"text\": \"" + subtitle + "\"}");
	        Constructor<?> timingTitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(
	                getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"),
	                int.class, int.class, int.class);
	        Object timingPacket = timingTitleConstructor.newInstance(
	                getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("SUBTITLE").get(null), chatsTitle,
	                fadeInTime, showTime, fadeOutTime);

	        sendPacket(player, packet);
	        sendPacket(player, timingPacket);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	public static void checkPlayersInRegion(ProtectedRegion region, Scoreboard board, Objective objective, MobManager mm, Score totalScore, List<String> mMMobNames, List<Double> chances, List<Integer> priorities, int maxMobsPerPlayer, long conversionSpawnRateMultiplier, double mobLevel) {
		int[] id = {0};
		Random rand = new Random();
		id[0] = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				if (timeReached == true) {
					Bukkit.getServer().getScheduler().cancelTask(id[0]);
				} else {
					List<Player> playerList = new ArrayList<>(Bukkit.getOnlinePlayers());
					playersInRegion = new ArrayList<>();
					List<Location> onlinePlayerLocations = new ArrayList<>();
					List<Location> regionPlayerLocations = new ArrayList<>();
					int scoreCounter = 0;
					
					for (int i = 0; i < playerList.size(); i++) {
						onlinePlayerLocations.add(playerList.get(i).getLocation()); 
						if (region.contains(onlinePlayerLocations.get(i).getBlockX(),
								onlinePlayerLocations.get(i).getBlockY(), onlinePlayerLocations.get(i).getBlockZ())) {
							playersInRegion.add(playerList.get(i));
							regionPlayerLocations.add(playerList.get(i).getLocation()); 
						}
					}
					
					// Add up scores for all players in region
					for (int n = 0; n < playersInRegion.size(); n++) {
						
						// Make sure the player is mapped in raidKills or it will return a null error
						
						if (playersInRegion.get(n).getScoreboard() != board) {
							playersInRegion.get(n).setScoreboard(board);
						}
						
						if (raidKills.containsKey(playersInRegion.get(n).getName())) {
							Score score = objective.getScore(playersInRegion.get(n).getName());
							score.setScore(raidKills.get(playersInRegion.get(n).getName()));
							scoreCounter += score.getScore();
						}
						
					} 
					
					// Spawn mobs for all players in region
					spawnMobs(rand, regionPlayerLocations, scoreCounter, totalScore, mm, mMMobNames, chances, priorities, maxMobsPerPlayer, mobLevel);
				
				}
			}
		}, 0L, 20L/conversionSpawnRateMultiplier);
	}
	
	private void getMobsFromConfig() {
		Set<String> mmMobs = this.plugin.getConfig().getConfigurationSection("RaidMobs").getKeys(false); // only gets top keys
		Iterator<String> it = mmMobs.iterator();
		//converts set to arraylist
		while(it.hasNext()){
			mMMobNames.add(it.next());
	    }
		//gets chance and priority data for each mob name
		for (int k = 0; k < mMMobNames.size(); k++) {
			double chance = this.plugin.getConfig().getConfigurationSection("RaidMobs").getDouble(mMMobNames.get(k) + ".Chance"); 
			int priority = this.plugin.getConfig().getConfigurationSection("RaidMobs").getInt(mMMobNames.get(k) + ".Priority"); 
			chances.add(chance);
			priorities.add(priority);
		}
	}
	
	private static void spawnMobs(Random rand, List<Location>regionPlayerLocations, int scoreCounter, Score totalScore, MobManager mm, List<String> mMMobNames, List<Double> chances, List<Integer> priorities, int maxMobsPerPlayer, double mobLevel) {
		for (int j = 0; j < playersInRegion.size(); j++) {
			int randomPlayerIdx = rand.nextInt(playersInRegion.size());
			World w = playersInRegion.get(j).getWorld();
			int x = regionPlayerLocations.get(randomPlayerIdx).getBlockX() + rand.nextInt(50) - 25;
			int y = regionPlayerLocations.get(randomPlayerIdx).getBlockY();
			int z = regionPlayerLocations.get(randomPlayerIdx).getBlockZ() + rand.nextInt(50) - 25;
	        String mythicMobName;
	        int spawnRate = rand.nextInt(3); // 1/3 chance of spawning zombie at this player per cycle. possibilities: 0, 1 or 2
	        int numPlayersInRegion = playersInRegion.size();
	        int mobsAlive = mobsSpawned - scoreCounter - otherDeaths;
	        
	        if (mobsAlive >= numPlayersInRegion*maxMobsPerPlayer) {
	        	maxMobsReached = true; 	
	        } else {
	            maxMobsReached = false;
	        }
	        if (spawnRate == 2 && !maxMobsReached) {
	        	List<Integer> hitIdxs = new ArrayList<>();
	        	for (int k = 0; k < mMMobNames.size(); k++) {
	        		int randomNum = rand.nextInt(1000) + 1; // generates number between 1 and 1000
	        		if (randomNum <= chances.get(k)*1000) { // test for hit
	        			hitIdxs.add(k); //add hit index
	        		}
	        	}
	        	int maxPriority = 0;
	        	int maxPriorityIdx = 0;
	        	for (int n = 0; n < hitIdxs.size(); n++) {
	        		if (priorities.get(hitIdxs.get(n)) > maxPriority) { // does not account for same priority
	        			maxPriority = priorities.get(hitIdxs.get(n));
	        			maxPriorityIdx = hitIdxs.get(n);
	        		}
	        	}
	        	
	        	mythicMobName = mMMobNames.get(maxPriorityIdx); // set to first idx if nothing else hits
	        	// Fix mob spawning in ground or in air and taking fall damage
	        	if (w.getBlockAt(x,y,z).getType() == Material.AIR) {
	        		while (w.getBlockAt(x, y, z).getType() == Material.AIR) {
		        		y--;
		        	}
	        		y+=2; // fixes mobs spawning half in a block
	        	} else {
	        		while (w.getBlockAt(x, y, z).getType() != Material.AIR) {
		        		y++;
		        	}
	        		y++; // fixes mobs spawning half in a block
	        	}
	        	
	        	Location mobSpawnLocation = new Location(w, x, y, z);
	        	
				ActiveMob mob = mm.spawnMob(mythicMobName, mobSpawnLocation, mobLevel); //type, location, level
				AbstractEntity entityOfMob = mob.getEntity();
				MmEntityList.add(entityOfMob);
				mobsSpawned++;
	        }
	        totalKills = scoreCounter;
	        totalScore.setScore(totalKills);
		}
	}
	
	private boolean isCancelledRaid(String tier, ProtectedRegion region, Player p) {
		if (Main.cancelledRaid) {
			String raidCancelledTitle = plugin.getConfig().getString("RaidCancelledTitle");
			String raidCancelledSubtitle = plugin.getConfig().getString("RaidCancelledSubtitle");
			if (raidCancelledTitle.contains("@TIER")) {
				raidCancelledTitle = raidCancelledTitle.replaceAll("@TIER", tier);
			}
			if (raidCancelledSubtitle.contains("@TIER")) {
				raidCancelledSubtitle = raidCancelledSubtitle.replaceAll("@TIER", tier);
			}
			if (raidCancelledTitle.contains("@REGION")) {
				raidCancelledTitle = raidCancelledTitle.replaceAll("@REGION", region.getId());
			}
			if (raidCancelledSubtitle.contains("@REGION")) {
				raidCancelledSubtitle = raidCancelledSubtitle.replaceAll("@REGION", region.getId());
			}
			if (raidCancelledTitle.contains("@SENDER")) {
				raidCancelledTitle = raidCancelledTitle.replaceAll("@SENDER", p.getName());
			}
			if (raidCancelledSubtitle.contains("@SENDER")) {
				raidCancelledSubtitle = raidCancelledSubtitle.replaceAll("@SENDER", p.getName());
			}
			for (int n = 0; n < playersInRegion.size(); n++) { // Broadcasts title to every player in the raiding region
				send(playersInRegion.get(n), ((ChatColor.translateAlternateColorCodes('&', raidCancelledTitle))), ((ChatColor.translateAlternateColorCodes('&', raidCancelledSubtitle))), 10, 60, 10);
			}
			for (int i = 0; i < MmEntityList.size(); i++) {
				if (MmEntityList.get(i).isLiving()) {
					MmEntityList.get(i).remove();
				}
			}
			return true;
		} else {
			return false;
		}
	}
	
	private boolean isWonRaid(String tier, ProtectedRegion region, Player p, int goal) {
		if (totalKills >= goal) {
			String raidWinTitle = plugin.getConfig().getString("RaidWinTitle");
			String raidWinSubtitle = plugin.getConfig().getString("RaidWinSubtitle");
			if (raidWinTitle.contains("@TIER")) {
				raidWinTitle = raidWinTitle.replaceAll("@TIER", tier);
			}
			if (raidWinSubtitle.contains("@TIER")) {
				raidWinSubtitle = raidWinSubtitle.replaceAll("@TIER", tier);
			}
			if (raidWinTitle.contains("@REGION")) {
				raidWinTitle = raidWinTitle.replaceAll("@REGION", region.getId());
			}
			if (raidWinSubtitle.contains("@REGION")) {
				raidWinSubtitle = raidWinSubtitle.replaceAll("@REGION", region.getId());
			}
			if (raidWinTitle.contains("@SENDER")) {
				raidWinTitle = raidWinTitle.replaceAll("@SENDER", p.getName());
			}
			if (raidWinSubtitle.contains("@SENDER")) {
				raidWinSubtitle = raidWinSubtitle.replaceAll("@SENDER", p.getName());
			}
			for (int n = 0; n < playersInRegion.size(); n++) { // Broadcasts title to every player in the raiding region
				send(playersInRegion.get(n), (ChatColor.translateAlternateColorCodes('&', raidWinTitle)), (ChatColor.translateAlternateColorCodes('&', raidWinSubtitle)), 10, 30, 10);
			}
			for (int i = 0; i < MmEntityList.size(); i++) {
				if (MmEntityList.get(i).isLiving()) {
					MmEntityList.get(i).damage(1000);
				}
			}
			return true;
		} else {
			return false;
		}
	}
	
	private boolean isLostRaid(String tier, ProtectedRegion region, Player p, int goal) {
		if (countdown == 0) {
			String raidLoseTitle = plugin.getConfig().getString("RaidLoseTitle");
			String raidLoseSubtitle = plugin.getConfig().getString("RaidLoseSubtitle");
			if (raidLoseTitle.contains("@TIER")) {
				raidLoseTitle = raidLoseTitle.replaceAll("@TIER", tier);
			}
			if (raidLoseSubtitle.contains("@TIER")) {
				raidLoseSubtitle = raidLoseSubtitle.replaceAll("@TIER", tier);
			}
			if (raidLoseTitle.contains("@REGION")) {
				raidLoseTitle = raidLoseTitle.replaceAll("@REGION", region.getId());
			}
			if (raidLoseSubtitle.contains("@REGION")) {
				raidLoseSubtitle = raidLoseSubtitle.replaceAll("@REGION", region.getId());
			}
			if (raidLoseTitle.contains("@SENDER")) {
				raidLoseTitle = raidLoseTitle.replaceAll("@SENDER", p.getName());
			}
			if (raidLoseSubtitle.contains("@SENDER")) {
				raidLoseSubtitle = raidLoseSubtitle.replaceAll("@SENDER", p.getName());
			}
			if (totalKills < goal) {
				//raid lost
				for (int n = 0; n < playersInRegion.size(); n++) { // Broadcasts title to every player in the raiding region
					send(playersInRegion.get(n), (ChatColor.translateAlternateColorCodes('&', raidLoseTitle)), (ChatColor.translateAlternateColorCodes('&', raidLoseSubtitle)), 10, 30, 10);
				}
			}
			return true;
		} else {
			return false;
		}
	}
	
	private void resetVariables() {
		timeReached = false;
		totalKills = 0;
		mobsSpawned = 0;
		maxMobsReached = false;
		playersInRegion = new ArrayList<>();
		MmEntityList = new ArrayList<>();
		mMMobNames = new ArrayList<>();
		raidKills = new HashMap<String, Integer>();
		Main.cancelledRaid = false;
		runOnce = false;
		priorities = new ArrayList<>();
		chances = new ArrayList<>();
		mMMobNames = new ArrayList<>();
		otherDeaths = 0;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		Player p = (Player) sender; // Convert sender into player
		World w = p.getWorld(); // Get world
		
		//Initial values for tiers from config
		int goal;
		int maxMobsPerPlayer = 10;
		double spawnRateMultiplier = 1.0;
		long conversionSpawnRateMultiplier = 10;
		double mobLevel = 1.0;
		
		//Check arguments
		//--------------------------------------------------------------------------------------------------------------
		
		if (!p.hasPermission("raidsperregion.raid")) {
			p.sendMessage(ChatColor.RED + "[RaidsPerRegion] You do not have permission to do this");
			return false;
		}
		
		if (args.length == 1 && args[0].equalsIgnoreCase("cancel")) {
			if (RaidCommands.region == null) { // if there is not a raid in progress
				p.sendMessage("There is not a raid in progress right now");
				return false;
			} else {
				p.sendMessage("Canceled raid on region " + RaidCommands.region.getId());
				Main.cancelledRaid = true;
				return false;
			}
		}
		
	    if (args.length != 2) {
			p.sendMessage("Invalid arguments. Useage: /raid [region] [tier]");
			return false;
		}
		
		if (region != null) {
			p.sendMessage("There is already a raid in progress in region " + region.getId());
			p.sendMessage("To cancel this raid type /raid cancel");
			return false;
		}
		
		com.sk89q.worldedit.world.World bukkitWorld = BukkitAdapter.adapt(w);
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer(); // Put regions on
		RegionManager regions = container.get(bukkitWorld);
		region = regions.getRegion(args[0]);
		
		Map<String, ProtectedRegion> regionMap = regions.getRegions();
		if (!regionMap.containsKey(args[0])) {
			p.sendMessage("Invalid region. Useage: /raid [region] [tier]");
			return false;
		}
		
		if (args[1].contentEquals("1") || args[1].contentEquals("2") || args[1].contentEquals("3")) {
			tier = Integer.parseInt(args[1]);
			goal = this.plugin.getConfig().getConfigurationSection("Tier" + String.valueOf(tier)).getInt("KillsGoal");
			countdown = this.plugin.getConfig().getConfigurationSection("Tier" + String.valueOf(tier)).getInt("Time");
			maxMobsPerPlayer = this.plugin.getConfig().getConfigurationSection("Tier" + String.valueOf(tier)).getInt("MaxMobsPerPlayer");
			spawnRateMultiplier = this.plugin.getConfig().getConfigurationSection("Tier" + String.valueOf(tier)).getDouble("SpawnRateMultiplier");
			conversionSpawnRateMultiplier = (long) (spawnRateMultiplier);
			mobLevel = this.plugin.getConfig().getConfigurationSection("Tier" + String.valueOf(tier)).getDouble("MobLevel");
			if (conversionSpawnRateMultiplier == 0) {
				conversionSpawnRateMultiplier = 1;
				p.sendMessage("SpawnRateMultipiler too low! defaulting to 1.0");
				
			}
			
		} else {
			p.sendMessage("Invalid tier. Useage: /raid [region] [tier]");
			return false;
		}
		
		//-----------------------------------------------------------------------------------------------------------------------
		
		// variables to be reset for each raid
		resetVariables();
		
		MobManager mm = MythicMobs.inst().getMobManager();
		region.setFlag(Flags.MOB_SPAWNING, StateFlag.State.ALLOW);
		
		Scoreboard board = p.getScoreboard();
		Objective objective = board.registerNewObjective("raidKills", "dummy", "Raid Kills");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		Set<String> namesInScoreboard = new HashSet<String>();
		Score goalKills = objective.getScore((ChatColor.GOLD + "Goal:")); //Get a fake offline player
		Score totalScore = objective.getScore((ChatColor.RED + "Total Kills:")); //Get a fake offline player
		goalKills.setScore(goal);
		
		getMobsFromConfig();
		checkPlayersInRegion(region, board, objective, mm, totalScore, mMMobNames, chances, priorities, maxMobsPerPlayer, conversionSpawnRateMultiplier, mobLevel);
		
		int[] id = {0};
		id[0] = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				
				if (!playersInRegion.isEmpty() && !runOnce) {
					runOnce = true;
					String raidAnnoucementTitle = plugin.getConfig().getString("RaidAnnoucementTitle");
					String raidAnnoucementSubtitle = plugin.getConfig().getString("RaidAnnoucementSubtitle");
					if (raidAnnoucementTitle.contains("@TIER")) {
						raidAnnoucementTitle = raidAnnoucementTitle.replaceAll("@TIER", args[1]);
					}
					if (raidAnnoucementSubtitle.contains("@TIER")) {
						raidAnnoucementSubtitle = raidAnnoucementSubtitle.replaceAll("@TIER", args[1]);
					}
					if (raidAnnoucementTitle.contains("@REGION")) {
						raidAnnoucementTitle = raidAnnoucementTitle.replaceAll("@REGION", region.getId());
					}
					if (raidAnnoucementSubtitle.contains("@REGION")) {
						raidAnnoucementSubtitle = raidAnnoucementSubtitle.replaceAll("@REGION", region.getId());
					}
					if (raidAnnoucementTitle.contains("@SENDER")) {
						raidAnnoucementTitle = raidAnnoucementTitle.replaceAll("@SENDER", p.getName());
					}
					if (raidAnnoucementSubtitle.contains("@SENDER")) {
						raidAnnoucementSubtitle = raidAnnoucementSubtitle.replaceAll("@SENDER", p.getName());
					}
					for (int n = 0; n < playersInRegion.size(); n++) { // Broadcasts title to every player in the raiding region
						send(playersInRegion.get(n), (ChatColor.translateAlternateColorCodes('&', raidAnnoucementTitle)), (ChatColor.translateAlternateColorCodes('&', raidAnnoucementSubtitle)), 10, 30, 10);
					}
				}
				
				// check for canceled, won or lost raid and perform task
				if (isCancelledRaid(args[1], region, p) || isWonRaid(args[1], region, p, goal) || isLostRaid(args[1], region, p, goal)) {
					Bukkit.getServer().getScheduler().cancelTask(id[0]);
					objective.unregister();
					timeReached = true;
					region.setFlag(Flags.MOB_SPAWNING, StateFlag.State.DENY);
					region = null;
					return;
				}
				
				countdown--;
				Score timer = objective.getScore((ChatColor.GREEN + "Time:")); //Making a offline player called "Time:" with a green name and adding it to the scoreboard
				timer.setScore(countdown); //Making it so after "Time:" it displays the int countdown(So how long it has left in seconds.)
				
			}
		}, 0L, 20L); // repeats every second
		
		return false;
	}

}
