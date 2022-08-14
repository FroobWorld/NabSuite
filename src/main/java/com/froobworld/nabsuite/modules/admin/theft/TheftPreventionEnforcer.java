package com.froobworld.nabsuite.modules.admin.theft;

import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class TheftPreventionEnforcer implements Listener {
    private final AdminModule adminModule;
    private final TheftPreventionManager theftPreventionManager;

    public TheftPreventionEnforcer(AdminModule adminModule, TheftPreventionManager theftPreventionManager) {
        this.adminModule = adminModule;
        this.theftPreventionManager = theftPreventionManager;
    }

    public boolean tryOpenChest(Player player) {
        if (!theftPreventionManager.understandsRules(player)) {
            player.sendMessage(
                    Component.text("Please be aware that stealing is against the rules.", NamedTextColor.RED)
                            .append(Component.newline())
                            .append(Component.newline())
                            .append(Component.text("Do not take from any chests unless given permission to do so.", NamedTextColor.RED))
                            .append(Component.newline())
                            .append(Component.newline())
                            .append(
                                    Component.text("Type ", NamedTextColor.RED)
                                            .append(Component.text("/nostealing", NamedTextColor.GOLD))
                                            .append(Component.text(" to gain access to chests.", NamedTextColor.RED))
                                            .clickEvent(ClickEvent.runCommand("/nostealing")))
                            .append(Component.newline())
            );
            return false;
        }
        return true;
    }

    @EventHandler(ignoreCancelled = true)
    private void onJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPlayedBefore()) {
            if (adminModule.getPlugin().getModule(BasicsModule.class).getPlayerDataManager().getPlayerData(event.getPlayer()).getFirstJoined() == event.getPlayer().getFirstPlayed()) {
                theftPreventionManager.setNotUnderstands(event.getPlayer());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.getClickedBlock() == null) {
            return;
        }
        if (event.getClickedBlock().getType() == Material.CHEST || event.getClickedBlock().getType() == Material.TRAPPED_CHEST || event.getClickedBlock().getType() == Material.BARREL) {
            if (!tryOpenChest(event.getPlayer())) {
                event.setCancelled(true);
            }
        }
    }

}
