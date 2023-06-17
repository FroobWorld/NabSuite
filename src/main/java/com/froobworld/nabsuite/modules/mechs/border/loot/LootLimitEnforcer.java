package com.froobworld.nabsuite.modules.mechs.border.loot;

import com.froobworld.nabsuite.modules.mechs.MechsModule;
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class LootLimitEnforcer implements Listener {
    private final LootLimitManager lootLimitManager;

    public LootLimitEnforcer(MechsModule mechsModule, LootLimitManager lootLimitManager) {
        this.lootLimitManager = lootLimitManager;
        Bukkit.getPluginManager().registerEvents(this, mechsModule.getPlugin());
    }

    private void sendFailureMessage(Player player) {
        player.sendMessage(
                Component.text("You have already looted this.")
                        .append(Component.newline())
                        .append(Component.text("Structures in the border regions may only be looted once."))
                        .color(NamedTextColor.RED)
        );
    }

    @EventHandler(ignoreCancelled = true)
    private void onHangingBreak(HangingBreakEvent event) {
        if (event.getEntity() instanceof ItemFrame) {
            if (lootLimitManager.isLootItemFrame((ItemFrame) event.getEntity())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        if (event.getEntity() instanceof ItemFrame) {
            if (lootLimitManager.isLootItemFrame((ItemFrame) event.getEntity())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onItemFrameChange(PlayerItemFrameChangeEvent event) {
        if (lootLimitManager.isLootItemFrame(event.getItemFrame())) {
            Location itemFrameLocation = new Location(
                    event.getItemFrame().getWorld(),
                    event.getItemFrame().getLocation().getBlockX(),
                    event.getItemFrame().getLocation().getBlockY(),
                    event.getItemFrame().getLocation().getBlockZ()
            );
            if (lootLimitManager.hasLootedPreviously(event.getPlayer(), itemFrameLocation)) {
                event.setCancelled(true);
                sendFailureMessage(event.getPlayer());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block != null) {
            if (lootLimitManager.isLootChest(block.getLocation()) && lootLimitManager.hasLootedPreviously(event.getPlayer(), block.getLocation())) {
                event.setCancelled(true);
                sendFailureMessage(event.getPlayer());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onBlockBreak(BlockBreakEvent event) {
        if (lootLimitManager.isLootChest(event.getBlock().getLocation()) || lootLimitManager.isLootBlock(event.getBlock().getLocation())) {
            if (lootLimitManager.hasLootedPreviously(event.getPlayer(), event.getBlock().getLocation())) {
                event.setCancelled(true);
                sendFailureMessage(event.getPlayer());
            }
        }
        if (!event.isCancelled() && lootLimitManager.isLootBlock(event.getBlock().getLocation())) {
            if (lootLimitManager.hasLootedPreviously(event.getPlayer(), event.getBlock().getChunk())) {
                event.setCancelled(true);
                sendFailureMessage(event.getPlayer());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> lootLimitManager.isLootChest(block.getLocation()) || lootLimitManager.isLootBlock(block.getLocation()));
    }

    @EventHandler(ignoreCancelled = true)
    private void onBlockExplode(BlockExplodeEvent event) {
        if (lootLimitManager.isLootChest(event.getBlock().getLocation()) || lootLimitManager.isLootBlock(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
        event.blockList().removeIf(block -> lootLimitManager.isLootChest(block.getLocation()) || lootLimitManager.isLootBlock(block.getLocation()));
    }

    @EventHandler(ignoreCancelled = true)
    private void onInventoryMoveItem(InventoryMoveItemEvent event) {
        Location location = event.getSource().getLocation();
        if (location != null && lootLimitManager.isLootChest(location)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onBlockPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (lootLimitManager.isLootBlock(block.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onBlockPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (lootLimitManager.isLootBlock(block.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

}
