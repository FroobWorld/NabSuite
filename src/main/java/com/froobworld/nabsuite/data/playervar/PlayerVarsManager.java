package com.froobworld.nabsuite.data.playervar;

import com.froobworld.nabsuite.data.DataLoader;
import com.froobworld.nabsuite.data.DataSaver;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.UUID;
import java.util.regex.Pattern;

public class PlayerVarsManager implements Listener {
    private static final Pattern fileNamePattern = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.json$");
    final DataSaver playerVarsSaver;
    private final BiMap<UUID, PlayerVars> playerVarsMap = HashBiMap.create();
    private final File directory;

    public PlayerVarsManager(Plugin plugin) {
        directory = new File(plugin.getDataFolder(),  "player-vars/");
        playerVarsSaver = new DataSaver(plugin, 1);
        playerVarsMap.putAll(DataLoader.loadAll(
                directory,
                fileName -> fileNamePattern.matcher(fileName.toLowerCase()).matches(),
                bytes -> PlayerVars.fromJsonString(this, new String(bytes)),
                (fileName, vars) -> vars.getUuid()
        ));
        playerVarsSaver.start();
        playerVarsSaver.addDataType(PlayerVars.class, vars -> vars.toJsonString().getBytes(), vars -> new File(directory, vars.getUuid().toString() + ".json"));
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void shutdown() {
        playerVarsSaver.stop();
    }

    public PlayerVars getVars(UUID uuid) {
        if (!playerVarsMap.containsKey(uuid)) {
            playerVarsMap.put(uuid, new PlayerVars(this, uuid));
        }
        return playerVarsMap.get(uuid);
    }

}
