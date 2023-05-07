package com.froobworld.nabsuite.modules.admin.suspicious.monitors;

import com.froobworld.nabsuite.modules.admin.AdminModule;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;

import java.util.*;

public class LavaCastMonitor implements ActivityMonitor, Listener {
    private static final int SUSPICION_THRESHOLD = 4000;
    private final Map<UUID, LiquidTracker> liquidTrackers = new HashMap<>();

    public LavaCastMonitor(AdminModule adminModule) {
        Bukkit.getPluginManager().registerEvents(this, adminModule.getPlugin());
    }

    @Override
    public double getSuspicionLevel(Player player) {
        LiquidTracker liquidTracker = getLiquidTracker(player);
        return Math.min(2, (double) (liquidTracker.lavaLocations.size() + liquidTracker.blockFormedLocations.size()) / (double) SUSPICION_THRESHOLD);
    }

    private LiquidTracker getLiquidTracker(Player player) {
        return liquidTrackers.computeIfAbsent(player.getUniqueId(), k -> new LiquidTracker());
    }

    @EventHandler(ignoreCancelled = true)
    private void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Material type;
        if (event.getBucket() == Material.LAVA_BUCKET) {
            type = Material.LAVA;
        } else if (event.getBucket() == Material.WATER_BUCKET) {
            type = Material.WATER;
        } else {
            return;
        }
        getLiquidTracker(event.getPlayer()).addLiquidPlace(type, event.getBlock().getLocation());
    }

    @EventHandler(ignoreCancelled = true)
    private void onBlockFromTo(BlockFromToEvent event) {
        if (event.getBlock().isLiquid()) {
            liquidTrackers.values().forEach(tracker -> tracker.tryAddLiquidFlow(event.getBlock().getType(), event.getBlock().getLocation(), event.getToBlock().getLocation()));
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onBlockForm(BlockFormEvent event) {
        if (event.getBlock().getType() == Material.LAVA) {
            Material newType = event.getNewState().getType();
            if (newType == Material.OBSIDIAN || newType == Material.COBBLESTONE || newType == Material.STONE) {
                liquidTrackers.values().forEach( tracker -> tracker.tryAddBlockForm(event.getBlock().getLocation()));
            }
        }
    }

    private static class LiquidTracker {
        private final Set<Location> waterLocations = new HashSet<>();
        private final Set<Location> lavaLocations = new HashSet<>();
        private final Set<Location> blockFormedLocations = new HashSet<>();

        public void addLiquidPlace(Material material, Location location) {
            if (material == Material.LAVA) {
                lavaLocations.add(location);
            } else if (material == Material.WATER) {
                waterLocations.add(location);
            }
        }

        public void tryAddLiquidFlow(Material material, Location fromLocation, Location toLocation) {
            Set<Location> locations;
            if (material == Material.LAVA) {
                locations = lavaLocations;
            } else if (material == Material.WATER) {
                locations = waterLocations;
            } else {
                return;
            }
            if (locations.contains(fromLocation)) {
                locations.add(toLocation);
            }
        }

        public void tryAddBlockForm(Location location) {
            if (lavaLocations.contains(location)) {
                blockFormedLocations.add(location);
            } else {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            if (dx == 0 && dy == 0 && dz == 0) continue;
                            if (waterLocations.contains(location.clone().add(dx, dy, dz))) {
                                blockFormedLocations.add(location);
                                return;
                            }
                        }
                    }
                }

            }
        }

    }

}
