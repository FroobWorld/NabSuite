package com.froobworld.nabsuite.modules.protect.user;

import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.util.UUID;

public class UserManager {
    private final ProtectModule protectModule;

    public UserManager(ProtectModule protectModule) {
        this.protectModule = protectModule;
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
                case "player" -> new PlayerUser(protectModule, UUID.fromString(value));
                case "friends" -> new FriendsUser(protectModule, UUID.fromString(value));
                case "group" -> null;
                default -> new UnknownUser(type, value);
            };
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
