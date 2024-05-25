package com.froobworld.nabsuite.modules.protect.lock;

import io.papermc.paper.event.player.PlayerOpenSignEvent;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.UUID;

public class LockListener implements Listener {
    private final LockManager lockManager;

    public LockListener(LockManager lockManager) {
        this.lockManager = lockManager;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        lockManager.onPlayerInteract(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        lockManager.onBlockBreak(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBurn_Override(BlockBurnEvent event) {
        lockManager.onBlockBurn(event);
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockExplode(BlockExplodeEvent event) {
        lockManager.onBlockExplode(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityExplode_Override(EntityExplodeEvent event) {
        lockManager.onEntityExplode(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onSignInteract(PlayerOpenSignEvent event) {
        if (lockManager.isLockSign(event.getSign())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onSignChange(SignChangeEvent event) {
        lockManager.onSignChange(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            for (Block b : lockManager.getAttachedLockables(block)) {
                if (lockManager.getOwner(b.getLocation(), true) != null) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            for (Block b : lockManager.getAttachedLockables(block)) {
                if (lockManager.getOwner(b.getLocation(), true) != null) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (event.getSource().getHolder(false) instanceof BlockState || event.getSource().getHolder(false) instanceof DoubleChest) {
            if (event.getDestination().getHolder(false) instanceof HopperMinecart) {
                if (lockManager.getOwner(event.getSource().getLocation(), true) != null) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (event.getDestination().getHolder(false) instanceof BlockState) {
                UUID ownerDestination = lockManager.getOwner(event.getDestination().getLocation(), true);
                UUID ownerSource = lockManager.getOwner(event.getSource().getLocation(), true);
                if (ownerSource != null && !ownerSource.equals(ownerDestination)) {
                    event.setCancelled(true);
                    return;
                }
            }

        }
    }

}
