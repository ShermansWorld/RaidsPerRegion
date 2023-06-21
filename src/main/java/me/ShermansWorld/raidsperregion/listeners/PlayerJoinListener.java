package me.ShermansWorld.raidsperregion.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.ShermansWorld.raidsperregion.RaidsPerRegion;
import me.ShermansWorld.raidsperregion.raid.Raid;
import me.ShermansWorld.raidsperregion.raid.Raids;

public class PlayerJoinListener implements Listener {
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		for (Raid regionRaid : Raids.activeRegionRaids) {
			if (regionRaid.getParticipantsKillsMap().containsKey(p.getUniqueId())) {
				regionRaid.createScoreboard(p);
			}
		}
		if (RaidsPerRegion.isUsingTowny) {
			for (Raid townRaid : Raids.activeTownRaids) {
				if (townRaid.getParticipantsKillsMap().containsKey(p.getUniqueId())) {
					townRaid.createScoreboard(p);
				}
			}
		}
	}
}
