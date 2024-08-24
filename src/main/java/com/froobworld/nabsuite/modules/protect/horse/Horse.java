package com.froobworld.nabsuite.modules.protect.horse;

import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;
import com.froobworld.nabsuite.user.User;
import com.froobworld.nabsuite.user.UserManager;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Horse {
    static final SimpleDataSchema<Horse> SCHEMA = new SimpleDataSchema.Builder<Horse>()
            .addField("uuid", SchemaEntries.uuidEntry(
                    horse -> horse.uuid,
                    (horse, uuid) -> horse.uuid = uuid
            ))
            .addField("owner", SchemaEntries.uuidEntry(
                    horse -> horse.owner,
                    (horse, owner) -> horse.owner = owner
            ))
            .addField("users", SchemaEntries.setEntry(
                    horse -> horse.users,
                    (horse, users) -> horse.users = users,
                    (jsonReader, horse) -> horse.userManager.parseUser(jsonReader),
                    User::writeToJson
            ))
            .build();

    private final HorseManager horseManager;
    private final UserManager userManager;
    private UUID uuid;
    private UUID owner;
    private Set<User> users;

    public Horse(HorseManager horseManager, UserManager userManager, UUID uuid, UUID owner) {
        this.horseManager = horseManager;
        this.userManager = userManager;
        this.uuid = uuid;
        this.owner = owner;
        this.users = new HashSet<>();
    }

    private Horse(HorseManager horseManager, UserManager userManager) {
        this.horseManager = horseManager;
        this.userManager = userManager;
    }

    public UUID getUuid() {
        return uuid;
    }

    public UUID getOwner() {
        return owner;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void addUser(User user) {
        users.add(user);
        horseManager.horseSaver.scheduleSave(this);
    }

    public void removeUser(User user) {
        users.remove(user);
        horseManager.horseSaver.scheduleSave(this);
    }

    public boolean isOwner(Player player) {
        return player.getUniqueId().equals(owner);
    }

    public boolean isUser(User user) {
        return users.contains(user);
    }

    public boolean hasUserRights(Player player) {
        for (User user : users) {
            if (user.includesPlayer(player)) {
                return true;
            }
        }
        return isOwner(player);
    }

    public String toJsonString() {
        try {
            return Horse.SCHEMA.toJsonString(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Horse fromJsonString(HorseManager horseManager, UserManager userManager, String jsonString) {
        Horse horse = new Horse(horseManager, userManager);
        try {
            Horse.SCHEMA.populateFromJsonString(horse, jsonString);
            return horse;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
