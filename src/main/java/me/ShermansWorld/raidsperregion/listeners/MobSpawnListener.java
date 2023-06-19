package me.ShermansWorld.raidsperregion.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.ShermansWorld.raidsperregion.RaidsPerRegion;
import me.ShermansWorld.raidsperregion.config.Config;
import me.ShermansWorld.raidsperregion.raid.Raids;
import me.ShermansWorld.raidsperregion.raid.RegionRaid;
import me.ShermansWorld.raidsperregion.towny.TownRaid;
import me.ShermansWorld.raidsperregion.towny.TownyUtil;
import me.ShermansWorld.raidsperregion.util.MythicMobsUtil;

public class MobSpawnListener implements Listener {

	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent e) {
		if (!Config.preventVanillaMobsSpawningInRaids) {
			return;
		}
		
		if (RaidsPerRegion.isUsingTowny) {
			if(Raids.activeRegionRaids.isEmpty() && Raids.activeTownRaids.isEmpty()) {
				return;
			}
		} else {
			if (Raids.activeRegionRaids.isEmpty()) {
				return;
			}
		}
		
		// clear any non-raid mobs that spawn
		Entity entity = e.getEntity();
		new BukkitRunnable() {
			public void run() {
				// for each raid, stop vanilla mobs from spawning in raid region
				if(!MythicMobsUtil.getMobManager().isMythicMob(entity)) {
					for (RegionRaid regionRaid : Raids.activeRegionRaids) {
						Location loc = e.getLocation();
						if (regionRaid.getRegion().contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
							e.getEntity().remove();
							this.cancel();
						}
					}
					for (TownRaid townRaid : Raids.activeTownRaids) {
						Location loc = e.getLocation();
						if (TownyUtil.townContains(townRaid.getTown(), loc)) {
							e.getEntity().remove();
							this.cancel();
						}
					}
				}
			}
		}.runTaskLater(RaidsPerRegion.getInstance(), 2);
	}
}
