package com.froobworld.nabsuite.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;

import java.util.regex.Pattern;

public final class ComponentUtils {
    private static final Pattern HTTP_PATTERN = Pattern.compile("(https?://)?[a-z0-9]+(\\.[a-z0-9]+)*(\\.[a-z0-9]{1,10})((/+)[^/ ]*)*", Pattern.MULTILINE);
    private static final TextReplacementConfig LINK_REPLACEMENT_CONFIG = TextReplacementConfig.builder()
            .match(HTTP_PATTERN)
            .replacement((builder) -> builder.clickEvent(ClickEvent.openUrl(builder.content().toLowerCase().startsWith("http") ? builder.content() : "https://" + builder.content())))
            .build();

    private ComponentUtils() {}

    public static Component clickableUrls(Component inputComponent) {
        return inputComponent.replaceText(LINK_REPLACEMENT_CONFIG);
    }

}
