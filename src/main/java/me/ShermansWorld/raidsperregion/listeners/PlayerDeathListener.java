package me.ShermansWorld.raidsperregion.listeners;


import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import me.ShermansWorld.raidsperregion.RaidsPerRegion;
import me.ShermansWorld.raidsperregion.config.Config;
import me.ShermansWorld.raidsperregion.raid.Raid;
import me.ShermansWorld.raidsperregion.raid.Raids;

public class PlayerDeathListener implements Listener {
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e){
		if (Config.keepInventoryInRaids) {
			Player p = (Player) e.getEntity();
			for (Raid regionRaid : Raids.activeRegionRaids) {
				if (regionRaid.getActiveParticipants().contains(p.getUniqueId())) {
					e.setKeepInventory(true);
				}
			}
			if (RaidsPerRegion.isUsingTowny) {
				for (Raid townRaid : Raids.activeTownRaids) {
					if (townRaid.getActiveParticipants().contains(p.getUniqueId())) {
						e.setKeepInventory(true);
					}
				}
			}
		}
		if (Config.keepXPInRaids) {
			Player p = (Player) e.getEntity();
			for (Raid regionRaid : Raids.activeRegionRaids) {
				if (regionRaid.getActiveParticipants().contains(p.getUniqueId())) {
					e.setKeepLevel(true);
				}
			}
			if (RaidsPerRegion.isUsingTowny) {
				for (Raid townRaid : Raids.activeTownRaids) {
					if (townRaid.getActiveParticipants().contains(p.getUniqueId())) {
						e.setKeepLevel(true);
					}
				}
			}
		}
		
	}
}
