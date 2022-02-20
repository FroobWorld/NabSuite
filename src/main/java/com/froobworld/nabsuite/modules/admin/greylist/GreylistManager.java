package com.froobworld.nabsuite.modules.admin.greylist;

import com.froobworld.nabsuite.data.DataLoader;
import com.froobworld.nabsuite.data.DataSaver;
import com.froobworld.nabsuite.modules.admin.AdminModule;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

public class GreylistManager {
    private static final Pattern fileNamePattern = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.json$");
    protected final DataSaver dataSaver;
    private final Map<UUID, PlayerGreylistData> dataMap = new HashMap<>();
    private final File directory;
    private final GreylistEnforcer greylistEnforcer;

    public GreylistManager(AdminModule adminModule) {
        directory = new File(adminModule.getDataFolder(), "greylist/");
        dataSaver = new DataSaver(adminModule.getPlugin(), 1200);
        dataMap.putAll(DataLoader.loadAll(
                directory,
                fileName -> fileNamePattern.matcher(fileName.toLowerCase()).matches(),
                bytes -> PlayerGreylistData.fromJsonString(this, new String(bytes)),
                (fileName, data) -> data.getUuid()
        ));
        dataSaver.start();
        dataSaver.addDataType(PlayerGreylistData.class, data -> data.toJsonString().getBytes(), data -> new File(directory, data.getUuid().toString() + ".json"));
        greylistEnforcer = new GreylistEnforcer(adminModule, this);
    }

    public PlayerGreylistData getGreylistData(UUID uuid) {
        if (!dataMap.containsKey(uuid)) {
            PlayerGreylistData data = new PlayerGreylistData(this, uuid, true, false);
            dataMap.put(uuid, data);
            dataSaver.scheduleSave(data);
        }
        return dataMap.get(uuid);
    }

    public Collection<PlayerGreylistData> getGreylistData() {
        return dataMap.values();
    }

    public void shutdown() {
        dataSaver.stop();
    }

    public GreylistEnforcer getGreylistEnforcer() {
        return greylistEnforcer;
    }
}
