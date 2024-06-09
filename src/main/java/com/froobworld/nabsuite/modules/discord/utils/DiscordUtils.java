package com.froobworld.nabsuite.modules.discord.utils;

import java.util.UUID;

public final class DiscordUtils {

    private DiscordUtils() {}

    public static String getAvatarUrl(UUID uuid, int size) {
        return String.format("http://cravatar.eu/avatar/%s/%d", uuid, size);
    }

    public static String getHeadUrl(UUID uuid, int size) {
        return String.format("http://cravatar.eu/head/%s/%d", uuid, size);
    }

}
