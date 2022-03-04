package com.froobworld.nabsuite.modules.basics.teleport;

import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

public class BackManager {
    private final NamespacedKey backLocationKey;
    private final Map<UUID, Set<NamespacedKey>> backExemptions = new HashMap<>();

    public BackManager(BasicsModule basicsModule) {
        backLocationKey = new NamespacedKey(basicsModule.getPlugin(), "back-location");
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
            private void onPlayerDeath(PlayerDeathEvent event) {
                setBackLocation(event.getPlayer(), event.getPlayer().getLocation());
            }
        }, basicsModule.getPlugin());
    }

    public Location getBackLocation(Player player) {
        BackLocation backLocation = player.getPersistentDataContainer().get(backLocationKey, BackLocation.DATA_TYPE);
        if (backLocation != null) {
            return backLocation.location;
        }
        return null;
    }

    public void setBackLocation(Player player, Location location) {
        if (backExemptions.containsKey(player.getUniqueId()) && !backExemptions.get(player.getUniqueId()).isEmpty()) {
            return;
        }
        player.getPersistentDataContainer().set(backLocationKey, BackLocation.DATA_TYPE, new BackLocation(location));
    }

    public void addBackExemption(Player player, NamespacedKey reasonKey) {
        backExemptions.computeIfAbsent(player.getUniqueId(), p -> new HashSet<>()).add(reasonKey);
    }

    public void removeBackExemption(Player player, NamespacedKey reasonKey) {
        backExemptions.computeIfAbsent(player.getUniqueId(), p -> new HashSet<>()).remove(reasonKey);
    }

    private static class BackLocation {
        private static final SimpleDataSchema<BackLocation> SCHEMA = new SimpleDataSchema.Builder<BackLocation>()
                .addField("location", SchemaEntries.locationEntry(
                        backLocation -> backLocation.location,
                        (backLocation, location) -> backLocation.location = location
                ))
                .build();
        private static final PersistentDataType<String, BackLocation> DATA_TYPE = new PersistentDataType<>() {
            @Override
            public @NotNull Class<String> getPrimitiveType() {
                return String.class;
            }

            @Override
            public @NotNull Class<BackLocation> getComplexType() {
                return BackLocation.class;
            }

            @SuppressWarnings("NullableProblems")
            @Override
            public String toPrimitive(@NotNull BackLocation backLocation, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
                try {
                    return BackLocation.SCHEMA.toJsonString(backLocation);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @SuppressWarnings("NullableProblems")
            @Override
            public BackLocation fromPrimitive(@NotNull String s, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
                try {
                    BackLocation backLocation = new BackLocation();
                    BackLocation.SCHEMA.populateFromJsonString(backLocation, s);
                    return backLocation;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };

        private Location location;

        private BackLocation() {}

        public BackLocation(Location location) {
            this.location = location;
        }

    }

}
