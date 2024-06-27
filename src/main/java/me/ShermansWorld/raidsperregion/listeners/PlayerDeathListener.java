package me.ShermansWorld.raidsperregion.listeners;


import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import me.ShermansWorld.raidsperregion.RaidsPerRegion;
import me.ShermansWorld.raidsperregion.config.Config;
import me.ShermansWorld.raidsperregion.raid.Raid;
import me.ShermansWorld.raidsperregion.raid.Raids;

import java.util.ArrayList;
import java.util.List;

public class PlayerDeathListener implements Listener {
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e){
		if (Config.keepInventoryInRaids) {
			Player p = (Player) e.getEntity();
			List<ItemStack> drops = new ArrayList<>(e.getDrops());
			Object[] array = drops.toArray();
			for (Raid regionRaid : Raids.activeRegionRaids) {
				if (regionRaid.getActiveParticipants().contains(p.getUniqueId())) {
					e.setKeepInventory(true);
					e.getDrops().clear();
					p.getInventory().setContents((ItemStack[])array);
				}
			}
			if (RaidsPerRegion.isUsingTowny) {
				for (Raid townRaid : Raids.activeTownRaids) {
					if (townRaid.getActiveParticipants().contains(p.getUniqueId())) {
						e.setKeepInventory(true);
						e.getDrops().clear();
						p.getInventory().setContents((ItemStack[])array);
					}
				}
			}
			drops.clear();
		}
		if (Config.keepXPInRaids) {
			Player p = (Player) e.getEntity();
			for (Raid regionRaid : Raids.activeRegionRaids) {
				if (regionRaid.getActiveParticipants().contains(p.getUniqueId())) {
					e.setKeepLevel(true);
					e.setDroppedExp(0);
				}
			}
			if (RaidsPerRegion.isUsingTowny) {
				for (Raid townRaid : Raids.activeTownRaids) {
					if (townRaid.getActiveParticipants().contains(p.getUniqueId())) {
						e.setKeepLevel(true);
						e.setDroppedExp(0);
					}
				}
			}
		}		
	}
}
