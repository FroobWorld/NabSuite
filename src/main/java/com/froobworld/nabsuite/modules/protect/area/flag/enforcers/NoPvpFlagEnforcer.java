package com.froobworld.nabsuite.modules.protect.area.flag.enforcers;

import com.froobworld.nabsuite.modules.protect.area.AreaLike;
import com.froobworld.nabsuite.modules.protect.area.AreaManager;
import com.froobworld.nabsuite.modules.protect.area.flag.Flags;
import com.froobworld.nabsuite.modules.protect.util.PlayerCauser;
import com.google.common.collect.Sets;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class NoPvpFlagEnforcer implements Listener {
    private static final Component PVP_DISABLED_MESSAGE = Component.text("PvP is disabled in this area.").color(NamedTextColor.RED);
    private final AreaManager areaManager;

    public NoPvpFlagEnforcer(AreaManager areaManager) {
        this.areaManager = areaManager;
    }

    private boolean canPvp(Player victim, Player attacker, boolean informOnFail) {
        for (AreaLike area : Sets.union(areaManager.getTopMostAreasAtLocation(victim.getLocation()), areaManager.getTopMostAreasAtLocation(attacker.getLocation()))) {
            if (area.hasFlag(Flags.NO_PVP)) {
                if (informOnFail) {
                    attacker.sendMessage(PVP_DISABLED_MESSAGE);
                }
                return false;
            }
        }
        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Player attacker = PlayerCauser.getPlayerCauser(event.getDamager());
            if (attacker == null) {
                return;
            }
            if (!canPvp(victim, attacker, true)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerCombust(EntityCombustByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Player attacker = PlayerCauser.getPlayerCauser(event.getCombuster());
            if (attacker == null) {
                return;
            }
            if (!canPvp(victim, attacker, true)) {
                event.setCancelled(true);
            }
        }
    }

}
