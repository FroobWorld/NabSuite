package com.froobworld.nabsuite.modules.basics.teleport.random;

import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.config.BasicsConfig;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class RandomTeleporter {
    private final Random random = new Random();
    private static final int MAX_ATTEMPTS = 15;
    private final BasicsModule basicsModule;

    public RandomTeleporter(BasicsModule basicsModule) {
        this.basicsModule = basicsModule;
    }

    public CompletableFuture<Location> attemptFindLocation(World world) {
        return attemptFindLocation(world, 0);
    }

    private CompletableFuture<Location> attemptFindLocation(World world, int attemptNumber) {
        if (attemptNumber >= MAX_ATTEMPTS) {
            return CompletableFuture.completedFuture(null);
        }
        return testLocation(generateRandomLocation(world))
                .thenCompose(location -> {
                    if (location != null) {
                        return CompletableFuture.completedFuture(location);
                    }
                    return attemptFindLocation(world, attemptNumber + 1);
                });
    }

    private CompletableFuture<Location> testLocation(Location location) {
        return location.getWorld().getChunkAtAsync(location)
                .thenCompose(chunk -> {
                    if (chunk != null) {
                        Block block = location.getWorld().getHighestBlockAt(location);
                        if (block.isSolid()) {
                            Location newLocation = location.clone();
                            newLocation.setY(block.getY() + 1);
                            return CompletableFuture.completedFuture(newLocation);
                        }
                    }
                    return CompletableFuture.completedFuture(null);
                });
    }

    private Location generateRandomLocation(World world) {
        BasicsConfig.RandomTeleportSettings.WorldSettings worldSettings = basicsModule.getConfig().randomTeleport.worldSettings.of(world);
        boolean unboundedX = random.nextBoolean();
        int offsetX = (random.nextBoolean() ? -1 : 1) * (unboundedX ? random.nextInt(worldSettings.majorRadiusX.get()) : (worldSettings.minorRadiusX.get() + random.nextInt(worldSettings.majorRadiusX.get() - worldSettings.minorRadiusX.get())));
        int offsetZ = (random.nextBoolean() ? -1 : 1) * (unboundedX ? (worldSettings.minorRadiusZ.get() + random.nextInt(worldSettings.majorRadiusZ.get() - worldSettings.minorRadiusZ.get())) : random.nextInt(worldSettings.majorRadiusZ.get()));

        return new Location(world, worldSettings.centreX.get() + offsetX + 0.5, 0, worldSettings.centreZ.get() + offsetZ + 0.5);
    }

}
