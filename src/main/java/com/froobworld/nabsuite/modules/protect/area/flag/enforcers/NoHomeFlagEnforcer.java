package com.froobworld.nabsuite.modules.protect.area.flag.enforcers;

import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent;
import com.froobworld.nabsuite.modules.basics.event.CreateHomeEvent;
import com.froobworld.nabsuite.modules.basics.event.TeleportHomeEvent;
import com.froobworld.nabsuite.modules.protect.area.AreaLike;
import com.froobworld.nabsuite.modules.protect.area.AreaManager;
import com.froobworld.nabsuite.modules.protect.area.flag.Flags;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class NoHomeFlagEnforcer implements Listener {
    private final AreaManager areaManager;

    public NoHomeFlagEnforcer(AreaManager areaManager) {
        this.areaManager = areaManager;
    }

    private boolean canUseHome(Location location, Player player) {
        for (AreaLike area : areaManager.getTopMostAreasAtLocation(location)) {
            if (area.hasFlag(Flags.NO_HOME) && !area.hasUserRights(player)) {
                return false;
            }
        }
        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onCreateHome(CreateHomeEvent event) {
        if (!canUseHome(event.getLocation(), event.getPlayer())) {
            event.getPlayer().sendMessage(
                    Component.text("This area does not allow setting homes.").color(NamedTextColor.RED)
            );
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onTeleportHome(TeleportHomeEvent event) {
        if (!canUseHome(event.getHome().getLocation(), event.getPlayer())) {
            event.getPlayer().sendMessage(
                    Component.text("Destination area does not allow teleporting.").color(NamedTextColor.RED)
            );
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerSetSpawn(PlayerSetSpawnEvent event) {
        if (event.getCause() == PlayerSetSpawnEvent.Cause.BED || event.getCause() == PlayerSetSpawnEvent.Cause.RESPAWN_ANCHOR) {
            if (!canUseHome(event.getLocation(), event.getPlayer())) {
                areaManager.getAreaNotificationManager().notifyProtected(event.getPlayer(), Component.text("This area does not allow respawn points.").color(NamedTextColor.RED));
                event.setCancelled(true);
            }
        }
    }


}
