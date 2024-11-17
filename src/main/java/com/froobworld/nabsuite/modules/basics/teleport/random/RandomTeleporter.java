package com.froobworld.nabsuite.modules.basics.teleport.random;

import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.config.BasicsConfig;
import com.froobworld.nabsuite.modules.basics.teleport.home.Home;
import com.froobworld.nabsuite.modules.basics.teleport.home.Homes;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class RandomTeleporter {
    private final Random random = new Random();
    private static final int MAX_ATTEMPTS = 50;
    private static final double MIN_DISTANCE_FROM_HOME = 300;
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
                .thenComposeAsync(location -> {
                    if (location != null) {
                        return CompletableFuture.completedFuture(location);
                    }
                    return attemptFindLocation(world, attemptNumber + 1);
                }, Bukkit.getScheduler().getMainThreadExecutor(basicsModule.getPlugin()));
    }

    private CompletableFuture<Location> testLocation(Location location) {
        if (location == null) {
            return CompletableFuture.completedFuture(null);
        }
        ProtectModule protectModule = basicsModule.getPlugin().getModule(ProtectModule.class);
        if (protectModule != null) {
            if (protectModule.getAreaManager().isAreaAtLocation(location)) {
                return CompletableFuture.completedFuture(null);
            }
        }
        for (Homes homes : basicsModule.getHomeManager().getAllHomes()) {
            for (Home home : homes.getHomes()) {
                if (Math.max(Math.abs(home.getLocation().getBlockX() - location.getBlockX()), Math.abs(home.getLocation().getBlockZ() - location.getBlockZ())) < MIN_DISTANCE_FROM_HOME) {
                    return CompletableFuture.completedFuture(null);
                }
            }
        }
        return location.getWorld().getChunkAtAsync(location)
                .thenCompose(chunk -> {
                    if (chunk != null) {
                        Block block = location.getWorld().getHighestBlockAt(location);
                        if (block.isSolid()) {
                            Location newLocation = location.clone();
                            newLocation.setY(block.getY() + 1);
                            for (int i = 0; i < 5; i++) {
                                Block checkBlock = location.getWorld().getBlockAt(newLocation.getBlockX(), newLocation.getBlockY() + i, newLocation.getBlockZ());
                                if (checkBlock.getType() == Material.POWDER_SNOW) {
                                    return CompletableFuture.completedFuture(null);
                                }
                            }
                            return CompletableFuture.completedFuture(newLocation);
                        }
                    }
                    return CompletableFuture.completedFuture(null);
                });
    }

    private Location generateRandomLocation(World world) {
        BasicsConfig.RandomTeleportSettings.WorldSettings worldSettings = basicsModule.getConfig().randomTeleport.worldSettings.of(world);
        int offsetX = (random.nextBoolean() ? -1 : 1) * random.nextInt(worldSettings.radiusX.get());
        int offsetZ = (random.nextBoolean() ? -1 : 1) * random.nextInt(worldSettings.radiusZ.get());
        Location location = new Location(world, worldSettings.centreX.get() + offsetX + 0.5, 0, worldSettings.centreZ.get() + offsetZ + 0.5);

        if (location.getBlockX() > worldSettings.exclusionCentreX.get() - worldSettings.exclusionRadiusX.get()) {
            if (location.getBlockX() < worldSettings.exclusionCentreX.get() + worldSettings.exclusionRadiusX.get()) {
                if (location.getBlockZ() > worldSettings.exclusionCentreZ.get() - worldSettings.exclusionRadiusZ.get()) {
                    if (location.getBlockZ() < worldSettings.exclusionCentreZ.get() + worldSettings.exclusionRadiusZ.get()) {
                        return null;
                    }
                }
            }
        }
        return location;
    }

}
