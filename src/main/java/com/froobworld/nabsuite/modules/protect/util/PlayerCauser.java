package com.froobworld.nabsuite.modules.protect.util;

import org.bukkit.entity.*;
import org.bukkit.projectiles.ProjectileSource;

public class PlayerCauser {

    public static Player getPlayerCauser(Entity causer) {
        if (causer instanceof Player) {
            return (Player) causer;
        }
        if (causer instanceof Projectile) {
            ProjectileSource source = ((Projectile) causer).getShooter();
            if (source instanceof Entity) {
                return getPlayerCauser((Entity) source);
            }
        }
        if (causer instanceof Monster) {
            return getPlayerCauser(((Monster) causer).getTarget());
        }
        if (causer instanceof Tameable) {
            AnimalTamer tamer = ((Tameable) causer).getOwner();
            if (tamer instanceof Entity) {
                return getPlayerCauser((Entity) tamer);
            }
        }
        return null;
    }

}
