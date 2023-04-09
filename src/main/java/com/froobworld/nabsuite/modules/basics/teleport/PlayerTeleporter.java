package com.froobworld.nabsuite.modules.basics.teleport;

import com.froobworld.nabsuite.modules.basics.BasicsModule;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.concurrent.CompletableFuture;

public class PlayerTeleporter {
    private final BasicsModule basicsModule;

    public PlayerTeleporter(BasicsModule basicsModule) {
        this.basicsModule = basicsModule;
    }

    public void teleport(Player player, Location location) {
        basicsModule.getBackManager().setBackLocation(player, player.getLocation());
        player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    public void teleport(Player player, Entity entity) {
        teleport(player, entity.getLocation());
    }

    public CompletableFuture<Location> teleportAsync(Player player, Location location) {
        basicsModule.getBackManager().setBackLocation(player, player.getLocation());
        return player.teleportAsync(location).thenApply(result -> location);
    }

    public CompletableFuture<Location> teleportAsync(Player player, Entity entity) {
        basicsModule.getBackManager().setBackLocation(player, player.getLocation());
        Location location = entity.getLocation();
        return player.teleportAsync(location).thenApply(result -> location);
    }

}
