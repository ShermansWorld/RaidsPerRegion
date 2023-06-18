package me.ShermansWorld.raidsperregion.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import me.ShermansWorld.raidsperregion.RaidsPerRegion;
import me.ShermansWorld.raidsperregion.config.Config;
import me.ShermansWorld.raidsperregion.raid.Raid;
import me.ShermansWorld.raidsperregion.raid.Raids;

public class PvPListener implements Listener {
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (Config.disablePvPInRaids) {
			// if both entites are players
			if (e.getEntity().getType().equals(EntityType.PLAYER)
					&& e.getDamager().getType().equals(EntityType.PLAYER)) {
				Player p = (Player) e.getEntity();
				for (Raid regionRaid : Raids.activeRegionRaids) {
					if (regionRaid.getActiveParticipants().contains(p.getUniqueId())) {
						e.setCancelled(true);
						return;
					}
				}
				if (RaidsPerRegion.isUsingTowny) {
					for (Raid townRaid : Raids.activeTownRaids) {
						if (townRaid.getActiveParticipants().contains(p.getUniqueId())) {
							e.setCancelled(true);
							return;
						}
					}
				}
			}
		}

	}
}
