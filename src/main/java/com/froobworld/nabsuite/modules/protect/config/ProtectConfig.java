package com.froobworld.nabsuite.modules.protect.config;

import com.froobworld.nabconfiguration.ConfigEntry;
import com.froobworld.nabconfiguration.NabConfiguration;
import com.froobworld.nabconfiguration.annotations.Entry;
import com.froobworld.nabsuite.modules.protect.ProtectModule;

import java.io.File;

public class ProtectConfig extends NabConfiguration {
    private static final int CONFIG_VERSION = 1;

    public ProtectConfig(ProtectModule protectModule) {
        super(
                new File(protectModule.getDataFolder(), "config.yml"),
                () -> protectModule.getResource("config.yml"),
                i -> protectModule.getResource("config-patches/" + i + ".patch"),
                CONFIG_VERSION
        );
    }

    @Entry(key = "map-url")
    public ConfigEntry<String> mapReviewUrl = new ConfigEntry<>();


}
