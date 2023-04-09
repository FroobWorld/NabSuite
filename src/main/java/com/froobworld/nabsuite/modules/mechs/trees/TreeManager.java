package com.froobworld.nabsuite.modules.mechs.trees;

import com.froobworld.nabsuite.data.DataLoader;
import com.froobworld.nabsuite.data.DataSaver;
import com.froobworld.nabsuite.modules.mechs.MechsModule;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.io.File;
import java.util.regex.Pattern;

public class TreeManager implements Listener {
    private static final Pattern fileNamePattern = Pattern.compile("^(([0-9]+)|(-[0-9]+))\\.(([0-9]+)|(-[0-9]+))\\.json$");
    protected final DataSaver regionDataSaver;
    private final BiMap<UnnaturalLogRegion.Key, UnnaturalLogRegion> logRegionMap = Maps.synchronizedBiMap(HashBiMap.create());
    private final File directory;

    public TreeManager(MechsModule mechsModule) {
        directory = new File(mechsModule.getDataFolder(), "unnatural-logs/");
        regionDataSaver = new DataSaver(mechsModule.getPlugin(), 1200 * 5);
        logRegionMap.putAll(DataLoader.loadAll(
                directory,
                fileName -> fileNamePattern.matcher(fileName.toLowerCase()).matches(),
                bytes -> UnnaturalLogRegion.fromJsonString(this, new String(bytes)),
                (fileName, logRegion) -> logRegion.getKey()
        ));
        regionDataSaver.start();
        regionDataSaver.addDataType(UnnaturalLogRegion.class, logRegion -> logRegion.toJsonString().getBytes(), logRegion -> new File(directory, logRegion.getKey().x() + "." + logRegion.getKey().z() + ".json"));
        Bukkit.getPluginManager().registerEvents(new TreeReplanter(mechsModule, this), mechsModule.getPlugin());
        Bukkit.getPluginManager().registerEvents(this, mechsModule.getPlugin());
    }

    public void shutdown() {
        regionDataSaver.stop();
    }

    private UnnaturalLogRegion getLogRegionData(Location location) {
        UnnaturalLogRegion.Key key = UnnaturalLogRegion.Key.fromLocation(location);
        return logRegionMap.computeIfAbsent(key, k -> {
            UnnaturalLogRegion logRegion = new UnnaturalLogRegion(this, key);
            regionDataSaver.scheduleSave(logRegion);
            return logRegion;
        });
    }

    public boolean isNaturalLog(Location location) {
        return !getLogRegionData(location).containsLocation(location);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onBlockBreak(BlockBreakEvent event) {
        if (Tag.LOGS.isTagged(event.getBlock().getType())) {
            Location location = event.getBlock().getLocation();
            getLogRegionData(location).removeLocation(location);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onBlockPlace(BlockPlaceEvent event) {
        if (Tag.LOGS.isTagged(event.getBlock().getType())) {
            Location location = event.getBlock().getLocation();
            getLogRegionData(location).addLocation(location);
        }
    }

}
