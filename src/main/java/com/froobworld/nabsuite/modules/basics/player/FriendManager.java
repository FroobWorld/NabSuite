package com.froobworld.nabsuite.modules.basics.player;

import org.bukkit.entity.Player;

import java.util.UUID;

public class FriendManager {
    private final PlayerDataManager playerDataManager;

    public FriendManager(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    public boolean areFriends(UUID player, UUID otherPlayer) {
        PlayerData playerData = playerDataManager.getPlayerData(player);
        PlayerData otherPlayerData = playerDataManager.getPlayerData(otherPlayer);
        if (playerData == null || otherPlayerData == null) {
            return false;
        }
        return playerData.isFriend(otherPlayer) && otherPlayerData.isFriend(player);
    }

    public boolean areFriends(Player player, UUID otherPlayer) {
        return areFriends(player.getUniqueId(), otherPlayer);
    }

    public boolean areFriends(UUID player, Player otherPlayer) {
        return areFriends(player, otherPlayer.getUniqueId());
    }

    public boolean areFriends(Player player, Player otherPlayer) {
        return areFriends(player.getUniqueId(), otherPlayer.getUniqueId());
    }

}
