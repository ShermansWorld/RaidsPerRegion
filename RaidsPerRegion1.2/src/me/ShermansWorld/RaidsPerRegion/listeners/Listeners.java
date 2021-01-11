package me.ShermansWorld.RaidsPerRegion.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import me.ShermansWorld.RaidsPerRegion.commands.RaidCommands;
import net.md_5.bungee.api.ChatColor;

public final class Listeners implements Listener {
	
	@EventHandler
	public void onMythicMobDead(MythicMobDeathEvent event) {
		
		
		if (RaidCommands.region != null || RaidCommands.town != null) { // if a raid is happening
			AbstractEntity mobEntity = event.getMob().getEntity();
			if (RaidCommands.MmEntityList.contains(mobEntity)) {
				LivingEntity killer = event.getKiller();
				if (killer instanceof Player) {
					Player player = (Player) killer;
					if (!RaidCommands.raidKills.containsKey(player.getName())) { // if the player isn't mapped, map them and set kills to one
			        	RaidCommands.raidKills.put(player.getName(), 1);
			        } else {
			        	RaidCommands.raidKills.put(player.getName(), RaidCommands.raidKills.get(player.getName()) + 1); // if already mapped, set kills to kills + 1
			        }
					if (RaidCommands.bossSpawned) {
						if (mobEntity.equals(RaidCommands.bossEntity)) {
							RaidCommands.boss = "NONE"; // should end the raid
							Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4&l[Tier" + String.valueOf(RaidCommands.tier) + " Raid] &4&lBoss slain by &6&l" + player.getName()));
						}
					}
				} else {
					RaidCommands.otherDeaths++;
				}
			}
			
		}
	
		
	}
	
}


