package com.froobworld.nabsuite.modules.mechs.border.loot;

import com.destroystokyo.paper.MaterialSetTag;
import com.destroystokyo.paper.MaterialTags;
import com.froobworld.nabsuite.modules.mechs.MechsModule;
import com.froobworld.nabsuite.modules.mechs.border.WorldBorderManager;
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.loot.Lootable;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class LootLimitManager implements Listener {
    private static final MaterialSetTag LOOTABLE_BLOCKS = new MaterialSetTag(NamespacedKey.fromString("loot_tracker_lootable_blocks"))
            .add(MaterialTags.SPONGES)
            .add(Material.GOLD_BLOCK, Material.DIAMOND_BLOCK)
            .add(Material.BOOKSHELF, Material.CHISELED_BOOKSHELF)
            .add(Material.TNT)
            .add(Material.CRYING_OBSIDIAN)
            .add(Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE, Material.ANCIENT_DEBRIS);
    private static final MaterialSetTag LOOTABLE_EXPLODABLE_BLOCKS = new MaterialSetTag(NamespacedKey.fromString("loot_tracker_lootable_explodable_blocks"))
            .add(Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE);
    private final MechsModule mechsModule;
    private final PlayerLootTracker playerLootTracker;
    private final Map<UUID, Set<Location>> sessionLootCache = new HashMap<>();
    private final Map<UUID, Set<Chunk>> sessionLootChunkCache = new HashMap<>();
    private final WorldBorderManager worldBorderManager;

    public LootLimitManager(MechsModule mechsModule, WorldBorderManager worldBorderManager) {
        this.mechsModule = mechsModule;
        this.worldBorderManager = worldBorderManager;
        playerLootTracker = new PlayerLootTracker(mechsModule);
        Bukkit.getPluginManager().registerEvents(this, mechsModule.getPlugin());
        new LootLimitEnforcer(mechsModule, this);
    }

    public void shutdown() {
        playerLootTracker.shutdown();
    }

    private NamespacedKey placedBlockKey(Location location) {
        return new NamespacedKey(mechsModule.getPlugin(), "placed_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ());
    }

    private NamespacedKey lootChestKey(Location location) {
        return new NamespacedKey(mechsModule.getPlugin(), "loot_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ());
    }

    public boolean isLootBlockType(Block block) {
        return LOOTABLE_BLOCKS.isTagged(block);
    }

    public boolean isExplodableLootBlockType(Block block) {
        return LOOTABLE_EXPLODABLE_BLOCKS.isTagged(block);
    }

    public boolean isLootBlock(Location location) {
        if (!worldBorderManager.isBorderRegion(location)) {
            return false;
        }
        if (!isLootBlockType(location.getBlock())) {
            return false;
        }
        return !location.getChunk().getPersistentDataContainer().has(placedBlockKey(location));
    }

    public boolean isNonExplodableLootBlock(Location location) {
        if (!worldBorderManager.isBorderRegion(location)) {
            return false;
        }
        if (!isLootBlockType(location.getBlock()) || isExplodableLootBlockType(location.getBlock())) {
            return false;
        }
        return !location.getChunk().getPersistentDataContainer().has(placedBlockKey(location));
    }

    public boolean isLootChest(Location location) {
        if (!worldBorderManager.isBorderRegion(location)) {
            return false;
        }
        if (location.getBlock().getState() instanceof Lootable) {
            if (((Lootable) location.getBlock().getState()).hasLootTable()) {
                return true;
            }
            return location.getChunk().getPersistentDataContainer().has(lootChestKey(location));
        }
        return false;
    }

    public boolean isLootItemFrame(ItemFrame itemFrame) {
        if (itemFrame.getWorld().getEnvironment() != World.Environment.THE_END) {
            return false;
        }
        return worldBorderManager.isBorderRegion(itemFrame.getLocation()) && itemFrame.getItem().getType() == Material.ELYTRA;
    }

    public boolean hasLootedPreviously(Player player, Location location) {
        if (sessionLootCache.getOrDefault(player.getUniqueId(), Collections.emptySet()).contains(location)) {
            return false;
        }
        return playerLootTracker.getLootTrackerData(player).hasLooted(location);
    }

    public boolean hasLootedPreviously(Player player, Chunk chunk) {
        if (sessionLootChunkCache.getOrDefault(player.getUniqueId(), Collections.emptySet()).contains(chunk)) {
            return false;
        }
        return playerLootTracker.getLootTrackerData(player).hasLooted(chunk);
    }

    private void markAsLooted(Player player, Location location) {
        LootTrackerData lootTrackerData = playerLootTracker.getLootTrackerData(player);
        if (!lootTrackerData.hasLooted(location)) {
            sessionLootCache.compute(player.getUniqueId(), (key, set) -> {
                if (set == null) {
                    set = new HashSet<>();
                }
                set.add(location);
                return set;
            });
        }
        lootTrackerData.addLocation(location);
    }

    private void markChunkAsLooted(Player player, Chunk chunk) {
        LootTrackerData lootTrackerData = playerLootTracker.getLootTrackerData(player);
        if (!lootTrackerData.hasLooted(chunk)) {
            sessionLootChunkCache.compute(player.getUniqueId(), (key, set) -> {
               if (set == null) {
                   set = new HashSet<>();
               }
               set.add(chunk);
               return set;
            });
        }
        lootTrackerData.addChunk(chunk);
    }

    @EventHandler(ignoreCancelled = true)
    private void onItemFrameChange(PlayerItemFrameChangeEvent event) {
        if (isLootItemFrame(event.getItemFrame())) {
            if (event.getAction() == PlayerItemFrameChangeEvent.ItemFrameChangeAction.REMOVE) {
                Location itemFrameLocation = new Location(
                        event.getItemFrame().getWorld(),
                        event.getItemFrame().getLocation().getBlockX(),
                        event.getItemFrame().getLocation().getBlockY(),
                        event.getItemFrame().getLocation().getBlockZ()
                );
                markAsLooted(event.getPlayer(), itemFrameLocation);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof Lootable)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        if (event.getInventory().getLocation() != null) {
            markAsLooted(player, event.getInventory().getLocation());
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof Lootable)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        if (event.getInventory().getLocation() != null) {
            markAsLooted(player, event.getInventory().getLocation());
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onBlockPlace(BlockPlaceEvent event) {
        if (!worldBorderManager.isBorderRegion(event.getBlock().getLocation())) {
            return;
        }
        if (isLootBlockType(event.getBlock())) {
            event.getBlock().getChunk().getPersistentDataContainer().set(placedBlockKey(event.getBlock().getLocation()), PersistentDataType.BYTE, (byte) 0);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onBlockBreak(BlockBreakEvent event) {
        if (!worldBorderManager.isBorderRegion(event.getBlock().getLocation())) {
            return;
        }
        if (isLootChest(event.getBlock().getLocation()) || isLootBlock(event.getBlock().getLocation())) {
            markAsLooted(event.getPlayer(), event.getBlock().getLocation());
        }
        if (isLootBlock(event.getBlock().getLocation())) {
            markChunkAsLooted(event.getPlayer(), event.getBlock().getChunk());
        }
    }

    @EventHandler
    private void onLootGenerate(LootGenerateEvent event) {
        if (event.getInventoryHolder() instanceof Chest) {
            Location location = ((Chest) event.getInventoryHolder()).getLocation();
            location.getChunk().getPersistentDataContainer().set(lootChestKey(location), PersistentDataType.BYTE, (byte) 1);
        }
    }

}
