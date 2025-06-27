package com.froobworld.nabsuite.modules.mechs.border;

import com.froobworld.nabsuite.data.playervar.PlayerVars;
import com.froobworld.nabsuite.modules.mechs.MechsModule;
import com.froobworld.nabsuite.modules.mechs.border.loot.LootLimitManager;
import com.froobworld.nabsuite.modules.mechs.config.MechsConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.HashMap;
import java.util.Map;

public class WorldBorderManager implements Listener {
    private static final String ACCEPTS_WARNING_KEY = "accepts-border-region-warning";
    private final MechsModule mechsModule;
    private final Map<World, WorldBorder> worldBorderMap = new HashMap<>();
    private final LootLimitManager lootLimitManager;

    public WorldBorderManager(MechsModule mechsModule) {
        this.mechsModule = mechsModule;
        Bukkit.getWorlds().forEach(this::addWorldBorder);
        Bukkit.getPluginManager().registerEvents(this, mechsModule.getPlugin());
        new WorldBorderEnforcer(mechsModule, this);
        lootLimitManager = new LootLimitManager(mechsModule, this);
        new WorldBorderDynmapHook(mechsModule, this);
        new BorderRegionWarning(mechsModule, this);
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
        PlayerVars playerVars = mechsModule.getPlugin().getPlayerVarsManager().getVars(player.getUniqueId());
        return playerVars.getOrDefault(ACCEPTS_WARNING_KEY, boolean.class, false);
    }

    public void acceptBorderRegionWarning(Player player) {
        PlayerVars playerVars = mechsModule.getPlugin().getPlayerVarsManager().getVars(player.getUniqueId());
        playerVars.put(ACCEPTS_WARNING_KEY, true);
    }

    @EventHandler(ignoreCancelled = true)
    private void onWorldLoad(WorldLoadEvent event) {
        addWorldBorder(event.getWorld());
    }

}
