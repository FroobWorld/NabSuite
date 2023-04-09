package com.froobworld.nabsuite.modules.mechs.pvp;

import com.froobworld.nabsuite.modules.mechs.MechsModule;
import com.google.common.collect.MapMaker;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

public class PvpManager implements Listener {
    private final NamespacedKey pdcKey;
    private final Map<Player, Boolean> pvpEnabledCache = new MapMaker().weakKeys().makeMap();

    public PvpManager(MechsModule mechsModule) {
        this.pdcKey = new NamespacedKey(mechsModule.getPlugin(), "pvp-enabled");
        Bukkit.getPluginManager().registerEvents(new PvpEnforcer(this, mechsModule), mechsModule.getPlugin());
        Bukkit.getPluginManager().registerEvents(this, mechsModule.getPlugin());
    }

    public boolean pvpEnabled(Player player) {
        return pvpEnabledCache.getOrDefault(player, false);
    }

    public void setPvpEnabled(Player player, boolean enabled) {
        if (enabled) {
            player.getPersistentDataContainer().set(pdcKey, PersistentDataType.BYTE, (byte) 1);
        } else {
            player.getPersistentDataContainer().remove(pdcKey);
        }
        pvpEnabledCache.put(player, enabled);
    }

    @EventHandler(ignoreCancelled = true)
    private void onPlayerJoin(PlayerJoinEvent event) {
        pvpEnabledCache.put(event.getPlayer(), event.getPlayer().getPersistentDataContainer().has(pdcKey));
    }

}
