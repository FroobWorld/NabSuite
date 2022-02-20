package com.froobworld.nabsuite.modules.basics.teleport;

import com.froobworld.nabsuite.modules.basics.BasicsModule;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class PlayerTeleporter {
    private final BasicsModule basicsModule;

    public PlayerTeleporter(BasicsModule basicsModule) {
        this.basicsModule = basicsModule;
    }

    public void teleport(Player player, Location location) {
        basicsModule.getBackManager().setBackLocation(player, player.getLocation());
        player.teleport(location);
    }

    public void teleport(Player player, Entity entity) {
        teleport(player, entity.getLocation());
    }

    public CompletableFuture<Location> teleportAsync(Player player, Location location) {
        return location.getWorld().getChunkAtAsync(location).thenApply(chunk -> {
            teleport(player, location);
            return location;
        });
    }

}
