package com.froobworld.nabsuite.modules.protect.user;

import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.google.gson.stream.JsonWriter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class FriendsUser extends User {
    private final ProtectModule protectModule;
    private final UUID uuid;

    public FriendsUser(ProtectModule protectModule, UUID uuid) {
        this.protectModule = protectModule;
        this.uuid = uuid;
    }

    @Override
    public boolean includesPlayer(Player player) {
        return protectModule.getPlugin().getModule(BasicsModule.class)
                .getPlayerDataManager()
                .getFriendManager()
                .areFriends(player, uuid);
    }

    @Override
    public String asString() {
        return "Friends:" + protectModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(uuid).getLastName();
    }

    @Override
    public Component asDecoratedComponent() {
        return Component.text("Friends:")
                .append(protectModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(uuid).displayName());
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
