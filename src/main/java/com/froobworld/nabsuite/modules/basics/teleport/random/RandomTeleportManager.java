package com.froobworld.nabsuite.modules.basics.teleport.random;

import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.google.common.collect.Sets;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class RandomTeleportManager {
    private final BasicsModule basicsModule;
    private final NamespacedKey allowancePdcKey;
    private final NamespacedKey timestampPdcKey;
    private final long regenerationFrequency;
    private final int maxRtps;
    private final RandomTeleporter randomTeleporter;
    private final Set<UUID> inProgress = Sets.newConcurrentHashSet();

    public RandomTeleportManager(BasicsModule basicsModule) {
        this.basicsModule = basicsModule;
        allowancePdcKey = NamespacedKey.fromString("rtp-allowance", basicsModule.getPlugin());
        timestampPdcKey = NamespacedKey.fromString("rtp-regeneration-timestamp", basicsModule.getPlugin());
        randomTeleporter = new RandomTeleporter(basicsModule);
        regenerationFrequency = TimeUnit.MINUTES.toMillis(basicsModule.getConfig().randomTeleport.regenerationFrequency.get());
        maxRtps = basicsModule.getConfig().randomTeleport.maxRandomTeleports.get();
    }

    public int getMaxRandomTeleportAllowance() {
        return maxRtps;
    }

    public int getRandomTeleportAllowance(Player player) {
        updateAllowance(player);
        return Math.min(player.getPersistentDataContainer().getOrDefault(allowancePdcKey, PersistentDataType.INTEGER, maxRtps), maxRtps);
    }

    public long getTimeUntilNextRandomTeleport(Player player) {
        updateAllowance(player);
        return getRegenerationTimestamp(player) + regenerationFrequency - System.currentTimeMillis();
    }

    public CompletableFuture<Location> randomTeleport(Player player) {
        inProgress.add(player.getUniqueId());
        return randomTeleporter.attemptFindLocation(player.getWorld())
                .thenComposeAsync(location -> {
                    try {
                        if (location == null) {
                            return CompletableFuture.completedFuture(null);
                        }
                        if (getRandomTeleportAllowance(player) == maxRtps) {
                            player.getPersistentDataContainer().set(timestampPdcKey, PersistentDataType.LONG, System.currentTimeMillis());
                        }
                        player.getPersistentDataContainer().set(allowancePdcKey, PersistentDataType.INTEGER, getRandomTeleportAllowance(player) - 1);
                        return basicsModule.getPlayerTeleporter().teleportAsync(player, location);
                    } finally {
                        inProgress.remove(player.getUniqueId());
                    }
                }, runnable -> {
                    if (basicsModule.getPlugin().getHookManager().getSchedulerHook().runEntityTaskAsap(runnable, runnable, player) == null) {
                        inProgress.remove(player.getUniqueId());
                    }
                }).exceptionally(ex -> {
                    ex.printStackTrace();
                    inProgress.remove(player.getUniqueId());
                    return null;
                });
    }

    public boolean isTeleportInProgress(Player player) {
        return inProgress.contains(player.getUniqueId());
    }

    private void updateAllowance(Player player) {
        if (player.getPersistentDataContainer().has(allowancePdcKey)) {
            @SuppressWarnings("ConstantConditions")
            int allowance = player.getPersistentDataContainer().get(allowancePdcKey, PersistentDataType.INTEGER);
            if (allowance < maxRtps) {
                int owedRtps = (int) ((System.currentTimeMillis() - getRegenerationTimestamp(player)) / regenerationFrequency);
                allowance = Math.min(allowance + owedRtps, maxRtps);
                long newRegenTimestamp = getRegenerationTimestamp(player) + owedRtps * regenerationFrequency;
                player.getPersistentDataContainer().set(allowancePdcKey, PersistentDataType.INTEGER, allowance);
                player.getPersistentDataContainer().set(timestampPdcKey, PersistentDataType.LONG, newRegenTimestamp);
            }
        }
    }

    private long getRegenerationTimestamp(Player player) {
        return player.getPersistentDataContainer().getOrDefault(timestampPdcKey, PersistentDataType.LONG, -1L);
    }

}
