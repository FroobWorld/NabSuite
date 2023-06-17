package com.froobworld.nabsuite.modules.mechs.config;

import com.froobworld.nabconfiguration.*;
import com.froobworld.nabconfiguration.annotations.Entry;
import com.froobworld.nabconfiguration.annotations.Section;
import com.froobworld.nabconfiguration.annotations.SectionMap;
import com.froobworld.nabsuite.modules.mechs.MechsModule;
import org.bukkit.World;
import org.bukkit.generator.WorldInfo;

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

    @SectionMap(key = "world-border", defaultKey = "default")
    public ConfigSectionMap<World, WorldBorderSettings> worldBorder = new ConfigSectionMap<>(WorldInfo::getName, WorldBorderSettings.class,  true);

    public static class WorldBorderSettings extends ConfigSection {

        @Entry(key = "use-border")
        public final ConfigEntry<Boolean> useBorder = new ConfigEntry<>();

        @Entry(key = "centre-x")
        public final ConfigEntry<Integer> centreX = ConfigEntries.integerEntry();

        @Entry(key = "centre-z")
        public final ConfigEntry<Integer> centreZ = ConfigEntries.integerEntry();

        @Entry(key = "radius-x")
        public final ConfigEntry<Integer> radiusX = ConfigEntries.integerEntry();

        @Entry(key = "radius-z")
        public final ConfigEntry<Integer> radiusZ = ConfigEntries.integerEntry();

        @Entry(key = "border-region-radius-x")
        public final ConfigEntry<Integer> borderRegionRadiusX = ConfigEntries.integerEntry();

        @Entry(key = "border-region-radius-z")
        public final ConfigEntry<Integer> borderRegionRadiusZ = ConfigEntries.integerEntry();

    }


}
