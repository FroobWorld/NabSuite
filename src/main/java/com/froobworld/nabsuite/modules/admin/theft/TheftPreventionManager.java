package com.froobworld.nabsuite.modules.admin.theft;

import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.google.common.collect.MapMaker;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

public class TheftPreventionManager implements Listener {
    private final NamespacedKey pdcKey;
    private final Map<Player, Boolean> understandsCache = new MapMaker().weakKeys().makeMap();

    public TheftPreventionManager(AdminModule adminModule) {
        this.pdcKey = NamespacedKey.fromString("theft-rule-accepted", adminModule.getPlugin());
        Bukkit.getPluginManager().registerEvents(new TheftPreventionEnforcer(adminModule, this), adminModule.getPlugin());
    }

    public boolean understandsRules(Player player) {
        return understandsCache.computeIfAbsent(player, p -> !p.getPersistentDataContainer().has(pdcKey));
    }

    void setNotUnderstands(Player player) {
        player.getPersistentDataContainer().set(pdcKey, PersistentDataType.BYTE, (byte) 1);
        understandsCache.put(player, false);
    }

    public void setUnderstands(Player player) {
        player.getPersistentDataContainer().remove(pdcKey);
        understandsCache.put(player, true);
    }

}
