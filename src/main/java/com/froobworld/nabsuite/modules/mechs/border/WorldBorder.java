package com.froobworld.nabsuite.modules.mechs.border;

import org.bukkit.Location;

public class WorldBorder {
    public final int minX, maxX, minZ, maxZ;
    public final int minBrX, maxBrX, minBrZ, maxBrZ;

    public WorldBorder(int centreX, int centreZ, int radiusX, int radiusZ, int radiusBrX, int radiusBrZ) {
        this.minX = centreX - radiusX;
        this.maxX = centreX + radiusX;
        this.minZ = centreZ - radiusZ;
        this.maxZ = centreZ + radiusZ;
        this.minBrX = centreX - radiusBrX;
        this.maxBrX = centreX + radiusBrX;
        this.minBrZ = centreZ - radiusBrZ;
        this.maxBrZ = centreZ + radiusBrZ;
    }

    public boolean isInBorder(Location location) {
        return location.getX() >= minX && location.getX() <= maxX &&
                location.getZ() >= minZ && location.getZ() <= maxZ;
    }

    public boolean isInBorderRegion(Location location) {
        return location.getX() < minBrX || location.getX() > maxBrX ||
                location.getZ() < minBrZ || location.getZ() > maxBrZ;
    }

    public Location knockBackLocation(Location location, int knockBackDistance) {
        return new Location(
                location.getWorld(),
                Math.max(minX + knockBackDistance, Math.min(maxX - knockBackDistance, location.getX())),
                location.getY(),
                Math.max(minZ + knockBackDistance, Math.min(maxZ - knockBackDistance, location.getZ())),
                location.getYaw(),
                location.getPitch()
        );
    }

}
