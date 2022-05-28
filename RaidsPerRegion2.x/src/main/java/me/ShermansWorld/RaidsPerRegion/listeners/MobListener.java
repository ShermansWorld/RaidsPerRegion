package me.ShermansWorld.RaidsPerRegion.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import me.ShermansWorld.RaidsPerRegion.Main;
import me.ShermansWorld.RaidsPerRegion.Raid.Raid;
import me.ShermansWorld.RaidsPerRegion.Raid.TownyHelper;
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
							String killmsg = Main.getInstance().getConfig().getString("BossKilledMessage");
							if (killmsg.contentEquals("") || killmsg == null) {
								return;
							}
							if (killmsg.contains("@PLAYER")) {
								killmsg = killmsg.replaceAll("@PLAYER", player.getName());
							}
							if (killmsg.contains("@TIER")) {
								killmsg = killmsg.replaceAll("@TIER", String.valueOf(Raid.tier));
							}
							if (killmsg.contains("@BOSSNAME")) {
								killmsg = killmsg.replaceAll("@BOSSNAME", event.getMob().getDisplayName());
							}
							if (killmsg.contains("@REGION")) {
								if (Raid.region != null) {
									killmsg = killmsg.replaceAll("@REGION", Raid.region.getId());
								}
							}
							if (killmsg.contains("@TOWN")) {
								if (Raid.town != null) {
									killmsg = TownyHelper.townPlaceHolder(killmsg);
								}
							}
							Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', killmsg));
						}
					}
				} else {
					Raid.otherDeaths++;
					if (mobEntity.equals(Raid.bossEntity)) {
						Raid.boss = "NONE"; // should end the raid
					}
				}
			}
			
		}
	
		
	}
	
}


