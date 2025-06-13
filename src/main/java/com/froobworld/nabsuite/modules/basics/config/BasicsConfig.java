package com.froobworld.nabsuite.modules.basics.config;

import com.froobworld.nabconfiguration.*;
import com.froobworld.nabconfiguration.annotations.Entry;
import com.froobworld.nabconfiguration.annotations.EntryMap;
import com.froobworld.nabconfiguration.annotations.Section;
import com.froobworld.nabconfiguration.annotations.SectionMap;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import org.bukkit.World;

import java.io.File;
import java.util.List;
import java.util.function.Function;

public class BasicsConfig extends NabConfiguration {
    private static final int CONFIG_VERSION = 7;

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

    @Entry(key = "chat-channel-prefix")
    public final ConfigEntry<String> chatChannelPrefix = new ConfigEntry<>();

    @Entry(key = "chat-channel-format")
    public final ConfigEntry<String> chatChannelFormat = new ConfigEntry<>();

    @Section(key = "auto-promote")
    public final AutoPromote autoPromote = new AutoPromote();

    public static class AutoPromote extends ConfigSection {

        @Entry(key = "track")
        public final ConfigEntry<String> track = new ConfigEntry<>();

        @EntryMap(key = "required-time", defaultKey = "other")
        public final ConfigEntryMap<String, Integer> requiredTime = new ConfigEntryMap<>(Function.identity(), ConfigEntries::integerEntry, true);

    }

    @Section(key = "auto-demote")
    public final AutoDemote autoDemote = new AutoDemote();

    public static class AutoDemote extends ConfigSection {

        @Entry(key = "groups")
        public final ConfigEntry<List<String>> groups = ConfigEntries.stringListEntry();

        @EntryMap(key = "inactive-time", defaultKey = "default")
        public final ConfigEntryMap<String, Integer> inactiveTime = new ConfigEntryMap<>(Function.identity(), ConfigEntries::integerEntry, true);

        @EntryMap(key = "demote-to-group", defaultKey = "default")
        public final ConfigEntryMap<String, String> demoteToGroup = new ConfigEntryMap<>(Function.identity(), ConfigEntry::new, true);

    }

    @Section(key = "random-teleport")
    public final RandomTeleportSettings randomTeleport = new RandomTeleportSettings();

    public static class RandomTeleportSettings extends ConfigSection {

        @Entry(key = "max-random-teleports")
        public final ConfigEntry<Integer> maxRandomTeleports = ConfigEntries.integerEntry();

        @Entry(key = "regeneration-frequency")
        public final ConfigEntry<Long> regenerationFrequency = ConfigEntries.longEntry();

        @Entry(key = "enabled-worlds")
        public final ConfigEntry<List<String>> enabledWorlds = ConfigEntries.stringListEntry();

        @SectionMap(key = "world-settings", defaultKey = "default")
        public final ConfigSectionMap<World, WorldSettings> worldSettings = new ConfigSectionMap<>(World::getName, WorldSettings.class, true);

        public static class WorldSettings extends ConfigSection {

            @Entry(key = "centre-x")
            public final ConfigEntry<Integer> centreX = ConfigEntries.integerEntry();

            @Entry(key = "centre-z")
            public final ConfigEntry<Integer> centreZ = ConfigEntries.integerEntry();

            @Entry(key = "radius-x")
            public final ConfigEntry<Integer> radiusX = ConfigEntries.integerEntry();

            @Entry(key = "radius-z")
            public final ConfigEntry<Integer> radiusZ = ConfigEntries.integerEntry();

            @Entry(key = "exclusion-centre-x")
            public final ConfigEntry<Integer> exclusionCentreX = ConfigEntries.integerEntry();

            @Entry(key = "exclusion-centre-z")
            public final ConfigEntry<Integer> exclusionCentreZ = ConfigEntries.integerEntry();

            @Entry(key = "exclusion-radius-x")
            public final ConfigEntry<Integer> exclusionRadiusX = ConfigEntries.integerEntry();

            @Entry(key = "exclusion-radius-z")
            public final ConfigEntry<Integer> exclusionRadiusZ = ConfigEntries.integerEntry();

            @Entry(key = "exclude-biomes")
            public final ConfigEntry<List<String>> excludeBiomes = ConfigEntries.stringListEntry();

            @Entry(key = "pregenerate-max")
            public final ConfigEntry<Integer> pregenerateMax = ConfigEntries.integerEntry();

            @Entry(key = "pregenerate-interval")
            public final ConfigEntry<Integer> pregenerateInterval = ConfigEntries.integerEntry();

        }

    }

    @SectionMap(key = "name-tag", defaultKey = "default")
    public ConfigSectionMap<String, NameTagSettings> nameTag = new ConfigSectionMap<>(s -> s, NameTagSettings.class,  true);

    public static class NameTagSettings extends ConfigSection {

        @Entry(key = "priority")
        public final ConfigEntry<Integer> priority = ConfigEntries.integerEntry();

        @Entry(key = "prefix")
        public final ConfigEntry<String> prefix = new ConfigEntry<>();

        @Entry(key = "suffix")
        public final ConfigEntry<String> suffix = new ConfigEntry<>();

        @Entry(key = "color")
        public final ConfigEntry<String> color = new ConfigEntry<>();

        public boolean isEmpty() {
            return prefix.get().isEmpty() && suffix.get().isEmpty() && color.get().isEmpty();
        }

    }

}
