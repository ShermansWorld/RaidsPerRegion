package me.ShermansWorld.raidsperregion.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import me.ShermansWorld.raidsperregion.raid.RegionRaid;

public class RegionRaidWinEvent extends Event {
	
	private static final HandlerList handlers = new HandlerList();
	private RegionRaid regionRaid;
	
	public RegionRaidWinEvent(RegionRaid regionRaid) {
		this.regionRaid = regionRaid;
	}
	
	public RegionRaid getRegionRaid() {
		return regionRaid;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
        return handlers;
    }

}
