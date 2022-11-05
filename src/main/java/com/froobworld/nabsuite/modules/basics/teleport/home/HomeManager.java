package com.froobworld.nabsuite.modules.basics.teleport.home;

import com.froobworld.nabsuite.data.DataLoader;
import com.froobworld.nabsuite.data.DataSaver;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public class HomeManager {
    public static final Pattern homeNamePattern = Pattern.compile("^[a-zA-z0-9-_]+$");
    private static final Pattern fileNamePattern = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.json$");
    private final BasicsModule basicsModule;
    protected final DataSaver homesSaver;
    private final BiMap<UUID, Homes> homesMap = HashBiMap.create();
    private final File directory;

    public HomeManager(BasicsModule basicsModule) {
        this.basicsModule = basicsModule;
        directory = new File(basicsModule.getDataFolder(), "homes/");
        homesSaver = new DataSaver(basicsModule.getPlugin(), 1200);
        homesMap.putAll(DataLoader.loadAll(
                directory,
                fileName -> fileNamePattern.matcher(fileName.toLowerCase()).matches(),
                bytes -> Homes.fromJsonString(this, new String(bytes)),
                (fileName, homes) -> homes.getUuid()
        ));
        homesSaver.start();
        homesSaver.addDataType(Homes.class, homes -> homes.toJsonString().getBytes(), homes -> new File(directory, homes.getUuid().toString() + ".json"));
    }

    public void shutdown() {
        homesSaver.stop();
    }

    public Set<Homes> getAllHomes() {
        return homesMap.values();
    }

    public Homes getHomes(Player player) {
        if (!homesMap.containsKey(player.getUniqueId())) {
            Homes homes = new Homes(this, player.getUniqueId());
            homesMap.put(player.getUniqueId(), homes);
            homesSaver.scheduleSave(homes);
        }
        return homesMap.get(player.getUniqueId());
    }

    public Home createHome(Player player, String name) {
        Home home = new Home(name, player.getLocation());
        getHomes(player).addHome(home);
        return home;
    }

    public void deleteHome(Player player, Home home) {
        getHomes(player).removeHome(home);
    }

    public int getMaxHomes(Player player) {
        int maxSearch = basicsModule.getConfig().maxHomePermissionCheck.get();
        int maxHomes = 1;
        for (int i = 1; i <= maxSearch; i++) {
            if (player.hasPermission("nabsuite.maxhomes." + i)) {
                maxHomes = i;
            }
        }
        return maxHomes;
    }

}
