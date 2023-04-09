package com.froobworld.nabsuite.modules.basics.player;

import com.froobworld.nabsuite.data.DataLoader;
import com.froobworld.nabsuite.data.DataSaver;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public class PlayerDataManager implements Listener {
    private static final Pattern fileNamePattern = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.json$");
    private final BasicsModule basicsModule;
    protected final DataSaver playerDataSaver;
    private final BiMap<UUID, PlayerData> playerDataMap = Maps.synchronizedBiMap(HashBiMap.create());
    private final File directory;
    private final IgnoreManager ignoreManager;
    private final FriendManager friendManager;

    public PlayerDataManager(BasicsModule basicsModule) {
        this.basicsModule = basicsModule;
        directory = new File(basicsModule.getDataFolder(), "playerdata/");
        playerDataSaver = new DataSaver(basicsModule.getPlugin(), 1200);
        playerDataMap.putAll(DataLoader.loadAll(
                directory,
                fileName -> fileNamePattern.matcher(fileName.toLowerCase()).matches(),
                bytes -> PlayerData.fromJsonString(this, new String(bytes)),
                (fileName, playerData) -> playerData.getUuid()
        ));
        playerDataSaver.start();
        playerDataSaver.addDataType(PlayerData.class, playerData -> playerData.toJsonString().getBytes(), playerData -> new File(directory, playerData.getUuid().toString() + ".json"));
        Bukkit.getPluginManager().registerEvents(this, basicsModule.getPlugin());
        ignoreManager = new IgnoreManager(basicsModule);
        friendManager = new FriendManager(this);
    }

    public void shutdown() {
        playerDataSaver.stop();
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.get(uuid);
    }

    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    public Set<PlayerData> getPlayerData(String name) {
        Set<PlayerData> playerDataSet = new HashSet<>();
        for (PlayerIdentity playerIdentity : basicsModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(name)) {
            PlayerData playerData = getPlayerData(playerIdentity.getUuid());
            if (playerData != null) {
                playerDataSet.add(playerData);
            }
        }
        return playerDataSet;
    }

    public IgnoreManager getIgnoreManager() {
        return ignoreManager;
    }

    public FriendManager getFriendManager() {
        return friendManager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        playerDataMap.computeIfAbsent(player.getUniqueId(), k -> {
            PlayerData playerData = new PlayerData(this, player);
            playerDataSaver.scheduleSave(playerData);
            return playerData;
        }).updateLastPlayedTime();
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        getPlayerData(event.getPlayer()).updateLastPlayedTime();
    }

}
