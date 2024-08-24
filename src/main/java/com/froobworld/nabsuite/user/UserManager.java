package com.froobworld.nabsuite.user;

import com.froobworld.nabsuite.NabSuite;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.util.UUID;

public class UserManager {
    private final NabSuite nabSuite;
    private final GroupUserManager groupUserManager;

    public UserManager(NabSuite nabSuite) {
        this.nabSuite = nabSuite;
        this.groupUserManager = new GroupUserManager(nabSuite);
    }

    public User parseUser(JsonReader jsonReader) {
        String type = null;
        String value = null;
        try {
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                String name = jsonReader.nextName();
                if (name.equalsIgnoreCase("type")) {
                    type = jsonReader.nextString().toLowerCase();
                } else if (name.equalsIgnoreCase("value")) {
                    value = jsonReader.nextString();
                }
            }
            jsonReader.endObject();
            return switch (type) {
                case "player" -> newPlayerUser(UUID.fromString(value));
                case "friends" -> newFriendsUser(UUID.fromString(value));
                case "group" -> newGroupUser(value);
                default -> new UnknownUser(type, value);
            };
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public PlayerUser newPlayerUser(UUID uuid) {
        return new PlayerUser(nabSuite, uuid);
    }

    public FriendsUser newFriendsUser(UUID uuid) {
        return new FriendsUser(nabSuite, uuid);
    }

    public GroupUser newGroupUser(String groupName) {
        return new GroupUser(groupUserManager, groupName);
    }

    public GroupUserManager getGroupUserManager() {
        return groupUserManager;
    }
}
