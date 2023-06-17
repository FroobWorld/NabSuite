package com.froobworld.nabsuite.modules.mechs.border;

import com.froobworld.nabsuite.modules.mechs.MechsModule;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.WorldLoadEvent;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerSet;

public class WorldBorderDynmapHook {
    private final WorldBorderManager worldBorderManager;
    private final DynmapAPI dynmapAPI;
    private final MarkerSet borderRegionMarkerSet;
    private final MarkerSet worldBorderMarkerSet;

    public WorldBorderDynmapHook(MechsModule mechsModule, WorldBorderManager worldBorderManager) {
        this.worldBorderManager = worldBorderManager;
        this.dynmapAPI = mechsModule.getPlugin().getHookManager().getDynmapHook().getDynmapAPI();
        borderRegionMarkerSet = dynmapAPI == null ? null : dynmapAPI.getMarkerAPI().createMarkerSet("nabsuite.mechs.borderregion.markerset", "Border region limit", null, false);
        worldBorderMarkerSet = dynmapAPI == null ? null : dynmapAPI.getMarkerAPI().createMarkerSet("nabsuite.mechs.worldborder.markerset", "World border", null, false);
        if (worldBorderMarkerSet != null) {
            worldBorderMarkerSet.setHideByDefault(true);
        }
        Bukkit.getWorlds().forEach(this::registerWorld);
    }

    private void registerWorld(World world) {
        if (dynmapAPI != null) {
            WorldBorder worldBorder = worldBorderManager.getWorldBorder(world);
            if (worldBorder != null) {
                double[] borderRegionX = new double[]{worldBorder.minBrX, worldBorder.maxBrX};
                double[] borderRegionZ = new double[]{worldBorder.minBrZ, worldBorder.maxBrZ};
                double[] worldBorderX = new double[]{worldBorder.minX, worldBorder.maxX};
                double[] worldBorderZ = new double[]{worldBorder.minZ, worldBorder.maxZ};

                AreaMarker borderRegionMarker = borderRegionMarkerSet.createAreaMarker(
                        "nabsuite.mechs.borderregion_" + world.getName(),
                        "The border region limit of this world.",
                        false, world.getName(),
                        borderRegionX,
                        borderRegionZ,
                        true
                );
                borderRegionMarker.setLineStyle(3, 1.0, 0xFF0000);
                borderRegionMarker.setFillStyle(0.0, 0x000000);

                AreaMarker worldBorderMarker = worldBorderMarkerSet.createAreaMarker(
                        "nabsuite.mechs.worldborder_" + world.getName(),
                        "The border of this world.",
                        false, world.getName(),
                        worldBorderX,
                        worldBorderZ,
                        true
                );
                worldBorderMarker.setLineStyle(3, 1.0, 0xFF0000);
                worldBorderMarker.setFillStyle(0.0, 0x000000);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onWorldLoad(WorldLoadEvent event) {
        registerWorld(event.getWorld());
    }

}
