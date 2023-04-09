package com.froobworld.nabsuite.data.identity;

import com.froobworld.nabsuite.NabSuite;
import com.froobworld.nabsuite.data.DataLoader;
import com.froobworld.nabsuite.data.DataSaver;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.io.File;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PlayerIdentityManager implements Listener {
    private static final Pattern fileNamePattern = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.json$");
    private final DataSaver playerIdentitySaver;
    private final BiMap<UUID, PlayerIdentity> playerIdentityMap = Maps.synchronizedBiMap(HashBiMap.create());
    private final File directory;

    public PlayerIdentityManager(NabSuite nabSuite) {
        directory = new File(nabSuite.getDataFolder(),  "player-identity/");
        playerIdentitySaver = new DataSaver(nabSuite, 1);
        playerIdentityMap.putAll(DataLoader.loadAll(
                directory,
                fileName -> fileNamePattern.matcher(fileName.toLowerCase()).matches(),
                bytes -> PlayerIdentity.fromJsonString(new String(bytes)),
                (fileName, playerIdentity) -> playerIdentity.getUuid()
        ));
        playerIdentitySaver.start();
        playerIdentitySaver.addDataType(PlayerIdentity.class, playerIdentity -> playerIdentity.toJsonString().getBytes(), playerIdentity -> new File(directory, playerIdentity.getUuid().toString() + ".json"));
        Bukkit.getPluginManager().registerEvents(this, nabSuite);
    }

    public PlayerIdentity getPlayerIdentity(UUID uuid) {
        return playerIdentityMap.get(uuid);
    }

    public PlayerIdentity getPlayerIdentity(Player player) {
        return getPlayerIdentity(player.getUniqueId());
    }

    public Set<PlayerIdentity> getPlayerIdentity(String name) {
        return playerIdentityMap.values()
                .stream()
                .filter(playerIdentity -> playerIdentity.getLastName().equalsIgnoreCase(name))
                .collect(Collectors.toSet());
    }

    public Set<PlayerIdentity> getAllPlayerIdentities() {
        return playerIdentityMap.values();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerJoin(AsyncPlayerPreLoginEvent event) {
        PlayerIdentity playerIdentity = playerIdentityMap.computeIfAbsent(event.getUniqueId(), k -> {
            PlayerIdentity newPlayerIdentity = new PlayerIdentity(event.getUniqueId(), event.getName(), Lists.newArrayList(event.getName()));
            playerIdentitySaver.scheduleSave(newPlayerIdentity);
            return newPlayerIdentity;
        });
        playerIdentity.lock.writeLock().lock();
        try {
            if (playerIdentity.getPreviousNames().size() == 0 || !playerIdentity.getPreviousNames().get(playerIdentity.getPreviousNames().size() - 1).equals(playerIdentity.getLastName())) {
                playerIdentity.getPreviousNames().add(playerIdentity.getLastName());
            }
            if (!playerIdentity.getLastName().equals(event.getName())) {
                playerIdentity.setLastName(event.getName());
                playerIdentity.addPreviousName(event.getName());
                playerIdentitySaver.scheduleSave(playerIdentity);
            }
        } finally {
            playerIdentity.lock.writeLock().unlock();
        }
    }

}
