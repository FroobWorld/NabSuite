package com.froobworld.nabsuite.modules.mechs.config;

import com.froobworld.nabconfiguration.ConfigEntries;
import com.froobworld.nabconfiguration.ConfigEntry;
import com.froobworld.nabconfiguration.ConfigSection;
import com.froobworld.nabconfiguration.NabConfiguration;
import com.froobworld.nabconfiguration.annotations.Entry;
import com.froobworld.nabconfiguration.annotations.Section;
import com.froobworld.nabsuite.modules.mechs.MechsModule;

import java.io.File;

public class MechsConfig extends NabConfiguration {
    private static final int CONFIG_VERSION = 1;

    public MechsConfig(MechsModule mechsModule) {
        super(
                new File(mechsModule.getDataFolder(), "config.yml"),
                () -> mechsModule.getResource("config.yml"),
                i -> mechsModule.getResource("config-patches/" + i + ".patch"),
                CONFIG_VERSION
        );
    }

    @Section(key = "view-distance")
    public ViewDistanceSettings viewDistance = new ViewDistanceSettings();

    public static class ViewDistanceSettings extends ConfigSection {

        @Entry(key = "capped-view-distance")
        public ConfigEntry<Integer> cappedViewDistance = ConfigEntries.integerEntry();

        @Entry(key = "uncapped-view-distance")
        public ConfigEntry<Integer> uncappedViewDistance = ConfigEntries.integerEntry();

    }


}
