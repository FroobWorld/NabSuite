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
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RandomTeleporter {
    private final Random random = new Random();
    private static final int MAX_ATTEMPTS = 50;
    private static final double MIN_DISTANCE_FROM_HOME = 300;
    private final BasicsModule basicsModule;
    private final Map<World, Queue<Location>> pregenerated = new HashMap<>();
    private final Map<World, Set<Biome>> excludeBiomes = new HashMap<>();

    public RandomTeleporter(BasicsModule basicsModule) {
        this.basicsModule = basicsModule;

        int offset = 0;
        for (String worldName: basicsModule.getConfig().randomTeleport.enabledWorlds.get()) {
            final World world = Bukkit.getWorld(worldName);
            if (world == null) {
                continue;
            }
            final BasicsConfig.RandomTeleportSettings.WorldSettings worldSettings = basicsModule.getConfig().randomTeleport.worldSettings.of(world);
            if (worldSettings == null) {
                continue;
            }

            excludeBiomes.put(world, new HashSet<>(
                    worldSettings.excludeBiomes.get()
                            .stream()
                            .map(name -> {
                                for (Biome biome: Biome.values()) {
                                    if (biome.name().equalsIgnoreCase(name.trim())) {
                                        return biome;
                                    }
                                }
                                basicsModule.getPlugin().getSLF4JLogger().warn("RandomTeleporter unknown biome in exclude-biomes: " + name);
                                return null;
                            })
                            .filter(Objects::nonNull)
                            .toList())
            );

            final Integer pregenerateMax = worldSettings.pregenerateMax.get();
            final Integer pregenerateInterval = worldSettings.pregenerateInterval.get();
            if (pregenerateMax != null && pregenerateInterval != null && pregenerateMax > 0 && pregenerateInterval > 0) {
                final Queue<Location> pregeneratedLocations = new ConcurrentLinkedQueue<>();
                pregenerated.put(world, pregeneratedLocations);
                Bukkit.getScheduler().runTaskTimerAsynchronously(
                        basicsModule.getPlugin(),
                        () -> this.pregenerateTeleports(world, pregeneratedLocations, pregenerateMax),
                        pregenerateInterval + offset,
                        pregenerateInterval
                );
                // Stagger initial delay between worlds
                offset += 20;
            }

        }
    }

    public CompletableFuture<Location> attemptFindLocation(World world) {
        return attemptFindLocation(world, 0);
    }

    private CompletableFuture<Location> attemptFindLocation(World world, int attemptNumber) {
        if (attemptNumber >= MAX_ATTEMPTS) {
            return CompletableFuture.completedFuture(null);
        }
        Location randomLocation = pregenerated.containsKey(world) ? pregenerated.get(world).poll() : null;
        if (randomLocation == null) {
            randomLocation = generateRandomLocation(world);
        }
        return testLocation(randomLocation)
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
                if (!location.getWorld().equals(home.getLocation().getWorld())) {
                    continue;
                }
                if (Math.max(Math.abs(home.getLocation().getBlockX() - location.getBlockX()), Math.abs(home.getLocation().getBlockZ() - location.getBlockZ())) < MIN_DISTANCE_FROM_HOME) {
                    return CompletableFuture.completedFuture(null);
                }
            }
        }
        return location.getWorld().getChunkAtAsync(location)
                .thenCompose(chunk -> {
                    if (chunk != null) {
                        Block block = location.getWorld().getHighestBlockAt(location);
                        if (excludeBiomes.containsKey(location.getWorld()) && excludeBiomes.get(location.getWorld()).contains(block.getBiome())) {
                            return CompletableFuture.completedFuture(null);
                        }
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

    private void pregenerateTeleports(World world, Queue<Location> queue, Integer maxLocations) {
        if (queue.size() >= maxLocations) {
            return;
        }
        testLocation(generateRandomLocation(world))
                .thenComposeAsync(location -> {
                    if (location != null) {
                        queue.add(location);
                    }
                    return CompletableFuture.completedFuture(location);
                }, Bukkit.getScheduler().getMainThreadExecutor(basicsModule.getPlugin()));
    }

    public List<WorldStatus> getStatus() {
        List<WorldStatus> status = new LinkedList<>();
        for (String worldName: basicsModule.getConfig().randomTeleport.enabledWorlds.get()) {
            final World world = Bukkit.getWorld(worldName);
            if (world == null) {
                continue;
            }
            final BasicsConfig.RandomTeleportSettings.WorldSettings worldSettings = basicsModule.getConfig().randomTeleport.worldSettings.of(world);
            if (worldSettings == null) {
                continue;
            }
            status.add(new WorldStatus(
                    world,
                    pregenerated.containsKey(world) ? pregenerated.get(world).size() : 0,
                    worldSettings.pregenerateMax.get()
            ));
        }
        return status;
    }

    public static class WorldStatus {
        private final World world;
        private final Integer pregenerated;
        private final Integer pregenerateMax;

        public WorldStatus(World world, Integer pregenerated, Integer pregenerateMax) {
            this.world = world;
            this.pregenerated = pregenerated;
            this.pregenerateMax = pregenerateMax;
        }

        public World getWorld() {
            return world;
        }

        public Integer getPregenerated() {
            return pregenerated;
        }

        public Integer getPregenerateMax() {
            return pregenerateMax;
        }
    }
}
