package com.froobworld.nabsuite.modules.basics.nametag;

import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.config.BasicsConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class NameTagManager implements Listener {
    private final BasicsModule basicsModule;
    private final List<NameTagScoreboard> scoreboards = new LinkedList<>();
    private final List<NameTagFeature> features = new LinkedList<>();

    public NameTagManager(BasicsModule basicsModule) {
        this.basicsModule = basicsModule;
        Bukkit.getPluginManager().registerEvents(this, basicsModule.getPlugin());
    }

    public void registerFeature(String name, NameTagFeature.Handler handler) {
        BasicsConfig.NameTagSettings featureConfig = basicsModule.getConfig().nameTag.of(name);
        if (!featureConfig.isEmpty()) {
            features.add(new NameTagFeature(name, handler, featureConfig));
        }
    }

    public void updatePlayer(Player player) {
        for (NameTagScoreboard scoreboard : scoreboards) {
            scoreboard.updatePlayer(player);
        }
    }

    /**
     * Create/set scoreboard for player based on their permissions
     * @param player Player to update scoreboard for
     */
    private void updatePlayerScoreboard(Player player) {
        Set<NameTagFeature> playerFeatures = new HashSet<>();
        for (NameTagFeature feature: features) {
            if (player.hasPermission("nabsuite.nametag." + feature.getName())) {
                playerFeatures.add(feature);
            }
        }
        if (playerFeatures.isEmpty()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            return;
        }

        for (NameTagScoreboard scoreboard : scoreboards) {
            if (scoreboard.getFeatures().equals(playerFeatures)) {
                player.setScoreboard(scoreboard.getScoreboard());
                return;
            }
        }

        NameTagScoreboard scoreboard = new NameTagScoreboard(playerFeatures);
        scoreboards.add(scoreboard);
        Bukkit.getOnlinePlayers().forEach(scoreboard::updatePlayer);
        player.setScoreboard(scoreboard.getScoreboard());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        updatePlayerScoreboard(player);
        updatePlayer(player);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        for (NameTagScoreboard scoreboard : scoreboards) {
            scoreboard.removePlayer(player);
        }
    }
}
