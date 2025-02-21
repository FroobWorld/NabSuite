package com.froobworld.nabsuite.modules.basics.nametag;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.stream.Collectors;

public class NameTagScoreboard {

    private final Set<NameTagFeature> features;
    private final Scoreboard scoreboard;

    public NameTagScoreboard(Set<NameTagFeature> features) {
        this.features = features;
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public Set<NameTagFeature> getFeatures() {
        return features;
    }

    public void updatePlayer(Player player) {

        List<NameTagFeature> playerFeatures = features.stream()
                .filter(feature -> feature.testPlayer(player))
                .sorted()
                .toList();

        if (playerFeatures.isEmpty()) {
            removePlayer(player);
            return;
        }

        // String join all the features to create a unique team name, e.g. "afk_muted_jailed"
        String teamName = playerFeatures.stream().map(NameTagFeature::getName).collect(Collectors.joining("_"));

        Team newTeam = scoreboard.getTeam(teamName);
        if (newTeam == null) {
            newTeam = scoreboard.registerNewTeam(teamName);
            newTeam.setCanSeeFriendlyInvisibles(false);
            newTeam.setAllowFriendlyFire(true);
            for (NameTagFeature feature : playerFeatures) {
                feature.setupTeam(newTeam);
            }
        }
        newTeam.addPlayer(player);
    }

    public void removePlayer(OfflinePlayer player) {
        Team team = scoreboard.getPlayerTeam(player);
        if (team != null) {
            team.removePlayer(player);
        }
    }

}
