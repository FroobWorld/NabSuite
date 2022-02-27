package com.froobworld.nabsuite.modules.basics.afk;

import com.froobworld.nabsuite.modules.basics.BasicsModule;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

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
        } else {
            afkStatusMap.remove(player);
            message = player.displayName().append(Component.text(" is no longer AFK.", NamedTextColor.WHITE));
        }
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.sendMessage(message);
        }
        Bukkit.getConsoleSender().sendMessage(message);
    }

    public boolean isAfk(Player player) {
        return afkStatusMap.containsKey(player);
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
                        player.kick(Component.text("Kicked - AFK too long", NamedTextColor.WHITE));
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
