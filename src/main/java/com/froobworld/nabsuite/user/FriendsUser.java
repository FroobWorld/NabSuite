package com.froobworld.nabsuite.user;

import com.froobworld.nabsuite.NabSuite;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.google.gson.stream.JsonWriter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class FriendsUser extends User {
    private final NabSuite nabSuite;
    private final UUID uuid;

    public FriendsUser(NabSuite nabSuite, UUID uuid) {
        this.nabSuite = nabSuite;
        this.uuid = uuid;
    }

    @Override
    public boolean includesPlayer(Player player) {
        return nabSuite.getModule(BasicsModule.class)
                .getPlayerDataManager()
                .getFriendManager()
                .areFriends(player, uuid);
    }

    @Override
    public String asString() {
        return "Friends:" + nabSuite.getPlayerIdentityManager().getPlayerIdentity(uuid).getLastName();
    }

    @Override
    public Component asDecoratedComponent() {
        return Component.text("Friends:")
                .append(nabSuite.getPlayerIdentityManager().getPlayerIdentity(uuid).displayName());
    }

    @Override
    public void writeToJson(JsonWriter jsonWriter) {
        try {
            jsonWriter.beginObject()
                    .name("type")
                    .value("friends")
                    .name("value")
                    .value(uuid.toString())
                    .endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FriendsUser that = (FriendsUser) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
