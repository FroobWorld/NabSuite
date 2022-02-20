package com.froobworld.nabsuite.modules.protect.user;

import com.google.gson.stream.JsonWriter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class GroupUser extends User {

    @Override
    public boolean includesPlayer(Player player) {
        return false;
    }

    @Override
    public String asString() {
        return null;
    }

    @Override
    public Component asDecoratedComponent() {
        return null;
    }

    @Override
    public void writeToJson(JsonWriter jsonWriter) {
    }
}
