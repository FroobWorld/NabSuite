package com.froobworld.nabsuite.modules.mechs.signedit;

import com.froobworld.nabsuite.modules.mechs.MechsModule;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;

import java.time.Duration;

public class SignEditDisabler implements Listener {
    private final Cache<Location, Location> locationCache = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(5)).build();

    public SignEditDisabler(MechsModule mechsModule) {
        Bukkit.getPluginManager().registerEvents(this, mechsModule.getPlugin());
    }

    @EventHandler(ignoreCancelled = true)
    private void onSignChange(SignChangeEvent event) {
        if (locationCache.getIfPresent(event.getBlock().getLocation()) == null) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text("Sign editing is temporarily disabled.", NamedTextColor.RED));
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onBlockPlace(BlockPlaceEvent event) {
        locationCache.put(event.getBlock().getLocation(), event.getBlock().getLocation());
    }

}
