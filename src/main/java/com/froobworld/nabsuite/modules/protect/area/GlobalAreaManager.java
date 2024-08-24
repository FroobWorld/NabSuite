package com.froobworld.nabsuite.modules.protect.area;

import com.froobworld.nabsuite.data.DataLoader;
import com.froobworld.nabsuite.data.DataSaver;
import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GlobalAreaManager {
    private static final SimpleDataSchema<GlobalAreaManager> SCHEMA = new SimpleDataSchema.Builder<GlobalAreaManager>()
            .addField("global-areas", SchemaEntries.mapEntry(
                    globalAreaManager -> globalAreaManager.globalAreaMap,
                    (globalAreaManager, globalAreaMap) -> globalAreaManager.globalAreaMap = globalAreaMap,
                    (jsonReader, globalAreaManager) -> GlobalArea.fromJsonReader(globalAreaManager, globalAreaManager.protectModule.getPlugin().getUserManager(), jsonReader),
                    GlobalArea.SCHEMA::write,
                    HashMap::new,
                    UUID::toString,
                    UUID::fromString
            ))
            .build();
    private final ProtectModule protectModule;
    private Map<UUID, GlobalArea> globalAreaMap = new HashMap<>();
    private final File globalAreaFile;
    private final DataSaver dataSaver;

    public GlobalAreaManager(ProtectModule protectModule) {
        this.protectModule = protectModule;
        globalAreaFile = new File(protectModule.getDataFolder(), "global-areas.json");
        dataSaver = new DataSaver(protectModule.getPlugin(), 1200);
        dataSaver.addDataType(GlobalAreaManager.class, globalAreaManager -> globalAreaManager.toJsonString().getBytes(), g -> globalAreaFile);
        dataSaver.start();
        load();
    }

    public Set<AreaLike> getTopMostAreasAtLocation(Location location) {
        return getGlobalArea(location.getWorld()).getTopMostAreas(location);
    }

    public GlobalArea getGlobalArea(World world) {
        GlobalArea globalArea = globalAreaMap.get(world.getUID());
        if (globalArea == null) {
            globalArea = new GlobalArea(this, protectModule.getPlugin().getUserManager());
            globalAreaMap.put(world.getUID(), globalArea);
            scheduleSave();
        }
        return globalArea;
    }

    public void createGlobalSubArea(World world, String name, int bound1, int bound2) {
        GlobalArea globalArea = getGlobalArea(world);
        GlobalSubArea globalSubArea = new GlobalSubArea(globalArea, name, bound1, bound2);
        globalArea.addSubArea(globalSubArea);
    }

    public void removeGlobalSubArea(World world, GlobalSubArea globalSubArea) {
        getGlobalArea(world).removeSubArea(globalSubArea);
    }

    public GlobalSubArea getGlobalSubArea(World world, String name) {
        for (GlobalSubArea globalSubArea : getGlobalArea(world).getSubAreas()) {
            if (globalSubArea.getName().equalsIgnoreCase(name)) {
                return globalSubArea;
            }
        }
        return null;
    }

    void scheduleSave() {
        dataSaver.scheduleSave(this);
    }

    private void load() {
        if (!globalAreaFile.exists()) {
            return;
        }
        DataLoader.load(globalAreaFile, bytes -> populateFromJsonString(new String(bytes)));
    }

    void shutdown() {
        dataSaver.stop();
    }

    private String toJsonString() {
        try {
            return SCHEMA.toJsonString(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private GlobalAreaManager populateFromJsonString(String jsonString) {
        try {
            SCHEMA.populateFromJsonString(this, jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

}
