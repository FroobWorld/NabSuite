package com.froobworld.nabsuite.modules.mechs.pvp;

import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.mechs.MechsModule;
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
    private final MechsModule mechsModule;

    public PvpEnforcer(PvpManager pvpManager, MechsModule mechsModule) {
        this.pvpManager = pvpManager;
        this.mechsModule = mechsModule;
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
        BasicsModule basicsModule = mechsModule.getPlugin().getModule(BasicsModule.class);
        if (basicsModule != null) {
            if (basicsModule.getAfkManager().isAfk(victim)) {
                if (informOnFail) {
                    damager.sendMessage(Component.text("You can't attack a player while they are AFK.", NamedTextColor.RED));
                }
                return false;
            } else if (basicsModule.getAfkManager().isAfk(damager)) {
                if (informOnFail) {
                    damager.sendMessage(Component.text("You can't attack another player while you are AFK.", NamedTextColor.RED));
                }
                return false;
            }
        }
        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Player damager = PlayerCauser.getPlayerCauser(event.getDamager(), false);
            if (damager != null && !damager.equals(victim)) {
                if (!tryPvp(damager, victim, true)) {
                    event.setCancelled(true);
                }
            }
        }
    }

}
