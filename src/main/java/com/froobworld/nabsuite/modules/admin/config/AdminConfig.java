package com.froobworld.nabsuite.modules.admin.config;

import com.froobworld.nabconfiguration.ConfigEntries;
import com.froobworld.nabconfiguration.ConfigEntry;
import com.froobworld.nabconfiguration.ConfigSection;
import com.froobworld.nabconfiguration.ConfigSectionMap;
import com.froobworld.nabconfiguration.NabConfiguration;
import com.froobworld.nabconfiguration.annotations.Entry;
import com.froobworld.nabconfiguration.annotations.Section;
import com.froobworld.nabconfiguration.annotations.SectionMap;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.util.DurationParser;

import java.io.File;
import java.util.Date;
import java.util.List;

public class AdminConfig extends NabConfiguration {
    private static final int CONFIG_VERSION = 4;

    public AdminConfig(AdminModule adminModule) {
        super(
                new File(adminModule.getDataFolder(), "config.yml"),
                () -> adminModule.getResource("config.yml"),
                i -> adminModule.getResource("config-patches/" + i + ".patch"),
                CONFIG_VERSION
        );
    }

    @Entry(key = "jail-command-whitelist")
    public final ConfigEntry<List<String>> jailCommandWhitelist = ConfigEntries.stringListEntry();

    @Entry(key = "word-filters")
    public final ConfigEntry<List<String>> wordFilters = ConfigEntries.stringListEntry();

    @Entry(key = "highly-offensive-words")
    public final ConfigEntry<List<String>> highlyOffensiveWords = ConfigEntries.stringListEntry();

    @Section(key = "ban-settings")
    public final BanSettings banSettings = new BanSettings();

    public static class BanSettings extends ConfigSection {

        @Entry(key = "ban-appeal-url")
        public final ConfigEntry<String> banAppealUrl = new ConfigEntry<>();

        @Entry(key = "auto-expiry-time")
        public final ConfigEntry<Integer> autoExpiryTime = ConfigEntries.integerEntry();

        @Entry(key = "auto-expiry-cutoff")
        public final ConfigEntry<Date> autoExpiryCutoff = new ConfigEntry<>();

    }

    @Entry(key = "deputy-levels")
    public final ConfigEntry<List<String>> deputyLevels = ConfigEntries.stringListEntry();

    @SectionMap(key = "deputy-settings", defaultKey = "default")
    public ConfigSectionMap<String, DeputySettings> deputySettings = new ConfigSectionMap<>(s -> s, DeputySettings.class,  true);

    public static class DeputySettings extends ConfigSection {

        @Entry(key = "deputy-group")
        public final ConfigEntry<String> deputyGroup = new ConfigEntry<>();

        @Entry(key = "candidate-groups")
        public final ConfigEntry<List<String>> candidateGroups = new ConfigEntry<>();

        @Entry(key = "default-duration")
        public final ConfigEntry<Long> defaultDuration = new ConfigEntry<>(duration -> DurationParser.fromString(duration.toString()));

        @Entry(key = "maximum-duration")
        public final ConfigEntry<Long> maximumDuration = new ConfigEntry<>(duration -> DurationParser.fromString(duration.toString()));

        @Entry(key = "expiry-notification-time")
        public final ConfigEntry<Long> expiryNotificationTime = new ConfigEntry<>(duration -> DurationParser.fromString(duration.toString()));
    }


}
