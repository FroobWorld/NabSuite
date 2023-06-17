package com.froobworld.nabsuite.modules.mechs.border;

import com.froobworld.nabsuite.modules.mechs.MechsModule;
import io.papermc.paper.entity.TeleportFlag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class WorldBorderEnforcer implements Listener {
    private static final int KNOCK_BACK_DISTANCE = 5;
    private static final Component KNOCK_BACK_MESSAGE = Component.text("You have reached the border of this world.", NamedTextColor.RED);
    private final WorldBorderManager worldBorderManager;

    public WorldBorderEnforcer(MechsModule mechsModule, WorldBorderManager worldBorderManager) {
        this.worldBorderManager = worldBorderManager;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(mechsModule.getPlugin(), this::checkPlayers, 5, 5);
        Bukkit.getPluginManager().registerEvents(this, mechsModule.getPlugin());
    }

    private Location getSafeLocation(Location location) {
        return location.toHighestLocation().add(0.5, 1, 0.5);
    }

    @SuppressWarnings("UnstableApiUsage")
    private void knockBack(Player player, WorldBorder worldBorder) {
        Location knockBackLocation = getSafeLocation(worldBorder.knockBackLocation(player.getLocation(), KNOCK_BACK_DISTANCE));
        Entity vehicle = player.getVehicle();
        if (vehicle != null) {
            player.leaveVehicle();
            vehicle.teleport(knockBackLocation, TeleportFlag.EntityState.RETAIN_PASSENGERS);
        }
        player.teleport(knockBackLocation);
        player.sendMessage(KNOCK_BACK_MESSAGE);
    }

    private void checkPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            WorldBorder worldBorder = worldBorderManager.getWorldBorder(player.getWorld());
            if (worldBorder != null && !worldBorder.isInBorder(player.getLocation())) {
                knockBack(player, worldBorder);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onPlayerTeleport(PlayerTeleportEvent event) {
        WorldBorder worldBorder = worldBorderManager.getWorldBorder(event.getTo().getWorld());
        if (worldBorder != null && !worldBorder.isInBorder(event.getTo())) {
            if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL || event.getCause() == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(KNOCK_BACK_MESSAGE);
                return;
            }
            event.setTo(getSafeLocation(worldBorder.knockBackLocation(event.getTo(), KNOCK_BACK_DISTANCE)));
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onPlayerPortal(PlayerPortalEvent event) {
        WorldBorder worldBorder = worldBorderManager.getWorldBorder(event.getTo().getWorld());
        if (worldBorder != null && !worldBorder.isInBorder(event.getTo())) {
            event.setTo(getSafeLocation(worldBorder.knockBackLocation(event.getTo(), KNOCK_BACK_DISTANCE)));
        }
    }


}
