package com.froobworld.nabsuite.modules.basics.player;

import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;
import com.google.common.collect.Sets;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
    public final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final PlayerDataManager playerDataManager;
    private UUID uuid;
    private long lastPlayed;
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
        lock.readLock().lock();
        try {
            return lastPlayed;
        } finally {
            lock.readLock().unlock();
        }
    }

    public Set<UUID> getIgnored() {
        lock.readLock().lock();
        try {
            return Set.copyOf(ignored);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isIgnoring(UUID uuid) {
        lock.readLock().lock();
        try {
            return ignored.contains(uuid);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void ignore(UUID uuid) {
        lock.writeLock().lock();
        try {
            ignored.add(uuid);
        } finally {
            lock.writeLock().unlock();
        }
        playerDataManager.playerDataSaver.scheduleSave(this);
    }

    public void unignore(UUID uuid) {
        lock.writeLock().lock();
        try {
            ignored.remove(uuid);
        } finally {
            lock.writeLock().unlock();
        }
        playerDataManager.playerDataSaver.scheduleSave(this);
    }

    public Set<UUID> getFriends() {
        lock.readLock().lock();
        try {
            return Set.copyOf(friends);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isFriend(UUID uuid) {
        lock.readLock().lock();
        try {
            return friends.contains(uuid);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void addFriend(UUID uuid) {
        lock.writeLock().lock();
        try {
            friends.add(uuid);
        } finally {
            lock.writeLock().unlock();
        }
        playerDataManager.playerDataSaver.scheduleSave(this);
    }

    public void removeFriend(UUID uuid) {
        lock.writeLock().lock();
        try {
            friends.remove(uuid);
        } finally {
            lock.writeLock().unlock();
        }
        playerDataManager.playerDataSaver.scheduleSave(this);
    }

    public boolean teleportFriendsEnabled() {
        lock.readLock().lock();
        try {
            return teleportFriends;
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean teleportRequestsEnabled() {
        lock.readLock().lock();
        try {
            return teleportRequests;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setTeleportFriends(boolean enabled) {
        lock.writeLock().lock();
        try {
            this.teleportFriends = enabled;
        } finally {
            lock.writeLock().unlock();
        }
        playerDataManager.playerDataSaver.scheduleSave(this);
    }

    public void setTeleportRequests(boolean enabled) {
        lock.writeLock().lock();
        try {
            this.teleportRequests = enabled;
        } finally {
            lock.writeLock().unlock();
        }
        playerDataManager.playerDataSaver.scheduleSave(this);
    }

    void updateLastPlayedTime() {
        lock.writeLock().lock();
        try {
            lastPlayed = System.currentTimeMillis();
        } finally {
            lock.writeLock().unlock();
        }
        playerDataManager.playerDataSaver.scheduleSave(this);
    }

    public String toJsonString() {
        try {
            lock.readLock().lock();
            try {
                return SCHEMA.toJsonString(this);
            } finally {
                lock.readLock().unlock();
            }
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
