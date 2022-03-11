package com.froobworld.nabsuite.modules.basics.config;

import com.froobworld.nabconfiguration.*;
import com.froobworld.nabconfiguration.annotations.Entry;
import com.froobworld.nabconfiguration.annotations.EntryMap;
import com.froobworld.nabconfiguration.annotations.Section;
import com.froobworld.nabsuite.modules.basics.BasicsModule;

import java.io.File;
import java.util.List;
import java.util.function.Function;

public class BasicsConfig extends NabConfiguration {
    private static final int CONFIG_VERSION = 1;

    public BasicsConfig(BasicsModule basicsModule) {
        super(
                new File(basicsModule.getDataFolder(), "config.yml"),
                () -> basicsModule.getResource("config.yml"),
                i -> basicsModule.getResource("config-patches/" + i + ".patch"),
                CONFIG_VERSION
        );
    }

    @Entry(key = "max-home-permission-check")
    public final ConfigEntry<Integer> maxHomePermissionCheck = ConfigEntries.integerEntry();


    @Section(key = "afk-settings")
    public final AfkSettings afkSettings = new AfkSettings();

    public static class AfkSettings extends ConfigSection {

        @Entry(key = "afk-time")
        public final ConfigEntry<Long> afkTime = ConfigEntries.longEntry();

        @Entry(key = "afk-kick-time")
        public final ConfigEntry<Long> afkKickTime = ConfigEntries.longEntry();

    }

    @Section(key = "messages")
    public final Messages messages = new Messages();

    public static class Messages extends ConfigSection {

        @Entry(key = "motd")
        public final ConfigEntry<List<String>> motd = ConfigEntries.stringListEntry();

        @Entry(key = "rules")
        public final ConfigEntry<List<String>> rules = ConfigEntries.stringListEntry();

        @Section(key = "announcements")
        public final Announcements announcements = new Announcements();

        public static class Announcements extends ConfigSection {

            @Entry(key = "frequency")
            public final ConfigEntry<Long> frequency = ConfigEntries.longEntry();

            @Entry(key = "messages")
            public final ConfigEntry<List<String>> messages = ConfigEntries.stringListEntry();

        }

    }

    @EntryMap(key = "display-name-formats", defaultKey = "default")
    public final ConfigEntryMap<String, String> displayNameFormats = new ConfigEntryMap<>(Function.identity(), ConfigEntry::new, true);

    @Section(key = "auto-promote")
    public final AutoPromote autoPromote = new AutoPromote();

    public static class AutoPromote extends ConfigSection {

        @Entry(key = "track")
        public final ConfigEntry<String> track = new ConfigEntry<>();

        @EntryMap(key = "required-time", defaultKey = "other")
        public final ConfigEntryMap<String, Integer> requiredTime = new ConfigEntryMap<>(Function.identity(), ConfigEntries::integerEntry, true);

    }

}
