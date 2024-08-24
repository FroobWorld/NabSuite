package com.froobworld.nabsuite.modules.basics.channel;

import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;
import com.froobworld.nabsuite.user.User;
import com.froobworld.nabsuite.user.UserManager;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChatChannel {

    private static final SimpleDataSchema<ChatChannel> SCHEMA = new SimpleDataSchema.Builder<ChatChannel>()
            .addField("creator", SchemaEntries.uuidEntry(
                    channel -> channel.creator,
                    (channel, creator) -> channel.creator = creator
            ))
            .addField("name", SchemaEntries.stringEntry(
                    channel -> channel.name,
                    (channel, name) -> channel.name = name
            ))
            .addField("owners", SchemaEntries.setEntry(
                    channel -> channel.owners,
                    (channel, owners) -> channel.owners = owners,
                    (jsonReader, channel) -> channel.userManager.parseUser(jsonReader),
                    User::writeToJson
            ))
            .addField("managers", SchemaEntries.setEntry(
                    channel -> channel.managers,
                    (channel, managers) -> channel.managers = managers,
                    (jsonReader, channel) -> channel.userManager.parseUser(jsonReader),
                    User::writeToJson
            ))
            .addField("users", SchemaEntries.setEntry(
                    channel -> channel.users,
                    (channel, users) -> channel.users = users,
                    (jsonReader, channel) -> channel.userManager.parseUser(jsonReader),
                    User::writeToJson
            ))
            .addField("joined-users", SchemaEntries.setEntry(
                    channel -> channel.joinedUsers,
                    (channel, joinedUsers) -> channel.joinedUsers = joinedUsers,
                    (jsonReader, channel) -> UUID.fromString(jsonReader.nextString()),
                    (uuid, jsonWriter) -> jsonWriter.value(uuid.toString())
            ))
            .build();

    private final UserManager userManager;
    private final ChatChannelManager channelManager;
    private UUID creator;
    private String name;
    private Set<User> owners;
    private Set<User> managers;
    private Set<User> users;
    private Set<UUID> joinedUsers;

    public ChatChannel(ChatChannelManager channelManager, UserManager userManager, UUID creator, String name, User owner) {
        this.userManager = userManager;
        this.channelManager = channelManager;
        this.creator = creator;
        this.name = name;
        this.owners = owner == null ? new HashSet<>() : new HashSet<>(Collections.singleton(owner));
        this.managers = new HashSet<>();
        this.users = new HashSet<>();
        this.joinedUsers = new HashSet<>();
    }

    private ChatChannel(ChatChannelManager channelManager, UserManager userManager) {
        this.userManager = userManager;
        this.channelManager = channelManager;
    }

    public UUID getCreator() {
        return creator;
    }

    public String getName() {
        return name;
    }

    public Set<User> getUsers() {
        return users;
    }

    public Set<User> getManagers() {
        return managers;
    }

    public Set<User> getOwners() {
        return owners;
    }

    public boolean isUser(User user) {
        return users.contains(user);
    }

    public boolean isManager(User user) {
        return managers.contains(user);
    }

    public boolean isOwner(User user) {
        return owners.contains(user);
    }

    public boolean isUser(Player player) {
        for (User user : users) {
            if (user.includesPlayer(player)) {
                return true;
            }
        }
        return false;
    }

    public boolean isManager(Player player) {
        for (User manager : managers) {
            if (manager.includesPlayer(player)) {
                return true;
            }
        }
        return false;
    }

    public boolean isOwner(Player player) {
        for (User owner : owners) {
            if (owner.includesPlayer(player)) {
                return true;
            }
        }
        return false;
    }

    public boolean isJoined(UUID user) {
        return joinedUsers.contains(user);
    }

    public void addUser(User user) {
        users.add(user);
        scheduleSave();
    }

    public void addManager(User user) {
        managers.add(user);
        scheduleSave();
    }

    public void addOwner(User user) {
        owners.add(user);
        scheduleSave();
    }

    public void join(UUID user) {
        joinedUsers.add(user);
        scheduleSave();
    }

    public void removeUser(User user) {
        users.remove(user);
        scheduleSave();
    }

    public void removeManager(User user) {
        managers.remove(user);
        scheduleSave();
    }

    public void removeOwner(User user) {
        owners.remove(user);
        scheduleSave();
    }

    public void leave(UUID user) {
        joinedUsers.remove(user);
        scheduleSave();
    }

    public boolean hasUserRights(Player player) {
        return isUser(player) || isManager(player) || isOwner(player);
    }

    public boolean hasManagerRights(Player player) {
        return isManager(player) || isOwner(player);
    }

    private void scheduleSave() {
        channelManager.channelSaver.scheduleSave(this);
    }

    public String toJsonString() {
        try {
            return ChatChannel.SCHEMA.toJsonString(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ChatChannel fromJsonString(ChatChannelManager channelManager, UserManager userManager, String jsonString) {
        ChatChannel channel = new ChatChannel(channelManager, userManager);
        try {
            ChatChannel.SCHEMA.populateFromJsonString(channel, jsonString);
            return channel;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
