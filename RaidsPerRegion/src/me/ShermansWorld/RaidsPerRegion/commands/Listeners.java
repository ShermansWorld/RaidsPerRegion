package me.ShermansWorld.RaidsPerRegion.commands;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;

public final class Listeners implements Listener {
	
	@EventHandler
	public void onMythicMobDead(MythicMobDeathEvent event) {
		
		
		if (RaidCommands.region != null) { // if a raid is happening
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
				} else {
					RaidCommands.otherDeaths++;
				}
			}
			
		}
	
		
	}
	
}


