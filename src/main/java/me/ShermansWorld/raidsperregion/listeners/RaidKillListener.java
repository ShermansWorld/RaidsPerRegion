package me.ShermansWorld.raidsperregion.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import me.ShermansWorld.raidsperregion.RaidsPerRegion;
import me.ShermansWorld.raidsperregion.raid.Raid;
import me.ShermansWorld.raidsperregion.raid.Raids;

public class RaidKillListener implements Listener {

	@EventHandler
	public void onMythicMobDeath(MythicMobDeathEvent e) {
		for (Raid regionRaid : Raids.activeRegionRaids) {

			// Check for boss kill
			if (regionRaid.isBossSpawned()) {
				if (e.getMob().equals(regionRaid.getBossMob())) {
					regionRaid.setBossKilled(true);
					return;
				}
			}

			// Move on to next raid if the mob is not part of this raid
			if (!regionRaid.getMobs().contains(e.getMob())) {
				continue;
			}

			if (e.getKiller() instanceof Player) {
				Player p = (Player) e.getKiller();
				// Add player to partcipants if they are not already
				if (!regionRaid.getParticipantsKillsMap().containsKey(p.getUniqueId())) {
					regionRaid.getParticipantsKillsMap().put(p.getUniqueId(), 0);
				}
				// increase global kills by 1
				regionRaid.setKills(regionRaid.getKills() + 1);
				// increase player's kills
				int playersKills = regionRaid.getParticipantsKillsMap().get(p.getUniqueId());
				playersKills++;
				regionRaid.getParticipantsKillsMap().put(p.getUniqueId(), playersKills);
			}
		}
		
		if (RaidsPerRegion.isUsingTowny) {
			for (Raid townRaid : Raids.activeTownRaids) {

				// Check for boss kill
				if (townRaid.isBossSpawned()) {
					if (e.getMob() == townRaid.getBossMob()) {
						townRaid.setBossKilled(true);
						return;
					}
				}

				// Move on to next raid if the mob is not part of this raid
				if (!townRaid.getMobs().contains(e.getMob())) {
					continue;
				}

				if (e.getKiller() instanceof Player) {
					Player p = (Player) e.getKiller();
					// Add player to partcipants if they are not already
					if (!townRaid.getParticipantsKillsMap().containsKey(p.getUniqueId())) {
						townRaid.getParticipantsKillsMap().put(p.getUniqueId(), 0);
					}
					// increase global kills by 1
					townRaid.setKills(townRaid.getKills() + 1);
					// increase player's kills
					int playersKills = townRaid.getParticipantsKillsMap().get(p.getUniqueId());
					playersKills++;
					townRaid.getParticipantsKillsMap().put(p.getUniqueId(), playersKills);
				}
			}
		}
	}

}
