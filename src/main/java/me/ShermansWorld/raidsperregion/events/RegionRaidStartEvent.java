package me.ShermansWorld.raidsperregion.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import me.ShermansWorld.raidsperregion.raid.RegionRaid;

public class RegionRaidStartEvent extends Event implements Cancellable {
	
	private static final HandlerList handlers = new HandlerList();
	private RegionRaid regionRaid;
	private boolean isCancelled;
	
	public RegionRaidStartEvent(RegionRaid regionRaid) {
		this.regionRaid = regionRaid;
	}
	
	public boolean isCancelled() {
		return isCancelled;
	}
	
	public void setCancelled(boolean cancel) {
		this.isCancelled = cancel;
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
