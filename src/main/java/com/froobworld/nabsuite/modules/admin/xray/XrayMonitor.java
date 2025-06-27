package com.froobworld.nabsuite.modules.admin.xray;

import com.froobworld.nabsuite.data.playervar.PlayerVars;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.util.OreUtils;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class XrayMonitor implements Listener {
    private static final String XRAYER_GROUP = "xrayer";
    private static final long COLLECTION_PERIOD = TimeUnit.MINUTES.toMillis(4);
    private static final int MINIMUM_VEIN_COUNT = 5;
    private static final int MINIMUM_VEIN_SIZE = 3;
    private static final String XRAY_RULE_ACCEPTED = "xray-rule-accepted";
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
            if (!understandsRules(event.getPlayer())) {
                sendFirstDiamondWarning(event.getPlayer());
                event.setCancelled(true);
                return;
            }
            if (diamondVeinTracker.addLocation(event.getBlock().getLocation(), OreUtils::isDiamondOre) >= MINIMUM_VEIN_SIZE) {
                PlayerTracker playerTracker = playerTrackers.computeIfAbsent(event.getPlayer(), player -> new PlayerTracker());
                playerTracker.foundVein();
                if (playerTracker.suspicious()) {
                    markAsXrayer(event.getPlayer().getUniqueId(), TimeUnit.DAYS.toMillis(7)).thenAcceptAsync(marked -> {
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

    @EventHandler(ignoreCancelled = true)
    private void onJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPlayedBefore()) {
            // don't show warning to returning players
            if (adminModule.getPlugin().getModule(BasicsModule.class).getPlayerDataManager().getPlayerData(event.getPlayer()).getFirstJoined() != event.getPlayer().getFirstPlayed()) {
                setUnderstandsRules(event.getPlayer());
            }
        }
    }

    public boolean understandsRules(Player player) {
        PlayerVars playerVars = adminModule.getPlugin().getPlayerVarsManager().getVars(player.getUniqueId());
        return playerVars.getOrDefault(XRAY_RULE_ACCEPTED, boolean.class, false);
    }

    public void setUnderstandsRules(Player player) {
        PlayerVars playerVars = adminModule.getPlugin().getPlayerVarsManager().getVars(player.getUniqueId());
        playerVars.put(XRAY_RULE_ACCEPTED, true);
    }

    private void sendFirstDiamondWarning(Player player) {
        player.sendMessage(
                Component.newline()
                        .append(Component.text("Congratulations on finding your first diamonds!", NamedTextColor.GOLD))
                        .append(Component.newline())
                        .append(Component.newline())
                        .append(
                                Component.text("Please remember that ", NamedTextColor.RED)
                                        .append(Component.text("using x-ray is against the rules", NamedTextColor.GOLD, TextDecoration.BOLD))
                                        .append(Component.text(",", NamedTextColor.RED))
                        )
                        .append(Component.text(" and will result in a ban and items being removed.", NamedTextColor.RED))
                        .append(Component.newline())
                        .append(Component.newline())
                        .append(Component.text("Suspicious mining is automatically flagged for investigation.", NamedTextColor.RED))
                        //.append(Component.text("Suspicious mining activity will be flagged for investigation.", NamedTextColor.RED))
                        .append(Component.newline())
                        .append(Component.newline())
                        .append(
                                Component.text("Type ")
                                        .append(Component.text("/noxray", NamedTextColor.GOLD))
                                        .append(Component.text(" to continue mining."))
                                        .clickEvent(ClickEvent.runCommand("/noxray"))
                                        .color(NamedTextColor.RED)
                        )
                        .append(Component.newline())
        );
    }

    public CompletableFuture<Boolean> markAsXrayer(UUID player, long durationMillis) {
        LuckPerms luckPerms = adminModule.getPlugin().getHookManager().getLuckPermsHook().getLuckPerms();
        if (luckPerms != null) {
            return isMarkedAsXrayer(player).thenApplyAsync(marked -> {
                if (marked) {
                    return false;
                }
                User user = luckPerms.getUserManager().getUser(player);
                if (user == null) {
                    return false;
                }
                InheritanceNode groupNode = InheritanceNode.builder().group(XRAYER_GROUP).expiry(durationMillis, TimeUnit.MILLISECONDS).build();
                user.data().add(groupNode);
                luckPerms.getUserManager().saveUser(user).join();
                return true;
            });

        }
        return CompletableFuture.completedFuture(false);
    }

    public CompletableFuture<Boolean> unmarkAsXrayer(UUID player) {
        LuckPerms luckPerms = adminModule.getPlugin().getHookManager().getLuckPermsHook().getLuckPerms();
        if (luckPerms != null) {
            return CompletableFuture.supplyAsync(() -> {
                User user = luckPerms.getUserManager().getUser(player);
                if (user == null) {
                    return false;
                }
                for (Node node : user.getNodes()) {
                    if (node.getKey().equalsIgnoreCase("group." + XRAYER_GROUP)) {
                        user.data().remove(node);
                    }
                }
                luckPerms.getUserManager().saveUser(user).join();
                return true;
            });
        }
        return CompletableFuture.completedFuture(false);
    }

    public CompletableFuture<Boolean> isMarkedAsXrayer(UUID player) {
        LuckPerms luckPerms = adminModule.getPlugin().getHookManager().getLuckPermsHook().getLuckPerms();
        if (luckPerms != null) {
            return CompletableFuture.supplyAsync(() -> {
                User user = luckPerms.getUserManager().getUser(player);
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
