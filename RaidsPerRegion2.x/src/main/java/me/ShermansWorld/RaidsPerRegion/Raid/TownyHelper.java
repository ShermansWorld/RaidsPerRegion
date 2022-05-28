package me.ShermansWorld.RaidsPerRegion.Raid;

import org.bukkit.Location;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.WorldCoord;

public class TownyHelper {
	public static boolean checkTown(Location mobSpawnLocation) {
		try {
			if (!WorldCoord.parseWorldCoord(mobSpawnLocation).getTownBlock().getTown().equals(Raid.town)) {
				return false;
			}
		} catch (NotRegisteredException e) {
			return false;
		}
		return true;
	}
	
	public static String townPlaceHolder(String input) {
		return input.replaceAll("@TOWN", Raid.town.getName());
	}
}
