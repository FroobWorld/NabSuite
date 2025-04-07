package com.froobworld.nabsuite.modules.basics.afk;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

public class AfkManager implements Listener {
    private final BasicsModule basicsModule;
    private final Map<Player, AfkStatus> afkStatusMap = new WeakHashMap<>();
    private final Map<Player, Long> lastActivityMap = new WeakHashMap<>();

    public AfkManager(BasicsModule basicsModule) {
        this.basicsModule = basicsModule;
        Bukkit.getPluginManager().registerEvents(this, basicsModule.getPlugin());
        Bukkit.getScheduler().scheduleSyncRepeatingTask(basicsModule.getPlugin(), this::loop, 20, 20);
    }

    public void setAfk(Player player, boolean afk, boolean auto) {
        Component message;
        if (afk) {
            afkStatusMap.put(player, new AfkStatus(System.currentTimeMillis(), player.getLocation(), auto));
            message = player.displayName().append(Component.text(" is now AFK.", NamedTextColor.WHITE));
            player.setSleepingIgnored(true);
        } else {
            afkStatusMap.remove(player);
            message = player.displayName().append(Component.text(" is no longer AFK.", NamedTextColor.WHITE));
            player.setSleepingIgnored(false);
            lastActivityMap.put(player, System.currentTimeMillis());
        }
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.sendMessage(message);
        }
        Bukkit.getConsoleSender().sendMessage(message);
    }

    public boolean isAfk(Player player) {
        return afkStatusMap.containsKey(player);
    }

    public Long getAfkTimestamp(Player player) {
        AfkStatus afkStatus = afkStatusMap.get(player);
        if (afkStatus == null) {
            return null;
        }
        return afkStatus.getTimestamp();
    }

    private void loop() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            lastActivityMap.putIfAbsent(player, System.currentTimeMillis());
            if (isAfk(player)) {
                AfkStatus afkStatus = afkStatusMap.get(player);
                if (!player.getLocation().getWorld().equals(afkStatus.getAfkLocation().getWorld()) || player.getLocation().distance(afkStatus.getAfkLocation()) > 1) {
                    setAfk(player, false, true);
                } else {
                    if (afkStatus.isAuto() && System.currentTimeMillis() - afkStatus.getTimestamp() > TimeUnit.SECONDS.toMillis(basicsModule.getConfig().afkSettings.afkKickTime.get())) {
                        player.kick(
                                Component.text("Kicked - AFK too long.", NamedTextColor.WHITE)
                                        .append(Component.newline())
                                        .append(Component.newline())
                                        .append(Component.text("Use /afk to avoid being kicked.", NamedTextColor.WHITE))
                        );
                        afkStatusMap.remove(player);
                    }
                }
            } else {
                if (System.currentTimeMillis() - lastActivityMap.get(player) > TimeUnit.SECONDS.toMillis(basicsModule.getConfig().afkSettings.afkTime.get())) {
                    setAfk(player, true, true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = false)
    private void onPlayerChat(AsyncChatEvent event) {
        if (isAfk(event.getPlayer())) {
            setAfk(event.getPlayer(), false, false);
        }
        lastActivityMap.put(event.getPlayer(), System.currentTimeMillis());
    }

    @EventHandler(ignoreCancelled = true)
    private void onPlayerMove(PlayerMoveEvent event) {
        if (event.hasChangedPosition()) {
            lastActivityMap.put(event.getPlayer(), System.currentTimeMillis());
        }
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        afkStatusMap.remove(event.getPlayer());
        lastActivityMap.put(event.getPlayer(), System.currentTimeMillis());
    }

    private List<Player> getFullJoinKickablePlayers() {
        return Bukkit.getOnlinePlayers().stream()
                .map(player -> (Player) player)
                .filter(afkStatusMap::containsKey)
                .filter(player -> System.currentTimeMillis() - afkStatusMap.get(player).getTimestamp() > TimeUnit.SECONDS.toMillis(basicsModule.getConfig().afkSettings.afkKickTime.get()))
                .sorted(Comparator.comparingLong(player -> afkStatusMap.get(player).getTimestamp()))
                .toList();
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getResult() == PlayerLoginEvent.Result.KICK_FULL) {
            List<Player> kickablePlayers = getFullJoinKickablePlayers();
            int playersToKick = Bukkit.getOnlinePlayers().size() - Bukkit.getMaxPlayers() + 1;
            if (playersToKick < 0) {
                return;
            }
            if (playersToKick > kickablePlayers.size()) {
                return;
            }
            for (int i = 0; i < playersToKick; i++) {
                kickablePlayers.get(i).kick(
                        Component.text("Kicked - server full.", NamedTextColor.WHITE)
                                .append(Component.newline())
                                .append(Component.newline())
                                .append(Component.text("AFK players can be kicked when another player attempts to join.", NamedTextColor.WHITE))
                );
            }
            event.setResult(PlayerLoginEvent.Result.ALLOWED);
        }
    }

    @EventHandler
    private void onServerListPing(PaperServerListPingEvent event) {
        if (event.getNumPlayers() >= event.getMaxPlayers()) {
            int kickablePlayers = getFullJoinKickablePlayers().size();
            if (event.getNumPlayers() - kickablePlayers < event.getMaxPlayers()) {
                event.setNumPlayers(event.getMaxPlayers() - 1); // Show one less than the max amount if there is room after kicking AFK players
            }
        }
    }

    public static class AfkStatus {
        private final long timestamp;
        private final Location afkLocation;
        private final boolean auto;

        public AfkStatus(long timestamp, Location afkLocation, boolean auto) {
            this.timestamp = timestamp;
            this.afkLocation = afkLocation;
            this.auto = auto;
        }

        public Location getAfkLocation() {
            return afkLocation;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public boolean isAuto() {
            return auto;
        }

    }

}
