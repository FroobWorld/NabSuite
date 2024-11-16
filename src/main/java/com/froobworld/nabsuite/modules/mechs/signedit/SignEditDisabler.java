package com.froobworld.nabsuite.modules.mechs.signedit;

import com.froobworld.nabsuite.modules.mechs.MechsModule;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.papermc.paper.event.player.PlayerOpenSignEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.time.Duration;

public class SignEditDisabler implements Listener {
    private final String editSignPerm = "nabsuite.editsigns";
    private final Cache<Location, Location> locationCache = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(5)).build();

    public SignEditDisabler(MechsModule mechsModule) {
        Bukkit.getPluginManager().registerEvents(this, mechsModule.getPlugin());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    private void onSignOpen(PlayerOpenSignEvent event) {
        if (event.getCause() == PlayerOpenSignEvent.Cause.INTERACT) {
            if (locationCache.getIfPresent(event.getSign().getLocation()) == null) {
                if (!event.getPlayer().hasPermission(editSignPerm)) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(Component.text("You do not have permission to edit signs.", NamedTextColor.RED));
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onBlockPlace(BlockPlaceEvent event) {
        locationCache.put(event.getBlock().getLocation(), event.getBlock().getLocation());
    }

}
