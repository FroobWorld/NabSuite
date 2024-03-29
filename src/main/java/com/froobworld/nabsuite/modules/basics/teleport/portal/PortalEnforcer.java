package com.froobworld.nabsuite.modules.basics.teleport.portal;

import com.froobworld.nabsuite.modules.basics.BasicsModule;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PortalEnforcer {
    private final PortalManager portalManager;
    private final Set<UUID> immunePlayers = new HashSet<>();

    public PortalEnforcer(BasicsModule basicsModule, PortalManager portalManager) {
        this.portalManager = portalManager;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(basicsModule.getPlugin(), this::loop, 1, 1);
    }

    private void loop() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean inPortal = false;
            for (Portal portal : portalManager.getPortals()) {
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
            }
            if (!inPortal) {
                immunePlayers.remove(player.getUniqueId());
            }
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
            player.teleport(destination);
            setPortalImmune(player);
        }
    }

    public void setPortalImmune(Player player) {
        immunePlayers.add(player.getUniqueId());
    }

}
