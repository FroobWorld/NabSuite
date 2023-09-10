package com.froobworld.nabsuite.modules.admin.config;

import com.froobworld.nabconfiguration.ConfigEntries;
import com.froobworld.nabconfiguration.ConfigEntry;
import com.froobworld.nabconfiguration.ConfigSection;
import com.froobworld.nabconfiguration.NabConfiguration;
import com.froobworld.nabconfiguration.annotations.Entry;
import com.froobworld.nabconfiguration.annotations.Section;
import com.froobworld.nabsuite.modules.admin.AdminModule;

import java.io.File;
import java.util.Date;
import java.util.List;

public class AdminConfig extends NabConfiguration {
    private static final int CONFIG_VERSION = 3;

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


}
