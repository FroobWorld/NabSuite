package com.froobworld.nabsuite.modules.mechs.border;

import com.froobworld.nabsuite.modules.mechs.MechsModule;
import com.froobworld.nabsuite.modules.mechs.border.loot.LootLimitManager;
import com.froobworld.nabsuite.modules.mechs.config.MechsConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class WorldBorderManager implements Listener {
    private final MechsModule mechsModule;
    private final Map<World, WorldBorder> worldBorderMap = new HashMap<>();
    private final LootLimitManager lootLimitManager;
    private final Map<Player, Boolean> borderRegionWarningMap = new WeakHashMap<>();
    private final NamespacedKey borderRegionWarningKey;

    public WorldBorderManager(MechsModule mechsModule) {
        this.mechsModule = mechsModule;
        Bukkit.getWorlds().forEach(this::addWorldBorder);
        Bukkit.getPluginManager().registerEvents(this, mechsModule.getPlugin());
        new WorldBorderEnforcer(mechsModule, this);
        lootLimitManager = new LootLimitManager(mechsModule, this);
        new WorldBorderDynmapHook(mechsModule, this);
        new BorderRegionWarning(mechsModule, this);
        this.borderRegionWarningKey = NamespacedKey.fromString("border-region-warning", mechsModule.getPlugin());
    }

    public void shutdown() {
        lootLimitManager.shutdown();
    }

    private void addWorldBorder(World world) {
        MechsConfig.WorldBorderSettings worldBorderSettings = mechsModule.getConfig().worldBorder.of(world);
        if (!worldBorderSettings.useBorder.get()) {
            worldBorderMap.remove(world);
            return;
        }
        worldBorderMap.put(world, new WorldBorder(worldBorderSettings.centreX.get(), worldBorderSettings.centreZ.get(),
                worldBorderSettings.radiusX.get(), worldBorderSettings.radiusZ.get(),
                worldBorderSettings.borderRegionRadiusX.get(), worldBorderSettings.borderRegionRadiusZ.get())
        );
    }

    public WorldBorder getWorldBorder(World world) {
        return worldBorderMap.get(world);
    }

    public boolean isBorderRegion(Location location) {
        WorldBorder worldBorder = getWorldBorder(location.getWorld());
        if (worldBorder != null) {
            return worldBorder.isInBorderRegion(location);
        }
        return false;
    }

    public boolean acceptedBorderRegionWarning(Player player) {
        return borderRegionWarningMap.computeIfAbsent(player, p -> player.getPersistentDataContainer().has(borderRegionWarningKey));
    }

    public void acceptBorderRegionWarning(Player player) {
        borderRegionWarningMap.put(player, true);
        player.getPersistentDataContainer().set(borderRegionWarningKey, PersistentDataType.BYTE, (byte) 1);
    }

    @EventHandler(ignoreCancelled = true)
    private void onWorldLoad(WorldLoadEvent event) {
        addWorldBorder(event.getWorld());
    }

}
