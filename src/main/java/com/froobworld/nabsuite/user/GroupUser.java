package com.froobworld.nabsuite.user;

import com.google.gson.stream.JsonWriter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Objects;

public class GroupUser extends User {
    private final GroupUserManager groupUserManager;
    private final String group;

    public GroupUser(GroupUserManager groupUserManager, String group) {
        this.groupUserManager = groupUserManager;
        this.group = group;
    }

    @Override
    public boolean includesPlayer(Player player) {
        return groupUserManager.getGroupMemberships(player).contains(group);
    }

    @Override
    public String asString() {
        return "Group:" + group;
    }

    @Override
    public Component asDecoratedComponent() {
        return Component.text("Group:" + group);
    }

    @Override
    public void writeToJson(JsonWriter jsonWriter) {
        try {
            jsonWriter.beginObject()
                    .name("type")
                    .value("group")
                    .name("value")
                    .value(group)
                    .endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupUser that = (GroupUser) o;
        return group.equals(that.group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group);
    }
}
