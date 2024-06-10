package com.froobworld.nabsuite.modules.discord.utils;

import net.dv8tion.jda.api.utils.MarkdownSanitizer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public final class DiscordUtils {
    private static final Pattern HTTP_PATTERN = Pattern.compile("(https?://)?[a-z0-9]+(\\.[a-z0-9]+)*(\\.[a-z0-9]{1,10})((/+)[^/ ]*)*");

    private DiscordUtils() {}

    public static String getAvatarUrl(UUID uuid, int size) {
        return String.format("http://cravatar.eu/avatar/%s/%d", uuid, size);
    }

    public static String getHeadUrl(UUID uuid, int size) {
        return String.format("http://cravatar.eu/head/%s/%d", uuid, size);
    }

    public static String escapeMarkdown(String inputString) {
        AtomicInteger urlCounter = new AtomicInteger(0);
        Map<String, String> replacedUrls = new HashMap<>();

        String cleanString = HTTP_PATTERN.matcher(inputString).replaceAll((matchResult -> {
            String urlKey = null;
            while (urlKey == null || inputString.contains(urlKey)) {
                urlKey = "url" + urlCounter.getAndIncrement();
            }
            replacedUrls.put(urlKey, matchResult.group());
            return urlKey;
        }));

        String escapedString = MarkdownSanitizer.escape(cleanString);
        for (Map.Entry<String, String> entry : replacedUrls.entrySet()) {
            String urlKey = entry.getKey();
            String urlString = entry.getValue();
            escapedString = escapedString.replace(urlKey, urlString);
        }
        return escapedString;
    }

}
