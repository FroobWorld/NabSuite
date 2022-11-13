package com.froobworld.nabsuite.modules.admin.suspicious.monitors;

import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.EnderChest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TheftMonitor implements ActivityMonitor, Listener {
    private static final int SPAWN_DISTANCE_THRESHOLD = 300;
    private static final int DIAMOND_AMOUNT_THRESHOLD = 3;
    private static final double SUSPICION_LEVEL_PER_LOCATION = 0.5;
    private final BasicsModule basicsModule;
    private final Map<UUID, PlayerTheftStats> theftStatsMap = new HashMap<>();

    public TheftMonitor(AdminModule adminModule) {
        this.basicsModule = adminModule.getPlugin().getModule(BasicsModule.class);
        Bukkit.getPluginManager().registerEvents(this, adminModule.getPlugin());
    }

    @Override
    public double getSuspicionLevel(Player player) {
        PlayerTheftStats theftStats = theftStatsMap.get(player.getUniqueId());
        if (theftStats == null) {
            return 0;
        }
        int locationsTakenFrom = 0;
        for (int takings : theftStats.diamondBalance.values()) {
            if (takings >= DIAMOND_AMOUNT_THRESHOLD) {
                locationsTakenFrom += 1;
            }
        }
        return Math.min(2, locationsTakenFrom * SUSPICION_LEVEL_PER_LOCATION);
    }

    private int getInventoryCapacity(Inventory inventory, ItemStack itemStack) {
        int amountFree = 0;
        for (ItemStack slot : inventory.getStorageContents()) {
            if (slot == null) {
                amountFree += itemStack.getMaxStackSize();
            } else {
                if (slot.isSimilar(itemStack)) {
                    amountFree += slot.getMaxStackSize() - slot.getAmount();
                }
            }
        }
        return amountFree;
    }

    private int getAmountContained(Inventory inventory, ItemStack itemStack) {
        int amount = 0;
        for (ItemStack slot : inventory.getStorageContents()) {
            if (slot != null) {
                if (slot.isSimilar(itemStack)) {
                    amount += slot.getAmount();
                }
            }
        }
        return amount;
    }

    @EventHandler(ignoreCancelled = true)
    private void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof EnderChest || event.getInventory().getHolder() instanceof ShulkerBox) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        Location containerLocation = event.getInventory().getLocation();
        Location spawnLocation = basicsModule.getSpawnManager().getSpawnLocation();
        if (containerLocation == null || spawnLocation == null) {
            return;
        }
        if (Math.max(Math.abs(containerLocation.getBlockX() - spawnLocation.getBlockX()), Math.abs(containerLocation.getBlockZ() - spawnLocation.getBlockZ())) > SPAWN_DISTANCE_THRESHOLD) {
            return;
        }

        int diamondsDifferent = 0; // positive for taking, negative for depositing
        if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            if (event.getCursor() != null && event.getCursor().getType() == Material.DIAMOND) {
                diamondsDifferent = Math.min(event.getCursor().getMaxStackSize() - event.getCursor().getAmount(), getAmountContained(event.getInventory(), event.getCursor()));
            }
        } else if (event.getAction() == InventoryAction.SWAP_WITH_CURSOR) {
            if (event.getClickedInventory() == event.getInventory()) {
                if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.DIAMOND) {
                    diamondsDifferent += event.getCurrentItem().getAmount();
                }
                if (event.getCursor() != null && event.getCursor().getType() == Material.DIAMOND) {
                    diamondsDifferent -= event.getCursor().getAmount();
                }
            }
        } else if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.DIAMOND) {
                if (event.getClickedInventory() == event.getView().getTopInventory()) {
                    diamondsDifferent += Math.min(event.getCurrentItem().getAmount(), getInventoryCapacity(event.getView().getBottomInventory(), event.getCurrentItem()));
                } else {
                    diamondsDifferent -= Math.min(event.getCurrentItem().getAmount(), getInventoryCapacity(event.getView().getTopInventory(), event.getCurrentItem()));
                }
            }
        } else if (event.getAction() == InventoryAction.PLACE_ONE) {
            if (event.getClickedInventory() == event.getInventory()) {
                if (event.getCursor() != null && event.getCursor().getType() == Material.DIAMOND) {
                    if (event.getCurrentItem() == null || event.getCurrentItem().getAmount() < event.getCurrentItem().getMaxStackSize()) {
                        diamondsDifferent -= 1;
                    }
                }
            }
        } else if (event.getAction() == InventoryAction.PLACE_SOME) {
            if (event.getClickedInventory() == event.getInventory()) {
                if (event.getCursor() != null && event.getCursor().getType() == Material.DIAMOND) {
                    diamondsDifferent -= Math.min(event.getCursor().getMaxStackSize() - (event.getCurrentItem() == null ? 0 : event.getCurrentItem().getAmount()), event.getCursor().getAmount());
                }
            }
        } else if (event.getAction() == InventoryAction.PLACE_ALL) {
            if (event.getClickedInventory() == event.getInventory()) {
                if (event.getCursor() != null && event.getCursor().getType() == Material.DIAMOND) {
                    diamondsDifferent -= event.getCursor().getAmount();
                }
            }
        } else if (event.getAction() == InventoryAction.PICKUP_ONE) {
            if (event.getClickedInventory() == event.getInventory()) {
                if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.DIAMOND) {
                    diamondsDifferent += 1;
                }
            }
        } else if (event.getAction() == InventoryAction.PICKUP_HALF) {
            if (event.getClickedInventory() == event.getInventory()) {
                if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.DIAMOND) {
                    diamondsDifferent += Math.ceil(event.getCurrentItem().getAmount() / 2.0);
                }
            }
        } else if (event.getAction() == InventoryAction.PICKUP_SOME) {
            if (event.getClickedInventory() == event.getInventory()) {
                if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.DIAMOND) {
                    diamondsDifferent += Math.min(event.getCurrentItem().getMaxStackSize(), event.getCurrentItem().getAmount());
                }
            }
        } else if (event.getAction() == InventoryAction.PICKUP_ALL) {
            if (event.getClickedInventory() == event.getInventory()) {
                if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.DIAMOND) {
                    diamondsDifferent += event.getCurrentItem().getAmount();
                }
            }
        }

        if (diamondsDifferent != 0) {
            theftStatsMap.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerTheftStats()).updateLocation(containerLocation, diamondsDifferent);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onInventoryDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        Location containerLocation = event.getInventory().getLocation();
        Location spawnLocation = basicsModule.getSpawnManager().getSpawnLocation();
        if (containerLocation == null || spawnLocation == null) {
            return;
        }
        if (Math.max(Math.abs(containerLocation.getBlockX() - spawnLocation.getBlockX()), Math.abs(containerLocation.getBlockZ() - spawnLocation.getBlockZ())) > SPAWN_DISTANCE_THRESHOLD) {
            return;
        }
        int diamondsDifferent = 0; // positive for taking, negative for depositing


        for (int slot : event.getRawSlots()) {
            if (slot < event.getView().getTopInventory().getSize()) {
                if (event.getNewItems().containsKey(slot)) {
                    ItemStack itemStack = event.getNewItems().get(slot);
                    if (itemStack.getType() == Material.DIAMOND) {
                        ItemStack oldItemStack = event.getView().getItem(slot);
                        diamondsDifferent -= itemStack.getAmount() - (oldItemStack == null ? 0 : oldItemStack.getAmount());
                    }
                }
            }
        }

        if (diamondsDifferent != 0) {
            theftStatsMap.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerTheftStats()).updateLocation(containerLocation, diamondsDifferent);
        }
    }

    private static class PlayerTheftStats {
        private final Map<Location, Integer> diamondBalance = new HashMap<>();

        public void updateLocation(Location location, int change) {
            diamondBalance.compute(location, (loc, amount) -> (amount == null ? 0 : amount) + change);
        }

        public int getTotalTakings() {
            int total = 0;
            for (int amount : diamondBalance.values()) {
                if (amount > 0) {
                    total += amount;
                }
            }
            return total;
        }

    }

}
