package com.froobworld.nabsuite.modules.basics.config;

import com.froobworld.nabconfiguration.ConfigEntries;
import com.froobworld.nabconfiguration.ConfigEntry;
import com.froobworld.nabconfiguration.ConfigSection;
import com.froobworld.nabconfiguration.NabConfiguration;
import com.froobworld.nabconfiguration.annotations.Entry;
import com.froobworld.nabconfiguration.annotations.Section;
import com.froobworld.nabsuite.modules.basics.BasicsModule;

import java.io.File;
import java.util.List;

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

    @Section(key = "messages")
    public final Messages messages = new Messages();

    public static class Messages extends ConfigSection {

        @Entry(key = "motd")
        public final ConfigEntry<List<String>> motd = ConfigEntries.stringListEntry();

        @Entry(key = "rules")
        public final ConfigEntry<List<String>> rules = ConfigEntries.stringListEntry();

    }

}
