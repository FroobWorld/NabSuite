package com.froobworld.nabsuite.modules.basics.teleport.warp;

import com.froobworld.nabsuite.data.DataLoader;
import com.froobworld.nabsuite.data.DataSaver;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Set;
import java.util.regex.Pattern;

public class WarpManager {
    public static final Pattern warpNamePattern = Pattern.compile("^[a-zA-z0-9-_]+$");
    private static final Pattern fileNamePattern = Pattern.compile("^[a-zA-z0-9-_]+\\.json$");
    protected final DataSaver warpSaver;
    private final BiMap<String, Warp> warpMap = HashBiMap.create();
    private final File directory;

    public WarpManager(BasicsModule basicsModule) {
        directory = new File(basicsModule.getDataFolder(), "warps/");
        warpSaver = new DataSaver(basicsModule.getPlugin(), 1200);
        warpMap.putAll(DataLoader.loadAll(
                directory,
                fileName -> fileNamePattern.matcher(fileName.toLowerCase()).matches(),
                bytes -> Warp.fromJsonString(this, new String(bytes)),
                (fileName, warp) -> warp.getName().toLowerCase()
        ));
        warpSaver.start();
        warpSaver.addDataType(Warp.class, warp -> warp.toJsonString().getBytes(), warp -> new File(directory, warp.getName() + ".json"));
    }

    public void shutdown() {
        warpSaver.stop();
    }

    public Warp createWarp(String name, Player creator) {
        if (!warpNamePattern.matcher(name).matches()) {
            throw new IllegalArgumentException("Name does not match pattern: " + warpNamePattern);
        }
        if (warpMap.containsKey(name.toLowerCase())) {
            throw new IllegalStateException("Warp with that name already exists");
        }
        Warp warp = new Warp(this, name, creator.getLocation(), creator.getUniqueId());
        warpMap.put(name.toLowerCase(), warp);
        warpSaver.scheduleSave(warp);
        return warp;
    }

    public void deleteWarp(Warp warp) {
        warpMap.remove(warp.getName().toLowerCase());
        warpSaver.scheduleDeletion(warp);
    }

    public Warp getWarp(String name) {
        return warpMap.get(name.toLowerCase());
    }

    public Set<Warp> getWarps() {
        return warpMap.values();
    }

}
