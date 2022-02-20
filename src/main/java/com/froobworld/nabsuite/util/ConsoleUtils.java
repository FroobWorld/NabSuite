package com.froobworld.nabsuite.util;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import java.util.UUID;

public final class ConsoleUtils {
    public static final UUID CONSOLE_UUID = new UUID(0, 0);

    private ConsoleUtils() {}

    public static UUID getSenderUUID(CommandSender sender) {
        return sender instanceof Entity ? ((Entity) sender).getUniqueId() : CONSOLE_UUID;
    }

}
