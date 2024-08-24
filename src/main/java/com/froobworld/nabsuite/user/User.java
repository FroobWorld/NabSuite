package com.froobworld.nabsuite.user;

import com.google.gson.stream.JsonWriter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public abstract class User {

    public abstract boolean includesPlayer(Player player);

    public abstract String asString();

    public abstract Component asDecoratedComponent();

    public abstract void writeToJson(JsonWriter jsonWriter);

}
