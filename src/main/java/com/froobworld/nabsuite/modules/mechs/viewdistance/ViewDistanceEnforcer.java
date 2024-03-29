package com.froobworld.nabsuite.modules.mechs.viewdistance;

import com.destroystokyo.paper.ClientOption;
import com.destroystokyo.paper.event.player.PlayerClientOptionsChangeEvent;
import com.froobworld.nabsuite.modules.mechs.MechsModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ViewDistanceEnforcer implements Listener {
    private final MechsModule mechsModule;
    private final ViewDistanceManager viewDistanceManager;
    private final Map<UUID, Integer> reminderTasks = new HashMap<>();
    private final Map<Player, Boolean> seenBeforeMap = new WeakHashMap<>();

    public ViewDistanceEnforcer(MechsModule mechsModule, ViewDistanceManager viewDistanceManager) {
        this.mechsModule = mechsModule;
        this.viewDistanceManager = viewDistanceManager;
    }



    @EventHandler
    private void onOptionsChanged(PlayerClientOptionsChangeEvent event) {
        if (event.hasViewDistanceChanged() || !seenBeforeMap.containsKey(event.getPlayer())) {
            seenBeforeMap.put(event.getPlayer(), true);

            int newViewDistance = event.getViewDistance();

            Integer previousReminderTask = reminderTasks.get(event.getPlayer().getUniqueId());
            if (previousReminderTask != null) {
                Bukkit.getScheduler().cancelTask(previousReminderTask);
            }

            if (event.getPlayer().hasPermission("nabsuite.command.togglevd") && viewDistanceManager.isViewDistanceCapped(event.getPlayer()) && newViewDistance > viewDistanceManager.getMaxViewDistance(event.getPlayer())) {
                int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(mechsModule.getPlugin(), () -> {
                    event.getPlayer().sendMessage(Component.empty());
                    event.getPlayer().sendMessage(
                            Component.text("Your view distance is currently limited to ")
                                    .append(Component.text(viewDistanceManager.getMaxViewDistance(event.getPlayer()), NamedTextColor.YELLOW))
                                    .append(Component.text(" despite it being set to "))
                                    .append(Component.text(newViewDistance, NamedTextColor.YELLOW))
                                    .append(Component.text(" in your client options."))
                                    .color(NamedTextColor.RED)
                    );
                    event.getPlayer().sendMessage(Component.empty());
                    event.getPlayer().sendMessage(
                            Component.text("If you believe your connection is strong enough, you can use ")
                                    .append(Component.text("/togglevd", NamedTextColor.YELLOW))
                                    .append(Component.text(" to disable this limit."))
                                    .color(NamedTextColor.RED)
                    );
                    event.getPlayer().sendMessage(Component.empty());
                }, 30 * 20);
                reminderTasks.put(event.getPlayer().getUniqueId(), taskId);
            }
            try {
                viewDistanceManager.recalcPlayerViewDistance(event.getPlayer(), event.getViewDistance());
            } catch (IllegalStateException e) { // Ugly solution to player sometimes not being attached to a world yet
                AtomicInteger attempts = new AtomicInteger(1);
                Runnable reattemptRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (attempts.getAndIncrement() >= 5 || !event.getPlayer().isOnline()) {
                            return;
                        }
                        try {
                            viewDistanceManager.recalcPlayerViewDistance(event.getPlayer(), event.getPlayer().getClientOption(ClientOption.VIEW_DISTANCE));
                        } catch (IllegalStateException exception) {
                            Bukkit.getScheduler().scheduleSyncDelayedTask(mechsModule.getPlugin(), this, 3);
                        }
                    }
                };

                Bukkit.getScheduler().scheduleSyncDelayedTask(mechsModule.getPlugin(), reattemptRunnable, 3);
            }
        }
    }

    @EventHandler
    private void onWorldChange(PlayerChangedWorldEvent event) {
        viewDistanceManager.recalcPlayerViewDistance(event.getPlayer(), event.getPlayer().getClientViewDistance());
    }

}
