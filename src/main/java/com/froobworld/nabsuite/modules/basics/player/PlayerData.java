package com.froobworld.nabsuite.modules.basics.player;

import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;
import com.google.common.collect.Sets;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class PlayerData {
    private static final SimpleDataSchema<PlayerData> SCHEMA = new SimpleDataSchema.Builder<PlayerData>()
            .addField("uuid", SchemaEntries.uuidEntry(
                    playerData -> playerData.uuid,
                    (playerData, uuid) -> playerData.uuid = uuid
            ))
            .addField("first-joined", SchemaEntries.longEntry(
                    playerData -> playerData.firstJoined,
                    (playerData, firstJoined) -> playerData.firstJoined = firstJoined
            ))
            .addField("last-played", SchemaEntries.longEntry(
                    playerData -> playerData.lastPlayed,
                    (playerData, lastPlayed) -> playerData.lastPlayed = lastPlayed
            ))
            .addField("last-deputy-expire-notification", SchemaEntries.longEntry(
                    playerData -> playerData.lastDeputyExpireNotification,
                    (playerData, lastDeputyExpireNotification) -> playerData.lastDeputyExpireNotification = lastDeputyExpireNotification
            ))
            .addField("friends", SchemaEntries.setEntry(
                    playerData -> playerData.friends,
                    (playerData, uuids) -> playerData.friends = uuids,
                    (jsonReader, playerData) -> UUID.fromString(jsonReader.nextString()),
                    (uuid, jsonWriter) -> jsonWriter.value(uuid.toString()))
            )
            .addField("ignored", SchemaEntries.setEntry(
                    playerData -> playerData.ignored,
                    (playerData, uuids) -> playerData.ignored = uuids,
                    (jsonReader, playerData) -> UUID.fromString(jsonReader.nextString()),
                    (uuid, jsonWriter) -> jsonWriter.value(uuid.toString()))
            )
            .addField("teleport-friends-enabled", SchemaEntries.booleanEntry(
                    playerData -> playerData.teleportFriends,
                    (playerData, bool) -> playerData.teleportFriends = bool
            ))
            .addField("teleport-requests-enabled", SchemaEntries.booleanEntry(
                    playerData -> playerData.teleportRequests,
                    (playerData, bool) -> playerData.teleportRequests = bool
            ))
            .build();
    private final PlayerDataManager playerDataManager;
    private UUID uuid;
    private long lastPlayed;
    private long lastDeputyExpireNotification;
    private long firstJoined;
    private Set<UUID> ignored;
    private Set<UUID> friends;
    private boolean teleportFriends;
    private boolean teleportRequests;

    private PlayerData(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    public PlayerData(PlayerDataManager playerDataManager, Player player) {
        this.playerDataManager = playerDataManager;
        this.uuid = player.getUniqueId();
        this.firstJoined = player.getFirstPlayed();
        this.lastPlayed = System.currentTimeMillis();
        this.ignored = Sets.newHashSet();
        this.friends = Sets.newHashSet();
        this.teleportFriends = true;
        this.teleportRequests = true;
    }

    public UUID getUuid() {
        return uuid;
    }

    public long getFirstJoined() {
        return firstJoined;
    }

    public long getLastPlayed() {
        return lastPlayed;
    }


    public long getLastDeputyExpireNotification() {
        return lastDeputyExpireNotification;
    }

    public Set<UUID> getIgnored() {
        return Set.copyOf(ignored);
    }

    public boolean isIgnoring(UUID uuid) {
        return ignored.contains(uuid);
    }

    public void ignore(UUID uuid) {
        ignored.add(uuid);
        playerDataManager.playerDataSaver.scheduleSave(this);
    }

    public void unignore(UUID uuid) {
        ignored.remove(uuid);
        playerDataManager.playerDataSaver.scheduleSave(this);
    }

    public Set<UUID> getFriends() {
        return Set.copyOf(friends);
    }

    public boolean isFriend(UUID uuid) {
        return friends.contains(uuid);
    }

    public void addFriend(UUID uuid) {
        friends.add(uuid);
        playerDataManager.playerDataSaver.scheduleSave(this);
    }

    public void removeFriend(UUID uuid) {
        friends.remove(uuid);
        playerDataManager.playerDataSaver.scheduleSave(this);
    }

    public boolean teleportFriendsEnabled() {
        return teleportFriends;
    }

    public boolean teleportRequestsEnabled() {
        return teleportRequests;
    }

    public void setTeleportFriends(boolean enabled) {
        this.teleportFriends = enabled;
        playerDataManager.playerDataSaver.scheduleSave(this);
    }

    public void setTeleportRequests(boolean enabled) {
        this.teleportRequests = enabled;
        playerDataManager.playerDataSaver.scheduleSave(this);
    }

    void updateLastPlayedTime() {
        lastPlayed = System.currentTimeMillis();
        playerDataManager.playerDataSaver.scheduleSave(this);
    }

    public void setLastDeputyExpireNotification(long timestamp) {
        lastDeputyExpireNotification = timestamp;
        playerDataManager.playerDataSaver.scheduleSave(this);
    }

    public String toJsonString() {
        try {
            return SCHEMA.toJsonString(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PlayerData fromJsonString(PlayerDataManager playerDataManager, String jsonString) {
        PlayerData playerData = new PlayerData(playerDataManager);
        try {
            SCHEMA.populateFromJsonString(playerData, jsonString);
            return playerData;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
