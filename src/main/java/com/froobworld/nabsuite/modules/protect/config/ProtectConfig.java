package com.froobworld.nabsuite.modules.protect.config;

import com.froobworld.nabconfiguration.ConfigEntry;
import com.froobworld.nabconfiguration.NabConfiguration;
import com.froobworld.nabconfiguration.annotations.Entry;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import org.bukkit.Material;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Entry(key = "lockable-materials")
    public final ConfigEntry<Set<Material>> lockableMaterials = new ConfigEntry<>(object -> {
        Set<Material> materials = new HashSet<>();
        if (object == null) {
            return materials;
        }
        //noinspection unchecked
        for (String material : (List<String>) object) {
            materials.add(Material.getMaterial(material));
        }
        return materials;
    });


}
