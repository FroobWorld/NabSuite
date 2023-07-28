package com.froobworld.nabsuite.modules.admin.inventory;

import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.nabmode.NabModeModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.Inventory;
import org.joor.Reflect;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InvSeeManager implements Listener {
    private final AdminModule adminModule;
    private final Map<Player, UUID> observingMap = new HashMap<>();

    public InvSeeManager(AdminModule adminModule) {
        this.adminModule = adminModule;
        Bukkit.getPluginManager().registerEvents(this, adminModule.getPlugin());
    }

    private boolean isFakeInventory(Inventory inventory) {
        if (inventory == null) {
            return false;
        }
        return inventory.getClass().getCanonicalName().toLowerCase().contains("invsee");
    }

    private UUID getFakeTarget(Inventory inventory) {
        return Reflect.on(inventory).call("getInventory").get("targetPlayerUuid");
    }

    @EventHandler
    private void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getPlayer();
        if (isFakeInventory(event.getInventory())) {
            UUID observing = getFakeTarget(event.getInventory());
            if (observing.toString().equalsIgnoreCase("ea74c9fd-30a1-46f5-8245-85c88c338890")) {
                event.setCancelled(true);
                player.sendMessage(Component.text("You are a nab.", NamedTextColor.RED));
                return;
            }
            if (player.getUniqueId().equals(observing)) {
                event.setCancelled(true);
                player.sendMessage(Component.text("You can't look at your own inventory.", NamedTextColor.RED));
                return;
            }
            NabModeModule nabModeModule = adminModule.getPlugin().getModule(NabModeModule.class);
            if (nabModeModule != null) {
                if (player.getWorld().equals(nabModeModule.getNabModeManager().getNabDimensionManager().getNabWorld())) {
                    event.setCancelled(true);
                    player.sendMessage(Component.text("You can't look in someone's inventory from this world.", NamedTextColor.RED));
                    return;
                }
                Player observedPlayer = Bukkit.getPlayer(observing);
                if (observedPlayer != null) {
                    if (observedPlayer.getWorld().equals(nabModeModule.getNabModeManager().getNabDimensionManager().getNabWorld())) {
                        event.setCancelled(true);
                        player.sendMessage(Component.text("That player is in a non-observable world.", NamedTextColor.RED));
                        return;
                    }
                }
            }
            observingMap.put(player, observing);
        }
    }

    @EventHandler
    private void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getPlayer();
        if (isFakeInventory(event.getInventory())) {
            observingMap.remove(player);
        }
    }

    @EventHandler
    private void onChangeWorld(PlayerChangedWorldEvent event) {
        NabModeModule nabModeModule = adminModule.getPlugin().getModule(NabModeModule.class);
        if (nabModeModule != null) {
            if (event.getPlayer().getWorld().equals(nabModeModule.getNabModeManager().getNabDimensionManager().getNabWorld())) {
                observingMap.entrySet().removeIf(entry -> {
                   if (entry.getValue().equals(event.getPlayer().getUniqueId())) {
                       entry.getKey().closeInventory();
                       return true;
                   }
                   return false;
                });
            }
        }
    }

}
