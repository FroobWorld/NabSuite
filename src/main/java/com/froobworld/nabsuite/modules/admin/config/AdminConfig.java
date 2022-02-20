package com.froobworld.nabsuite.modules.admin.config;

import com.froobworld.nabconfiguration.ConfigEntries;
import com.froobworld.nabconfiguration.ConfigEntry;
import com.froobworld.nabconfiguration.NabConfiguration;
import com.froobworld.nabconfiguration.annotations.Entry;
import com.froobworld.nabsuite.modules.admin.AdminModule;

import java.io.File;
import java.util.List;

public class AdminConfig extends NabConfiguration {
    private static final int CONFIG_VERSION = 1;

    public AdminConfig(AdminModule adminModule) {
        super(
                new File(adminModule.getDataFolder(), "config.yml"),
                () -> adminModule.getResource("config.yml"),
                i -> adminModule.getResource("config-patches/" + i + ".patch"),
                CONFIG_VERSION
        );
    }

    @Entry(key = "ban-appeal-url")
    public final ConfigEntry<String> banAppealUrl = new ConfigEntry<>();

    @Entry(key = "jail-command-whitelist")
    public final ConfigEntry<List<String>> jailCommandWhitelist = ConfigEntries.stringListEntry();


}
