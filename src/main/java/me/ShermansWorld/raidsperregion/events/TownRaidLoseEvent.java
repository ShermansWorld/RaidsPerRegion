package me.ShermansWorld.raidsperregion.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import me.ShermansWorld.raidsperregion.towny.TownRaid;

public class TownRaidLoseEvent extends Event {
	
	private static final HandlerList handlers = new HandlerList();
	private TownRaid townRaid;
	
	public TownRaidLoseEvent(TownRaid townRaid) {
		this.townRaid = townRaid;
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
