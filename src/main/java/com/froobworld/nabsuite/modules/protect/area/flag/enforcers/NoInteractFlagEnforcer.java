package com.froobworld.nabsuite.modules.protect.area.flag.enforcers;

import com.froobworld.nabsuite.modules.protect.area.Area;
import com.froobworld.nabsuite.modules.protect.area.AreaManager;
import com.froobworld.nabsuite.modules.protect.area.flag.Flags;
import com.froobworld.nabsuite.modules.protect.util.PlayerCauser;
import org.bukkit.Location;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;

public class NoInteractFlagEnforcer implements Listener {
    private final AreaManager areaManager;

    public NoInteractFlagEnforcer(AreaManager areaManager) {
        this.areaManager = areaManager;
    }

    private boolean canInteract(Location location, Player player, boolean informOnFail) {
        for (Area area : areaManager.getTopMostAreasAtLocation(location)) {
            if (area.hasFlag(Flags.NO_INTERACT) && !area.hasUserRights(player)) {
                if (informOnFail) {
                    player.sendMessage(AreaManager.AREA_PROTECTED_MESSAGE);
                }
                return false;
            }
        }
        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_AIR) {
            return;
        }
        if (event.getClickedBlock() == null) {
            return;
        }
        if (!canInteract(event.getClickedBlock().getLocation(), event.getPlayer(), event.getAction() != Action.PHYSICAL)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (!canInteract(event.getRightClicked().getLocation(), event.getPlayer(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (!canInteract(event.getRightClicked().getLocation(), event.getPlayer(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerLeashEntity(PlayerLeashEntityEvent event) {
        if (!canInteract(event.getEntity().getLocation(), event.getPlayer(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerUnleashEntity(PlayerUnleashEntityEvent event) {
        if (!canInteract(event.getEntity().getLocation(), event.getPlayer(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerShearEntity(PlayerShearEntityEvent event) {
        if (!canInteract(event.getEntity().getLocation(), event.getPlayer(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onCombustEntity(EntityCombustByEntityEvent event) {
        Player causer = PlayerCauser.getPlayerCauser(event.getCombuster());
        if (causer == null) {
            return;
        }
        if (!canInteract(event.getEntity().getLocation(), causer, true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Player causer = PlayerCauser.getPlayerCauser(event.getDamager());
        if (causer == null) {
            return;
        }
        if (event.getEntity() instanceof Player) {
            return;
        }
        if (event.getEntity() instanceof Monster && event.getEntity().customName() == null && causer.equals(((Monster) event.getEntity()).getTarget())) {
            return; // Allow the killing of un-named monsters that are attacking the player
        }
        if (!canInteract(event.getEntity().getLocation(), causer, true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onVehicleDamage(VehicleDamageEvent event) {
        Player causer = PlayerCauser.getPlayerCauser(event.getAttacker());
        if (causer == null) {
            return;
        }
        if (!canInteract(event.getVehicle().getLocation(), causer, true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onVehicleEnter(VehicleEnterEvent event) {
        Player causer = PlayerCauser.getPlayerCauser(event.getEntered());
        if (causer == null) {
            return;
        }
        if (!canInteract(event.getVehicle().getLocation(), causer, true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onArmourStandManipulate(PlayerArmorStandManipulateEvent event) {
        if (!canInteract(event.getRightClicked().getLocation(), event.getPlayer(), true)) {
            event.setCancelled(true);
        }
    }


}
