package com.froobworld.nabsuite.modules.protect.vehicle;

import com.froobworld.nabsuite.modules.protect.ProtectModule;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.spigotmc.event.entity.EntityMountEvent;

public class VehicleTracker implements Listener {
    private final NamespacedKey pdcKey;

    public VehicleTracker(ProtectModule protectModule) {
        pdcKey = new NamespacedKey(protectModule.getPlugin(), "vehicle-user-data");
        Bukkit.getPluginManager().registerEvents(this, protectModule.getPlugin());
    }

    public boolean hasAccessed(Entity entity, Player player) {
        if (entity instanceof Tameable && player.getUniqueId().equals(((Tameable) entity).getOwnerUniqueId())) {
            return true;
        }
        VehicleUserData vehicleUserData = entity.getPersistentDataContainer().get(pdcKey, VehicleUserData.DATA_TYPE);
        return vehicleUserData != null && vehicleUserData.isUser(player);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onEntityMount(EntityMountEvent event) {
        if (event.getEntity() instanceof Player) {
            VehicleUserData vehicleUserData = event.getMount().getPersistentDataContainer().get(pdcKey, VehicleUserData.DATA_TYPE);
            if (vehicleUserData == null) {
                vehicleUserData = new VehicleUserData();
            }
            vehicleUserData.addUser((Player) event.getEntity());
            event.getMount().getPersistentDataContainer().set(pdcKey, VehicleUserData.DATA_TYPE, vehicleUserData);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onVehicleEnter(VehicleEnterEvent event) {
        if (event.getEntered() instanceof Player) {
            VehicleUserData vehicleUserData = event.getVehicle().getPersistentDataContainer().get(pdcKey, VehicleUserData.DATA_TYPE);
            if (vehicleUserData == null) {
                vehicleUserData = new VehicleUserData();
            }
            vehicleUserData.addUser((Player) event.getEntered());
            event.getVehicle().getPersistentDataContainer().set(pdcKey, VehicleUserData.DATA_TYPE, vehicleUserData);
        }
    }


}
