package com.froobworld.nabsuite.modules.admin.greylist;

import com.froobworld.nabsuite.modules.admin.AdminModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class GreylistEnforcer implements Listener {
    private final AdminModule adminModule;
    private final GreylistManager greylistManager;

    public GreylistEnforcer(AdminModule adminModule, GreylistManager greylistManager) {
        this.adminModule = adminModule;
        this.greylistManager = greylistManager;
        Bukkit.getPluginManager().registerEvents(this, adminModule.getPlugin());
    }

    public void setGreylisted(UUID player, boolean greylisted) {
        PlayerGreylistData data = greylistManager.getGreylistData(player);
        data.setGreylisted(greylisted);
        Player onlinePlayer = Bukkit.getPlayer(player);
        if (onlinePlayer != null) {
            if (data.isGreylisted()) {
                if (data.hasRequestedRemoval()) {
                    informOfDenial(onlinePlayer);
                } else {
                    informOfGreylisting(onlinePlayer);
                }
            } else {
                informOfRemoval(onlinePlayer);
            }
        } else if (!data.isGreylisted()) {
             data.setInformedOfRemoval(false);
        }
        data.setRequestedRemoval(false);
    }

    public void applyForRemoval(Player player) {
        PlayerGreylistData data = greylistManager.getGreylistData(player.getUniqueId());
        data.setRequestedRemoval(true);
    }

    public boolean tryGreylisted(Player player, boolean informOnFailure) {
        PlayerGreylistData data = greylistManager.getGreylistData(player.getUniqueId());
        if (data.isGreylisted()) {
            if (informOnFailure) {
                Component message = Component.text("You are on the grey list and unable to interact with the world.");
                if (data.hasRequestedRemoval()) {
                    message = message.append(Component.newline())
                            .append(Component.text("Your application to be removed from the grey list will be actioned by the next available staff member."))
                            .color(NamedTextColor.RED);
                } else {
                    message = message.append(Component.newline())
                            .append(Component.text("Read the "))
                            .append(
                                    Component.text("/rules")
                                            .clickEvent(ClickEvent.runCommand("/rules"))
                                            .color(NamedTextColor.GRAY)
                            )
                            .append(Component.text(" to learn how to be removed from the grey list."))
                            .color(NamedTextColor.RED);
                }
                player.sendMessage(message);
            }
            return true;
        }
        return false;
    }

    private void informOfRemoval(Player player) {
        player.sendMessage(
                Component.text("You have been removed from the grey list.")
                .append(Component.newline())
                .append(Component.text("You are now able to interact with the world."))
                .color(NamedTextColor.YELLOW)
        );
    }

    private void informOfDenial(Player player) {
        player.sendMessage(
                Component.text("Your application to be removed from the greylist was denied.")
                        .color(NamedTextColor.YELLOW)
        );
    }

    private void informOfGreylisting(Player player) {
        player.sendMessage(
                Component.text("You have been added to the grey list.")
                        .append(Component.newline())
                        .append(Component.text("You are no longer able to interact with the world"))
                        .color(NamedTextColor.YELLOW)
        );
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        PlayerGreylistData data = greylistManager.getGreylistData(event.getPlayer().getUniqueId());
        if (!data.isInformedOfRemoval()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(adminModule.getPlugin(), () -> {
                if (event.getPlayer().isOnline()) {
                    data.setInformedOfRemoval(false);
                    if (data.isGreylisted()) {
                        informOfDenial(event.getPlayer());
                    } else {
                        informOfRemoval(event.getPlayer());
                    }
                }
            }, 20);
        }
    }

    @EventHandler
    private void onBlockBreak(BlockBreakEvent event) {
        if (tryGreylisted(event.getPlayer(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onBlockPlace(BlockPlaceEvent event) {
        if (tryGreylisted(event.getPlayer(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_AIR) {
            return;
        }
        if (tryGreylisted(event.getPlayer(), event.getAction() != Action.PHYSICAL)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onFoodChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        if (tryGreylisted((Player) event.getEntity(), false)) {
            if (event.getFoodLevel() < event.getEntity().getFoodLevel()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    private void onInteractEntity(PlayerInteractEntityEvent event) {
        if (tryGreylisted(event.getPlayer(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (tryGreylisted(event.getPlayer(), false)) {
            event.setCancelled(true);
        }
    }

}
