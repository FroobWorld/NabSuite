package com.froobworld.nabsuite.modules.admin.xray;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class VeinTracker {
    private final Cache<Location, Location> playerPlacedCache = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();
    private final Cache<Location, Location> veinBlockCache = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();

    public void playerPlaced(Location location) {
        playerPlacedCache.put(location, location);
    }

    public int addLocation(Location location, Predicate<Material> oreTypePredicate) {
        if (!isTracked(location)) {
            veinBlockCache.put(location, location);
            return followVein(location, oreTypePredicate) + 1;
        }
        return 0;
    }

    private int followVein(Location location, Predicate<Material> oreTypePredicate) {
        int totalCount = 0;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }
                    Location nextLocation = location.clone().add(dx, dy, dz);
                    if (nextLocation.isChunkLoaded()) {
                        if (oreTypePredicate.test(nextLocation.getBlock().getType())) {
                            totalCount += addLocation(nextLocation, oreTypePredicate);
                        }
                    }
                }
            }
        }
        return totalCount;
    }

    private boolean isTracked(Location location) {
        return veinBlockCache.getIfPresent(location) != null || playerPlacedCache.getIfPresent(location) != null;
    }

}
