package com.froobworld.nabsuite.modules.basics.nametag;

import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.punishment.PunishmentManager;
import com.froobworld.nabsuite.modules.admin.vanish.VanishManager;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.afk.AfkManager;
import com.froobworld.nabsuite.modules.basics.config.BasicsConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.stream.Collectors;

public class NameTagManager implements Listener {
    private final BasicsModule basicsModule;
    private final List<NameTagFeature> features = new LinkedList<>();
    private NameTagFeature vanishFeature;

    public NameTagManager(BasicsModule basicsModule) {
        this.basicsModule = basicsModule;
        Bukkit.getPluginManager().registerEvents(this, basicsModule.getPlugin());
        // Update scoreboards every 3 seconds
        Bukkit.getScheduler().scheduleSyncRepeatingTask(basicsModule.getPlugin(), this::loop, 30L, 60L);
    }

    public void postStartup() {
        AfkManager afkManager = basicsModule.getAfkManager();
        registerFeature("afk", afkManager::isAfk);

        PunishmentManager punishmentManager = basicsModule.getPlugin().getModule(AdminModule.class).getPunishmentManager();
        registerFeature("jailed", p -> punishmentManager.getPunishments(p.getUniqueId()).getJailPunishment() != null);
        registerFeature("muted", p -> punishmentManager.getPunishments(p.getUniqueId()).getMutePunishment() != null);
        registerFeature("restricted", p -> punishmentManager.getPunishments(p.getUniqueId()).getRestrictionPunishment() != null);

        VanishManager vanishManager = basicsModule.getPlugin().getModule(AdminModule.class).getVanishManager();
        vanishFeature = registerFeature("vanished", vanishManager::isVanished);
    }

    public NameTagFeature registerFeature(String name, NameTagFeature.Handler handler) {
        BasicsConfig.NameTagSettings featureConfig = basicsModule.getConfig().nameTag.of(name);
        if (!featureConfig.isEmpty()) {
            NameTagFeature feature = new NameTagFeature(name, handler, featureConfig);
            features.add(feature);
            return feature;
        }
        return null;
    }

    private void loop() {
        Map<Player,Set<NameTagFeature>> features = getPlayerFeatures();
        Bukkit.getOnlinePlayers().forEach(player -> updatePlayerScoreboard(player, features));
    }

    private Map<Player,Set<NameTagFeature>> getPlayerFeatures() {
        Map<Player,Set<NameTagFeature>> playerFeatures = new HashMap<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerFeatures.put(player, features.stream().filter(feature -> feature.testPlayer(player)).collect(Collectors.toSet()));
        }
        return playerFeatures;
    }

    private Team getEveryoneTeam(Scoreboard scoreboard) {
        Team team = scoreboard.getTeam("everyone");
        if (team == null) {
            team = scoreboard.registerNewTeam("everyone");
            team.setCanSeeFriendlyInvisibles(true);
            team.setAllowFriendlyFire(true);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }
        return team;
    }

    private void updatePlayerScoreboard(Player player, Map<Player,Set<NameTagFeature>> playerFeatures) {
        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard.equals(Bukkit.getScoreboardManager().getMainScoreboard())) {
            return;
        }

        boolean isVanished = basicsModule.getPlugin().getModule(AdminModule.class).getVanishManager().isVanished(player);
        Team teamEveryone = isVanished ? getEveryoneTeam(scoreboard) : null;

        playerFeatures.forEach((target, featureSet) -> {
            // Filter out any features the player isn't allowed to see
            featureSet = featureSet.stream()
                    .filter(feature -> feature.hasPermission(player))
                    .collect(Collectors.toSet());

            if (!player.equals(target) && featureSet.contains(vanishFeature)) {
                // Target is vanished, put in own team
                Team team = scoreboard.getTeam("vanished");
                if (team == null) {
                    team = scoreboard.registerNewTeam("vanished");
                    team.setCanSeeFriendlyInvisibles(true);
                    team.setAllowFriendlyFire(true);
                    team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
                    vanishFeature.setupTeam(team);
                }
                team.addPlayer(target);
            } else if (isVanished) {
                // Player is vanished, put everyone in same team
                teamEveryone.addPlayer(target);
            } else if (featureSet.isEmpty()) {
                // Target has no features, remove from current team
                Team current = scoreboard.getPlayerTeam(target);
                if (current != null) {
                    current.removePlayer(target);
                }
            } else {
                // Calculate team from features and add target
                // example team name with multiple features: "afk_muted_restricted"
                String teamName = featureSet.stream().map(NameTagFeature::getName).collect(Collectors.joining("_"));
                Team team = scoreboard.getTeam(teamName);
                if (team == null) {
                    team = scoreboard.registerNewTeam(teamName);
                    team.setCanSeeFriendlyInvisibles(false);
                    team.setAllowFriendlyFire(true);
                    for (NameTagFeature feature : featureSet) {
                        feature.setupTeam(team);
                    }
                }
                team.addPlayer(target);
            }
        });

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Set<NameTagFeature> playerFeatures = features.stream()
                .filter(feature -> feature.hasPermission(player))
                .collect(Collectors.toSet());
        if (!playerFeatures.isEmpty() || player.hasPermission(VanishManager.VANISH_PERMISSION)) {
            // Create per-player scoreboard
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            updatePlayerScoreboard(player, getPlayerFeatures());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Bukkit.getOnlinePlayers().forEach(p -> {
            Team current = p.getScoreboard().getPlayerTeam(player);
            if (current != null) {
                current.removePlayer(player);
            }
        });
    }
}
