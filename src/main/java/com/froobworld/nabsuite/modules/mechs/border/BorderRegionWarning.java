package com.froobworld.nabsuite.modules.mechs.border;

import com.froobworld.nabsuite.modules.mechs.MechsModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Map;
import java.util.WeakHashMap;

public class BorderRegionWarning implements Listener {
    private static final int REMINDER_PERIOD = 1200 * 10; // 10 minutes
    private static final int INITIAL_DELAY = REMINDER_PERIOD + 1200;
    private final WorldBorderManager worldBorderManager;
    private final Map<Player, Boolean> lastStatus = new WeakHashMap<>();

    public BorderRegionWarning(MechsModule mechsModule, WorldBorderManager worldBorderManager) {
        this.worldBorderManager = worldBorderManager;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(mechsModule.getPlugin(), this::sendReminders, REMINDER_PERIOD, INITIAL_DELAY);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(mechsModule.getPlugin(), this::sendEntryWarning, 40, 40);
        Bukkit.getPluginManager().registerEvents(this, mechsModule.getPlugin());
    }

    private void sendEntryWarning() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            lastStatus.compute(player, (p, oldStatus) -> {
                WorldBorder worldBorder = worldBorderManager.getWorldBorder(player.getWorld());
                boolean newStatus = worldBorder != null && worldBorder.isInBorderRegion(player.getLocation());
                if (newStatus && (oldStatus == null || !oldStatus)) {
                    player.sendMessage(
                            Component.newline()
                                    .append(Component.text("You have entered the border region of this world."))
                                    .append(Component.newline())
                                    .append(Component.newline())
                                    .append(Component.text("This area may be reset without notice. Do not place anything you are not willing to lose."))
                                    .append(Component.newline())
                                    .color(NamedTextColor.RED)
                    );
                } else if (oldStatus != null && oldStatus && !newStatus) {
                    player.sendMessage(Component.text("You have left the border region.", NamedTextColor.RED));
                }
                return newStatus;
            });
        }
    }

    private void sendReminders() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            WorldBorder worldBorder = worldBorderManager.getWorldBorder(player.getWorld());
            if (worldBorder != null && worldBorder.isInBorderRegion(player.getLocation())) {
                player.sendMessage(Component.empty());
                player.sendMessage(Component.text("Please note: the area of the world you are in may be reset without notice.", NamedTextColor.RED));
                player.sendMessage(Component.empty());
                player.sendMessage(Component.text("Do not place anything you are not willing to lose.", NamedTextColor.RED));
                player.sendMessage(Component.empty());
            }
        }
    }

    private void sendPreliminaryWarning(Player player) {
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("You are in the border region.", NamedTextColor.RED));
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("This area resets frequently. Anything placed here will be lost.", NamedTextColor.RED));
        player.sendMessage(Component.empty());
        player.sendMessage(
                Component.text("If you understand and wish to continue, type ")
                        .append(Component.text("/borderwarning", NamedTextColor.GOLD))
                        .append(Component.text(".")).color(NamedTextColor.RED)
        );
        player.sendMessage(Component.empty());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    private void onBlockBreak(BlockBreakEvent event) {
        WorldBorder worldBorder = worldBorderManager.getWorldBorder(event.getBlock().getWorld());
        if (worldBorder != null && worldBorder.isInBorderRegion(event.getBlock().getLocation())) {
            if (!worldBorderManager.acceptedBorderRegionWarning(event.getPlayer())) {
                sendPreliminaryWarning(event.getPlayer());
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    private void onBlockPlace(BlockPlaceEvent event) {
        WorldBorder worldBorder = worldBorderManager.getWorldBorder(event.getBlock().getWorld());
        if (worldBorder != null && worldBorder.isInBorderRegion(event.getBlock().getLocation())) {
            if (!worldBorderManager.acceptedBorderRegionWarning(event.getPlayer())) {
                sendPreliminaryWarning(event.getPlayer());
                event.setCancelled(true);
            }
        }
    }

}
