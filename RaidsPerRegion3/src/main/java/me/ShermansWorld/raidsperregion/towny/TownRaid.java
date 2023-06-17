package me.ShermansWorld.raidsperregion.towny;

import java.util.Iterator;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.palmergames.bukkit.towny.object.Town;

import io.lumine.mythic.core.mobs.ActiveMob;
import me.ShermansWorld.raidsperregion.RaidsPerRegion;
import me.ShermansWorld.raidsperregion.config.Config;
import me.ShermansWorld.raidsperregion.raid.Raid;
import me.ShermansWorld.raidsperregion.raid.RaidMob;
import me.ShermansWorld.raidsperregion.raid.Raids;
import me.ShermansWorld.raidsperregion.util.ScoreboardUtil;
import me.ShermansWorld.raidsperregion.util.Helper;
import me.ShermansWorld.raidsperregion.util.MythicMobsUtil;

public class TownRaid extends Raid {

	private Town town;
	private BukkitTask updateParticipantsTimer;
	private BukkitTask spawnMobsTimer;
	private BukkitTask raidTimer;
	// private boolean townHasMobSpawning;

	public TownRaid(String owner, String name, World world, int tier, Town town) {
		super(owner, name, world, tier);
		this.town = town;
	}

	// ----- Implemented Methods -----

	@Override
	public void startRaid(CommandSender sender, boolean isConsole) {

		Raids.globalRaidID++;
		this.setID(Raids.globalRaidID);
		Raids.activeTownRaids.add(this);

		// see if mob spawning is allowed in region, and force it on if necessary
		forceMobsSpawning();

		if (RaidsPerRegion.isInDebugMode) {
			String mobList = "Raid Mobs: ";
			for (RaidMob raidMob : Raids.raidMobs) {
				mobList += raidMob.getName() + ", ";
			}
			Bukkit.broadcastMessage(mobList);
		}

		updateParticipantsTimer = new BukkitRunnable() {
			public void run() {
				findParticipants();
				if (RaidsPerRegion.isInDebugMode) {
					String participants = "Active Participants: ";
					if (getActiveParticipants().isEmpty()) {
						participants += "None";
					}
					for (UUID playerUUID : getActiveParticipants()) {
						participants += Bukkit.getOfflinePlayer(playerUUID).getName() + ", ";
					}
					Bukkit.broadcastMessage(participants);
				}
			}
		}.runTaskTimer(RaidsPerRegion.getInstance(), 0L, 60L); // Runs instantly, repeats every 3 secs

		spawnMobsTimer = new BukkitRunnable() {
			public void run() {
				spawnMobsForCycle(20);
			}
		}.runTaskTimer(RaidsPerRegion.getInstance(), 0L, (long) (20L * super.getSpawnRate())); // Runs instantly,
																								// repeats every 1 sec *
																								// spawn rate

		raidTimer = new BukkitRunnable() {
			public void run() {
				onRaidTimer();
			}
		}.runTaskTimer(RaidsPerRegion.getInstance(), 0L, 20L); // Runs instantly, repeats every 1 sec

	}

	@Override
	public void stopRaid() {

		// restore region's mobspawning settings prior to raid
		resetMobsSpawning();

		updateParticipantsTimer.cancel();
		spawnMobsTimer.cancel();
		raidTimer.cancel();

		Raids.activeTownRaids.remove(this);

		// remove all spawned mobs
		for (ActiveMob mob : super.getMobs()) {
			if (mob != null) {
				if (!mob.isDead()) {
					mob.remove();
				}
			}
		}

		// remove boss if applicable
		if (super.hasBoss()) {
			if (super.isBossSpawned() && !super.isBossKilled()) {
				super.getBossMob().remove();
			}
		}

		this.sendTitleToParticipants(Config.raidStopTitle, Config.raidStopSubtitle);

		// clear scoreboard for all players
		ScoreboardUtil.clearScoreboards(this);

	}

	// Get all players in the region getting raided and add them to participants.
	// Uses HashSets (duplicates won't be added)

	@Override
	public void findParticipants() {

		for (Player player : Bukkit.getOnlinePlayers()) {
			Location loc = player.getLocation();
			if (TownyUtil.townContains(town, loc)) {
				if (!super.getParticipantsKillsMap().containsKey(player.getUniqueId())) {
					super.getParticipantsKillsMap().put(player.getUniqueId(), 0);
					super.sendTitleToPlayer(player, Config.raidStartTitle, Config.raidStartSubtitle);
					// Show scoreboard after title goes away
					new BukkitRunnable() {
						public void run() {
							// Show scoreboard after title goes away
							createScoreboard(player);
							updateScoreboard(player);
						}
					}.runTaskLater(RaidsPerRegion.getInstance(), 80);
				}
				if (!super.getActiveParticipants().contains(player.getUniqueId())) {
					super.getActiveParticipants().add(player.getUniqueId());
				}
			} else {
				if (super.getActiveParticipants().contains(player.getUniqueId())) {
					super.getActiveParticipants().remove(player.getUniqueId());
				}
			}
		}
	}

	@Override
	public void onRaidWin() {

		// check for boss
		if (super.hasBoss()) {
			if (!super.isBossSpawned()) {
				super.setBossSpawned(spawnBoss(0));
				return;
			}
			if (!super.isBossKilled()) {
				return;
			}
		}

		// restore region's mobspawning settings prior to raid
		resetMobsSpawning();

		// Send title message to anyone who participated
		for (UUID playerUUID : super.getParticipantsKillsMap().keySet()) {
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
			if (offlinePlayer.isOnline()) {
				Player player = (Player) offlinePlayer;
				super.sendTitleToPlayer(player, Config.raidWinTitle, Config.raidWinSubtitle);
			}
		}

		// cancel bukkit tasks
		updateParticipantsTimer.cancel();
		spawnMobsTimer.cancel();
		raidTimer.cancel();

		// remove raid from list
		Raids.activeTownRaids.remove(this);

		// remove all spawned mobs
		for (ActiveMob mob : super.getMobs()) {
			if (mob != null) {
				if (!mob.isDead()) {
					mob.remove();
				}
			}
		}

		// clear scoreboard for all players
		ScoreboardUtil.clearScoreboards(this);

	}

	@Override
	public void onRaidLoss() {

		// restore region's mobspawning settings prior to raid
		resetMobsSpawning();

		// Send title message to anyone who participated
		for (UUID playerUUID : super.getParticipantsKillsMap().keySet()) {
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
			if (offlinePlayer.isOnline()) {
				Player player = (Player) offlinePlayer;
				super.sendTitleToPlayer(player, Config.raidLoseTitle, Config.raidLoseSubtitle);
			}
		}

		// cancel bukkit tasks
		updateParticipantsTimer.cancel();
		spawnMobsTimer.cancel();
		raidTimer.cancel();

		// remove raid from list
		Raids.activeTownRaids.remove(this);

		// clear scoreboard for all players
		ScoreboardUtil.clearScoreboards(this);

		// remove all spawned mobs if setting enabled
		if (!Config.keepMobsOnRaidLoss) {
			for (ActiveMob mob : super.getMobs()) {
				if (mob != null) {
					if (!mob.isDead()) {
						mob.remove();
					}
				}
			}
		}

	}

	@Override
	public boolean spawnBoss(int distanceFactor) {

		// GET RANDOM PLAYER FROM ACTIVE PARTICIPANTS
		int randIndex = (int) (Math.random() * super.getActiveParticipants().size());

		UUID playerUUID = super.getActiveParticipants().get(randIndex);
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
		Player player;
		if (!offlinePlayer.isOnline()) {
			return false;
		} else {
			player = (Player) offlinePlayer;
		}

		// If raid mobs already spawned is at the limit, do not spawn
		if (super.getMobs().size() >= super.getMaxTotalMobs()) {
			if (RaidsPerRegion.isInDebugMode) {
				Bukkit.broadcastMessage(Helper.color("&6Max mob limit reached"));
			}
			return false;
		}

		final int distanceFactorSubtractionPerSpawn = distanceFactor / 10;

		// 10 attempts to spawn a mob
		for (int i = 0; i < 10; i++) {
			if (distanceFactor < 0) {
				// failed to spawn mob after multiple attempts, give up. They are most likely at
				// the edge of the region
				break;
			}
			// GET RANDOM DISTANCE FROM PLAYER

			double radius = distanceFactor * Math.random() + 10;
			// *20+10: random number between 10 and 30
			double angle = Math.toRadians(360 * Math.random());

			// x and z as distances
			int x = (int) (Math.sin(angle) * radius);
			int z = (int) (Math.cos(angle) * radius);

			// ADD DISTANCE TO PLAYER LOCATION TO PRODUCE RAND LOCATION
			x = player.getLocation().getBlockX() + x;
			z = player.getLocation().getBlockZ() + z;

			// CALCULATE Y FOR LOCATION
			// start with player's y location -5
			int minY = player.getLocation().getBlockY() - 10;
			int maxY = player.getLocation().getBlockY() + 10;
			int y = -65;

			for (int testY = minY; testY < maxY; testY++) {
				Location potentialSpawnLoc = new Location(super.getWorld(), x, testY, z);
				if (!potentialSpawnLoc.getBlock().getType().isSolid()) {
					continue;
				}
				potentialSpawnLoc.setY((int) testY + 1);
				if (!potentialSpawnLoc.getBlock().getType().equals(Material.AIR)) {
					continue;
				}
				potentialSpawnLoc.setY((int) testY + 2);
				if (!potentialSpawnLoc.getBlock().getType().equals(Material.AIR)) {
					continue;
				}
				potentialSpawnLoc.setY((int) testY + 3);
				if (!potentialSpawnLoc.getBlock().getType().equals(Material.AIR)) {
					continue;
				}
				y = testY;
				break;
			}

			if (y == -65) {
				continue;
			}

			// Get block above spawnpoint so mob does not spawn in the ground
			y++;

			if (RaidsPerRegion.isInDebugMode) {
				Bukkit.broadcastMessage(Helper.color("&4Testing spawn location at X: " + String.valueOf(x) + " Y: "
						+ String.valueOf(y) + " Z: " + String.valueOf(z)) + "for boss");
			}

			// if mob cannot spawn inside the region, try again with smaller radius
			if (!TownyUtil.townContains(town, super.getWorld(), x, y, z)) {
				distanceFactor -= distanceFactorSubtractionPerSpawn;
				continue;
			}

			// spawn mob
			ActiveMob mob = MythicMobsUtil.spawnRandomMob(new Location(super.getWorld(), x, y, z), super.getMobLevel());
			super.getMobs().add(mob);
			if (RaidsPerRegion.isInDebugMode) {
				Bukkit.broadcastMessage(Helper.color("&4Mob " + mob.getName() + " &4has spawned at X: "
						+ String.valueOf(x) + " Y: " + String.valueOf(y) + " Z: " + String.valueOf(z)));
			}
			// spawn the boss
			super.setBossMob(MythicMobsUtil.spawnMob(super.getBossName(), new Location(super.getWorld(), x, y, z)));
			// Send title message to anyone who participated so far
			for (UUID participantUUID : super.getParticipantsKillsMap().keySet()) {
				OfflinePlayer offlineParticipant = Bukkit.getOfflinePlayer(participantUUID);
				if (offlineParticipant.isOnline()) {
					Player participant = (Player) offlineParticipant;
					super.sendTitleToPlayer(participant, Config.raidBossTitle, Config.raidBossSubtitle);
				}
			}
			// break out of method, boss spawned
			return true;
		}
		if (RaidsPerRegion.isInDebugMode) {
			Bukkit.broadcastMessage(Helper.color("&4Failed to spawn boss... retrying"));
		}
		return false;
	}

	@Override
	public void spawnMobForSpecficParticipant(int distanceFactor, Player player) {
		
		// If raid mobs already spawned is at the limit, do not spawn
		if (super.getMobs().size() >= super.getMaxTotalMobs()) {
			if (RaidsPerRegion.isInDebugMode) {
				Bukkit.broadcastMessage(Helper.color("&6Max mob limit reached"));
			}
			return;
		}

		final int distanceFactorSubtractionPerSpawn = distanceFactor / 10;

		// 10 attempts to spawn a mob
		for (int i = 0; i < 10; i++) {
			if (distanceFactor < 0) {
				// failed to spawn mob after multiple attempts, give up. They are most likely at
				// the edge of the region
				break;
			}
			// GET RANDOM DISTANCE FROM PLAYER

			double radius = distanceFactor * Math.random() + 10;
			// *20+10: random number between 10 and 30
			double angle = Math.toRadians(360 * Math.random());

			// x and z as distances
			int x = (int) (Math.sin(angle) * radius);
			int z = (int) (Math.cos(angle) * radius);

			// ADD DISTANCE TO PLAYER LOCATION TO PRODUCE RAND LOCATION
			x = player.getLocation().getBlockX() + x;
			z = player.getLocation().getBlockZ() + z;

			// CALCULATE Y FOR LOCATION
			// start with player's y location -5
			int minY = player.getLocation().getBlockY() - 10;
			int maxY = player.getLocation().getBlockY() + 10;
			int y = -65;

			for (int testY = minY; testY < maxY; testY++) {
				Location potentialSpawnLoc = new Location(super.getWorld(), x, testY, z);
				if (!potentialSpawnLoc.getBlock().getType().isSolid()) {
					continue;
				}
				potentialSpawnLoc.setY((int) testY + 1);
				if (!potentialSpawnLoc.getBlock().getType().equals(Material.AIR)) {
					continue;
				}
				potentialSpawnLoc.setY((int) testY + 2);
				if (!potentialSpawnLoc.getBlock().getType().equals(Material.AIR)) {
					continue;
				}
				potentialSpawnLoc.setY((int) testY + 3);
				if (!potentialSpawnLoc.getBlock().getType().equals(Material.AIR)) {
					continue;
				}
				y = testY;
				break;
			}

			if (y == -65) {
				continue;
			}

			// Get block above spawnpoint so mob does not spawn in the ground
			y++;

			if (RaidsPerRegion.isInDebugMode) {
				Bukkit.broadcastMessage(Helper.color("&eTesting spawn location at X: " + String.valueOf(x) + " Y: "
						+ String.valueOf(y) + " Z: " + String.valueOf(z)));
			}

			// if mob cannot spawn inside the region, try again with smaller radius
			if (!TownyUtil.townContains(town, super.getWorld(), x, y, z)) {
				distanceFactor -= distanceFactorSubtractionPerSpawn;
				continue;
			}

			// spawn mob
			ActiveMob mob = MythicMobsUtil.spawnRandomMob(new Location(super.getWorld(), x, y, z), super.getMobLevel());
			super.getMobs().add(mob);
			if (RaidsPerRegion.isInDebugMode) {
				Bukkit.broadcastMessage(Helper.color("&aMob " + mob.getName() + " has spawned at X: "
						+ String.valueOf(x) + " Y: " + String.valueOf(y) + " Z: " + String.valueOf(z)));
			}
			// break out of loop, mob spawned
			return;
		}
		if (RaidsPerRegion.isInDebugMode) {
			Bukkit.broadcastMessage(Helper.color("&cCould not spawn mob, abandoning"));
		}

	}

	@Override
	public void onRaidTimer() {

		super.cleanStoredMobs();

		super.setTimeLeft(super.getTimeLeft() - 1);

		// iterate through the spawned mobs of the raid
		Iterator<ActiveMob> i = super.getMobs().iterator();
		while (i.hasNext()) {
			ActiveMob mob = i.next();
			if (mob == null) {
				// remove mob from set if it is null (happens after mob is dead for an interval,
				// seems to be part of MythicMobs)
				i.remove();
			}
		}

		// update scoreboard for all participants
		for (UUID playerUUID : super.getParticipantsKillsMap().keySet()) {
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
			if (offlinePlayer.isOnline()) {
				Player player = (Player) offlinePlayer;
				super.updateScoreboard(player);
			}
		}

		// check for win
		if (super.getKills() >= super.getKillLimit()) {
			onRaidWin();
			return;
		}

		// check for loss
		if (super.getTimeLeft() <= 0) {
			onRaidLoss();
			return;
		}

	}
	
	@Override
	public void forceMobsSpawning() {
		if (!town.hasMobs()) {
			TownyUtil.forceMobSpawning(town);
			this.setMobSpawning(false);
		} else {
			this.setMobSpawning(true);
		}

	}

	@Override
	public void resetMobsSpawning() {
		if (this.getMobSpawning() == false) {
			TownyUtil.resetMobSpawning(town);
		}

	}

	// Getters and Setters
	public Town getTown() {
		return town;
	}

}
