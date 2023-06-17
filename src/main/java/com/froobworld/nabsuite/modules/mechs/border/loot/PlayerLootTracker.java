package com.froobworld.nabsuite.modules.mechs.border.loot;

import com.froobworld.nabsuite.data.DataLoader;
import com.froobworld.nabsuite.data.DataSaver;
import com.froobworld.nabsuite.modules.mechs.MechsModule;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.UUID;
import java.util.regex.Pattern;

public class PlayerLootTracker implements Listener {
    private static final Pattern fileNamePattern = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.json$");
    protected final DataSaver lootTrackerDataSaver;
    private final BiMap<UUID, LootTrackerData> lootTrackerDataMap = HashBiMap.create();
    private final File directory;

    public PlayerLootTracker(MechsModule mechsModule) {
        directory = new File(mechsModule.getDataFolder(), "loottracker/");
        lootTrackerDataSaver = new DataSaver(mechsModule.getPlugin(), 1200);
        lootTrackerDataMap.putAll(DataLoader.loadAll(
                directory,
                fileName -> fileNamePattern.matcher(fileName.toLowerCase()).matches(),
                bytes -> LootTrackerData.fromJsonString(this, new String(bytes)),
                (fileName, lootTrackerData) -> lootTrackerData.getUuid()
        ));
        lootTrackerDataSaver.start();
        lootTrackerDataSaver.addDataType(LootTrackerData.class, lootTrackerData -> lootTrackerData.toJsonString().getBytes(), lootTrackerData -> new File(directory, lootTrackerData.getUuid().toString() + ".json"));
        Bukkit.getPluginManager().registerEvents(this, mechsModule.getPlugin());
    }

    public void shutdown() {
        lootTrackerDataSaver.stop();
    }

    public LootTrackerData getLootTrackerData(Player player) {
        return lootTrackerDataMap.compute(player.getUniqueId(), (key, data) -> {
            if (data == null) {
                data = new LootTrackerData(this, player.getUniqueId());
                lootTrackerDataSaver.scheduleSave(data);
            }
            return data;
        });
    }

}
