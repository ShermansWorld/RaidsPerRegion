package me.ShermansWorld.raidsperregion.towny;

import org.bukkit.Location;
import org.bukkit.World;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;

public class TownyUtil {
	
	private static TownyAPI townyAPI;
	
	public static boolean townContains(Town raidTown, Location location) {
		Town town = WorldCoord.parseWorldCoord(location).getTownOrNull();
		if (town != null) {
			return town == raidTown;
		} else {
			return false;
		}
	}
	
	public static boolean townContains(Town raidTown, World world, int x, int y, int z) {
		Location location = new Location(world, x, y, z);
		Town town = WorldCoord.parseWorldCoord(location).getTownOrNull();
		if (town != null) {
			return town == raidTown;
		} else {
			return false;
		}
	}
	
	public static void forceMobSpawning(Town town) {
		town.setHasMobs(true);
		for (TownBlock townBlock : town.getTownBlocks()) {
			if (!townBlock.hasResident() && !townBlock.isChanged()) {
				townBlock.setType(townBlock.getType());
				townBlock.save();
			}
		}
	}
	
	public static void resetMobSpawning(Town town) {
		town.setHasMobs(false);
		for (TownBlock townBlock : town.getTownBlocks()) {
			if (!townBlock.hasResident() && !townBlock.isChanged()) {
				townBlock.setType(townBlock.getType());
				townBlock.save();
			}
		}
	}
	
	public static void init() {
		townyAPI = TownyAPI.getInstance();
	}
	
	public static TownyAPI getTownyAPI() {
		return townyAPI;
	}
	
}
