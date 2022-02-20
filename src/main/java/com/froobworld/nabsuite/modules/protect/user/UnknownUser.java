package com.froobworld.nabsuite.modules.protect.user;

import com.google.gson.stream.JsonWriter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.io.IOException;

public class UnknownUser extends User {
    private final String type;
    private final String value;

    public UnknownUser(String type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public boolean includesPlayer(Player player) {
        return false;
    }

    @Override
    public String asString() {
        return type + ":" + value;
    }

    @Override
    public Component asDecoratedComponent() {
        return Component.text(asString());
    }

    @Override
    public void writeToJson(JsonWriter jsonWriter) {
        try {
            jsonWriter.beginObject()
                    .name("type")
                    .value(type)
                    .name("value")
                    .value(value)
                    .endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
