package com.froobworld.nabsuite.modules.protect.config;

import com.froobworld.nabconfiguration.ConfigEntry;
import com.froobworld.nabconfiguration.ConfigSection;
import com.froobworld.nabconfiguration.NabConfiguration;
import com.froobworld.nabconfiguration.annotations.Entry;
import com.froobworld.nabconfiguration.annotations.Section;
import com.froobworld.nabsuite.modules.protect.ProtectModule;

import java.io.File;

public class ProtectConfig extends NabConfiguration {
    private static final int CONFIG_VERSION = 2;

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

    @Section(key = "area-near")
    public final AreaNearSettings areaNearSettings = new AreaNearSettings();

    public static class AreaNearSettings extends ConfigSection {

        @Entry(key = "radius")
        public ConfigEntry<Integer> radius = new ConfigEntry<>();

        @Entry(key = "max-radius")
        public ConfigEntry<Integer> maxRadius = new ConfigEntry<>();

        @Entry(key = "limit")
        public ConfigEntry<Integer> limit = new ConfigEntry<>();
    }
}
