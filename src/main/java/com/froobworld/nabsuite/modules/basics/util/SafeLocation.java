package com.froobworld.nabsuite.modules.basics.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

public final class SafeLocation {

    private SafeLocation() {}

    public static Location findSafeLocation(Player player, Location location) {
        double playerWidth = player.getBoundingBox().getWidthX();
        double minX = player.getBoundingBox().getMinX() + location.getX() - player.getLocation().getX();
        double minY = location.getBlockY();
        double minZ = player.getBoundingBox().getMinZ() + location.getZ() - player.getLocation().getZ();
        BoundingBox boundingBox = new BoundingBox(minX, minY, minZ, minX + playerWidth, minY + 1, minZ + playerWidth);
        double yShift = 0;

        return new Location(location.getWorld(), location.getX(), minY + yShift, location.getZ(), location.getYaw(), location.getPitch());
    }

}
