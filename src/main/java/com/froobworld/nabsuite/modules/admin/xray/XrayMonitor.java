package com.froobworld.nabsuite.modules.admin.xray;

import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.util.OreUtils;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerKickEvent;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class XrayMonitor implements Listener {
    private static final String XRAYER_GROUP = "xrayer";
    private static final long COLLECTION_PERIOD = TimeUnit.MINUTES.toMillis(4);
    private static final int MINIMUM_VEIN_COUNT = 5;
    private static final int MINIMUM_VEIN_SIZE = 3;
    private final AdminModule adminModule;
    private final VeinTracker diamondVeinTracker;
    private final Map<Player, PlayerTracker> playerTrackers = new WeakHashMap<>();

    public XrayMonitor(AdminModule adminModule) {
        this.adminModule = adminModule;
        this.diamondVeinTracker = new VeinTracker();
        Bukkit.getPluginManager().registerEvents(this, adminModule.getPlugin());
    }

    @EventHandler(ignoreCancelled = true)
    private void onBlockBreak(BlockBreakEvent event) {
        if (OreUtils.isDiamondOre(event.getBlock().getType())) {
            if (diamondVeinTracker.addLocation(event.getBlock().getLocation(), OreUtils::isDiamondOre) >= MINIMUM_VEIN_SIZE) {
                PlayerTracker playerTracker = playerTrackers.computeIfAbsent(event.getPlayer(), player -> new PlayerTracker());
                playerTracker.foundVein();
                if (playerTracker.suspicious()) {
                    markAsXrayer(event.getPlayer()).thenAcceptAsync(marked -> {
                        if (marked) {
                            adminModule.getTicketManager().createSystemTicket(
                                    event.getPlayer().getLocation(),
                                    "Player " + event.getPlayer().getName() + " has suspicious mining activity. Please investigate if they have been x-raying." +
                                            " They may have just been cave mining or gotten lucky."
                                    );
                            Bukkit.getScheduler().scheduleSyncDelayedTask(adminModule.getPlugin(), () -> {
                                event.getPlayer().kick(Component.text("Timed out"), PlayerKickEvent.Cause.TIMEOUT);
                            }, 100);
                        }
                    });
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onBlockPlace(BlockPlaceEvent event) {
        if (OreUtils.isDiamondOre(event.getBlock().getType())) {
            diamondVeinTracker.playerPlaced(event.getBlock().getLocation());
        }
    }

    public CompletableFuture<Boolean> markAsXrayer(Player player) {
        LuckPerms luckPerms = adminModule.getPlugin().getHookManager().getLuckPermsHook().getLuckPerms();
        if (luckPerms != null) {
            return isMarkedAsXrayer(player).thenApplyAsync(marked -> {
                if (marked) {
                    return false;
                }
                User user = luckPerms.getUserManager().getUser(player.getUniqueId());
                if (user == null) {
                    return false;
                }
                InheritanceNode groupNode = InheritanceNode.builder().group(XRAYER_GROUP).expiry(7, TimeUnit.DAYS).build();
                user.data().add(groupNode);
                luckPerms.getUserManager().saveUser(user).join();
                return true;
            });

        }
        return CompletableFuture.completedFuture(false);
    }

    public CompletableFuture<Boolean> isMarkedAsXrayer(Player player) {
        LuckPerms luckPerms = adminModule.getPlugin().getHookManager().getLuckPermsHook().getLuckPerms();
        if (luckPerms != null) {
            return CompletableFuture.supplyAsync(() -> {
                User user = luckPerms.getUserManager().getUser(player.getUniqueId());
                if (user == null) {
                    return false;
                }
                return user.getCachedData().getPermissionData().checkPermission("group." + XRAYER_GROUP).asBoolean();
            });
        }
        return CompletableFuture.completedFuture(false);
    }

    private static class PlayerTracker {
        private final Queue<Long> timestamps;

        public PlayerTracker() {
            timestamps = new ArrayDeque<>();
        }

        public void foundVein() {
            if (timestamps.size() >= XrayMonitor.MINIMUM_VEIN_COUNT) {
                timestamps.remove();
            }
            timestamps.add(System.currentTimeMillis());
        }

        public boolean suspicious() {
            if (timestamps.size() < XrayMonitor.MINIMUM_VEIN_COUNT) {
                return false;
            }
            for (long timestamp : timestamps) {
                if (System.currentTimeMillis() - timestamp > XrayMonitor.COLLECTION_PERIOD) {
                    return false;
                }
            }
            return true;
        }

    }

}
