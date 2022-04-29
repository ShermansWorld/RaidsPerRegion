package me.ShermansWorld.RaidsPerRegion.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import me.ShermansWorld.RaidsPerRegion.Raid.Raid;
import net.md_5.bungee.api.ChatColor;

public final class MobListener implements Listener {
	
	@EventHandler
	public void onMythicMobDead(MythicMobDeathEvent event) {
		
		
		if (Raid.region != null || Raid.town != null) { // if a raid is happening
			AbstractEntity mobEntity = event.getMob().getEntity();
			if (Raid.MmEntityList.contains(mobEntity)) {
				LivingEntity killer = event.getKiller();
				if (killer instanceof Player) {
					Player player = (Player) killer;
					if (!Raid.raidKills.containsKey(player.getName())) { // if the player isn't mapped, map them and set kills to one
						Raid.raidKills.put(player.getName(), 1);
			        } else {
			        	Raid.raidKills.put(player.getName(), Raid.raidKills.get(player.getName()) + 1); // if already mapped, set kills to kills + 1
			        }
					if (Raid.bossSpawned) {
						if (mobEntity.equals(Raid.bossEntity)) {
							Raid.boss = "NONE"; // should end the raid
							Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4&l[Tier" + String.valueOf(Raid.tier) + " Raid] &4&lBoss slain by &6&l" + player.getName()));
						}
					}
				} else {
					Raid.otherDeaths++;
				}
			}
			
		}
	
		
	}
	
}


