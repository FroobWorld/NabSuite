package com.froobworld.nabsuite.modules.mechs.pvp;

import com.froobworld.nabsuite.modules.protect.util.PlayerCauser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PvpEnforcer implements Listener {
    public final PvpManager pvpManager;

    public PvpEnforcer(PvpManager pvpManager) {
        this.pvpManager = pvpManager;
    }

    private boolean tryPvp(Player damager, Player victim, boolean informOnFail) {
        if (!pvpManager.pvpEnabled(damager)) {
            if (informOnFail) {
                damager.sendMessage(Component.text("You have PvP disabled.", NamedTextColor.RED));
            }
            return false;
        } else if (!pvpManager.pvpEnabled(victim)) {
            if (informOnFail) {
                damager.sendMessage(Component.text("That player has PvP disabled.", NamedTextColor.RED));
            }
            return false;
        }
        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Player damager = PlayerCauser.getPlayerCauser(event.getDamager());
            if (damager != null) {
                if (!tryPvp(damager, victim, true));
            }
        }
    }

}
