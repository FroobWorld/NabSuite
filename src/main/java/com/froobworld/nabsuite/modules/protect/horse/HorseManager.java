package com.froobworld.nabsuite.modules.protect.horse;

import com.froobworld.nabsuite.data.DataLoader;
import com.froobworld.nabsuite.data.DataSaver;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

public class HorseManager {
    public static final String EDIT_ALL_HORSES_PERMISSION = "nabsuite.editallhorses";
    private static final Pattern fileNamePattern = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.json$");
    private final ProtectModule protectModule;
    protected final DataSaver horseSaver;
    private final BiMap<UUID, Horse> horseMap = HashBiMap.create();
    private final File directory;
    private final HorseNotificationManager horseNotificationManager;

    public HorseManager(ProtectModule protectModule) {
        this.protectModule = protectModule;
        directory = new File(protectModule.getDataFolder(), "horses/");
        horseSaver = new DataSaver(protectModule.getPlugin(), 1200);
        horseMap.putAll(DataLoader.loadAll(
                directory,
                fileName -> fileNamePattern.matcher(fileName.toLowerCase()).matches(),
                bytes -> Horse.fromJsonString(this, protectModule.getPlugin().getUserManager(), new String(bytes)),
                (fileName, horse) -> horse.getUuid()
        ));
        horseSaver.start();
        horseSaver.addDataType(Horse.class, horse -> horse.toJsonString().getBytes(), horse -> new File(directory, horse.getUuid().toString() + ".json"));
        Bukkit.getPluginManager().registerEvents(new HorseClaimEnforcer(this), protectModule.getPlugin());
        horseNotificationManager = new HorseNotificationManager();
    }

    public void shutdown() {
        horseSaver.stop();
    }

    public Horse getHorse(UUID uuid) {
        return horseMap.get(uuid);
    }

    public void deleteHorse(Horse horse) {
        horseMap.remove(horse.getUuid());
        horseSaver.scheduleDeletion(horse);
    }

    public Horse protectHorse(UUID uuid, UUID owner) {
        if (!horseMap.containsKey(uuid)) {
            Horse horse = new Horse(this, protectModule.getPlugin().getUserManager(), uuid, owner);
            horseMap.put(uuid, horse);
            horseSaver.scheduleSave(horse);
            return horse;
        }
        return null;
    }

    public HorseNotificationManager getHorseNotificationManager() {
        return horseNotificationManager;
    }
}
