package com.froobworld.nabsuite.modules.basics.event;

import com.froobworld.nabsuite.modules.basics.teleport.home.Home;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TeleportHomeEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean cancelled = false;
    private final Player player;
    private final Home home;

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public TeleportHomeEvent(Player player, Home home) {
        super();
        this.player = player;
        this.home = home;
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

    public Player getPlayer() {
        return player;
    }

    public Home getHome() {
        return home;
    }
}
