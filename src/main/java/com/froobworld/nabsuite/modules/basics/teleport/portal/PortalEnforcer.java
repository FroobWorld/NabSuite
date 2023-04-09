package com.froobworld.nabsuite.modules.basics.teleport.portal;

import com.froobworld.nabsuite.modules.basics.BasicsModule;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PortalEnforcer {
    private final BasicsModule basicsModule;
    private final PortalManager portalManager;
    private final Set<UUID> immunePlayers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public PortalEnforcer(BasicsModule basicsModule, PortalManager portalManager) {
        this.basicsModule = basicsModule;
        this.portalManager = portalManager;
        basicsModule.getPlugin().getHookManager().getSchedulerHook().runRepeatingTask(this::loop, 10, 10);
    }

    private void loop() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            basicsModule.getPlugin().getHookManager().getSchedulerHook().runEntityTaskAsap(() -> {
                boolean inPortal = false;
                for (Portal portal : portalManager.getPortals()) {
                    portal.lock.readLock().lock();
                    try {
                        if (portal.getLink() == null) {
                            continue;
                        }
                        if (!portal.getLocation().getWorld().equals(player.getWorld())) {
                            continue;
                        }
                        if (player.getLocation().distanceSquared(portal.getLocation()) <= portal.getRadius() * portal.getRadius()) {
                            inPortal = true;
                            handleInPortal(player, portal);
                            break;
                        }
                    } finally {
                        portal.lock.readLock().unlock();
                    }
                }
                if (!inPortal) {
                    immunePlayers.remove(player.getUniqueId());
                }
            }, null, player);
        }
    }

    private void handleInPortal(Player player, Portal portal) {
        if (!immunePlayers.contains(player.getUniqueId())) {
            Location destination = portal.getLink().getLocation().clone();
            if (portal.useRelativePosition()) {
                Vector locationDifference = player.getLocation().subtract(portal.getLocation()).toVector();
                destination.add(locationDifference);
                destination.setPitch(player.getLocation().getPitch());
                destination.setYaw(player.getLocation().getYaw());
            }
            setPortalImmune(player); // Set the player immune before teleporting so they are only teleported once (noting async teleport can take multiple ticks)
            player.teleportAsync(destination).thenRun(() -> setPortalImmune(player));
        }
    }

    public void setPortalImmune(Player player) {
        immunePlayers.add(player.getUniqueId());
    }

}
