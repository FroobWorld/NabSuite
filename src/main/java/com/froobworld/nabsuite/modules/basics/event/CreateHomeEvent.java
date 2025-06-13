package com.froobworld.nabsuite.modules.basics.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class CreateHomeEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean cancelled = false;
    private final Location location;
    private final String homeName;

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public CreateHomeEvent(Player player, Location location, String homeName) {
        super(player);
        this.location = location;
        this.homeName = homeName;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public Location getLocation() {
        return location;
    }

    public String getHomeName() {
        return homeName;
    }
}
