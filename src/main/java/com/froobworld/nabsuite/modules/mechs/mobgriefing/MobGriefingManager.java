package com.froobworld.nabsuite.modules.mechs.mobgriefing;

import com.froobworld.nabsuite.modules.mechs.MechsModule;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class MobGriefingManager implements Listener {

    public MobGriefingManager(MechsModule mechsModule) {
        Bukkit.getPluginManager().registerEvents(this, mechsModule.getPlugin());
    }

    @EventHandler
    private void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.getEntity().getType() == EntityType.ENDERMAN) {
            event.setCancelled(true);
        }
    }

}
