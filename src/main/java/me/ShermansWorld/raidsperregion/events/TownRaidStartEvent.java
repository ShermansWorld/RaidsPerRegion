package me.ShermansWorld.raidsperregion.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import me.ShermansWorld.raidsperregion.towny.TownRaid;

public class TownRaidStartEvent extends Event implements Cancellable {
	
	private static final HandlerList handlers = new HandlerList();
	private TownRaid townRaid;
	private boolean isCancelled;
	
	public TownRaidStartEvent(TownRaid townRaid) {
		this.townRaid = townRaid;
	}
	
	public boolean isCancelled() {
		return isCancelled;
	}
	
	public void setCancelled(boolean cancel) {
		this.isCancelled = cancel;
	}
	
	public TownRaid getTownRaid() {
		return townRaid;
	}


	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
        return handlers;
    }

}
