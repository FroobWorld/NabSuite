package com.froobworld.nabsuite.modules.protect.util;

import org.bukkit.entity.*;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerCauser {

    private static Player getPlayerCauser(Entity causer, boolean includeTargets, Set<UUID> visitedEntities) {
        if (causer != null) {
            if (visitedEntities.contains(causer.getUniqueId())) {
                return null;
            }
            visitedEntities.add(causer.getUniqueId());
        }
        if (causer instanceof Player) {
            return (Player) causer;
        }
        if (causer instanceof Projectile) {
            ProjectileSource source = ((Projectile) causer).getShooter();
            if (source instanceof Entity) {
                return getPlayerCauser((Entity) source, includeTargets, visitedEntities);
            }
        }
        if (includeTargets && causer instanceof Monster) {
            return getPlayerCauser(((Monster) causer).getTarget(), includeTargets, visitedEntities);
        }
        if (causer instanceof Tameable) {
            AnimalTamer tamer = ((Tameable) causer).getOwner();
            if (tamer instanceof Entity) {
                return getPlayerCauser((Entity) tamer, includeTargets, visitedEntities);
            }
        }
        return null;
    }

    public static Player getPlayerCauser(Entity causer, boolean includeTargets) {
        return getPlayerCauser(causer, includeTargets, new HashSet<>());
    }

    public static Player getPlayerCauser(Entity causer) {
        return getPlayerCauser(causer, true);
    }

}
