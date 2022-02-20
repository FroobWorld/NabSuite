package com.froobworld.nabsuite.modules.admin.xray;

import com.froobworld.nabsuite.data.DataLoader;
import com.froobworld.nabsuite.data.DataSaver;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

public class OreStatsManager {
    private static final Pattern fileNamePattern = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.json$");
    protected final DataSaver dataSaver;
    private final Map<UUID, PlayerOreStatsData> dataMap = new HashMap<>();
    private final File directory;

    public OreStatsManager(AdminModule adminModule) {
        directory = new File(adminModule.getDataFolder(), "orestats/");
        dataSaver = new DataSaver(adminModule.getPlugin(), 1200);
        dataMap.putAll(DataLoader.loadAll(
                directory,
                fileName -> fileNamePattern.matcher(fileName.toLowerCase()).matches(),
                bytes -> PlayerOreStatsData.fromJsonString(this, new String(bytes)),
                (fileName, data) -> data.getUuid()
        ));
        dataSaver.start();
        dataSaver.addDataType(PlayerOreStatsData.class, data -> data.toJsonString().getBytes(), data -> new File(directory, data.getUuid().toString() + ".json"));
        Bukkit.getPluginManager().registerEvents(new OreMonitor(adminModule, this), adminModule.getPlugin());
    }

    public PlayerOreStatsData getOreStatsData(UUID uuid) {
        if (!dataMap.containsKey(uuid)) {
            PlayerOreStatsData data = new PlayerOreStatsData(this, uuid);
            dataMap.put(uuid, data);
            dataSaver.scheduleSave(data);
        }
        return dataMap.get(uuid);
    }

    public Collection<PlayerOreStatsData> getOreStatsData() {
        return dataMap.values();
    }

    public void shutdown() {
        dataSaver.stop();
    }
}
