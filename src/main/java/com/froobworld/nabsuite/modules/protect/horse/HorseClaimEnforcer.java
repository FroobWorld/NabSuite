package com.froobworld.nabsuite.modules.protect.horse;

import com.froobworld.nabsuite.modules.protect.util.PlayerCauser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.spigotmc.event.entity.EntityMountEvent;

public class HorseClaimEnforcer implements Listener {
    private final HorseManager horseManager;

    public HorseClaimEnforcer(HorseManager horseManager) {
        this.horseManager = horseManager;
    }

    private boolean canInteract(Player player, Entity entity, boolean informOnFail) {
        Horse horse = horseManager.getHorse(entity.getUniqueId());
        if (horse != null) {
            if (!horse.hasUserRights(player) && !player.hasPermission(HorseManager.EDIT_ALL_HORSES_PERMISSION)) {
                if (informOnFail) {
                    player.sendMessage(HorseManager.HORSE_PROTECTED_MESSAGE);
                }
                return false;
            }
        }
        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    private void onEntityMount(EntityMountEvent event) {
        if (event.getEntity() instanceof Player) {
            if (!canInteract(((Player) event.getEntity()).getPlayer(), event.getMount(), true)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    private void onEntityDamage(EntityDamageByEntityEvent event) {
        Player causer = PlayerCauser.getPlayerCauser(event.getDamager());
        if (causer == null) {
            return;
        }
        if (!canInteract(causer, event.getEntity(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    private void onInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (!canInteract(event.getPlayer(), event.getRightClicked(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    private void onInteractEntity(PlayerInteractEntityEvent event) {
        if (!canInteract(event.getPlayer(), event.getRightClicked(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    private void onLeashEntity(PlayerLeashEntityEvent event) {
        if (!canInteract(event.getPlayer(), event.getEntity(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    private void onUnleashEntity(PlayerUnleashEntityEvent event) {
        if (!canInteract(event.getPlayer(), event.getEntity(), true)) {
            event.setCancelled(true);
        }
    }

}
