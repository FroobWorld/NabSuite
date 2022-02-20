package com.froobworld.nabsuite.modules.mechs.pvp;

import com.froobworld.nabsuite.modules.mechs.MechsModule;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.WeakHashMap;

public class PvpManager {
    private final NamespacedKey pdcKey;
    private final Map<Player, Boolean> pvpEnabledCache = new WeakHashMap<>();

    public PvpManager(MechsModule mechsModule) {
        this.pdcKey = new NamespacedKey(mechsModule.getPlugin(), "pvp-enabled");
        Bukkit.getPluginManager().registerEvents(new PvpEnforcer(this), mechsModule.getPlugin());
    }

    public boolean pvpEnabled(Player player) {
        return pvpEnabledCache.computeIfAbsent(player, p -> p.getPersistentDataContainer().has(pdcKey));
    }

    public void setPvpEanbled(Player player, boolean enabled) {
        if (enabled) {
            player.getPersistentDataContainer().set(pdcKey, PersistentDataType.BYTE, (byte) 1);
        } else {
            player.getPersistentDataContainer().remove(pdcKey);
        }
        pvpEnabledCache.put(player, enabled);
    }

}
