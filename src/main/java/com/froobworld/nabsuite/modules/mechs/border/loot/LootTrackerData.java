package com.froobworld.nabsuite.modules.mechs.border.loot;

import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LootTrackerData {
    private static final SimpleDataSchema<LootTrackerData> SCHEMA = new SimpleDataSchema.Builder<LootTrackerData>()
            .addField(
                    "uuid",
                    SchemaEntries.uuidEntry(
                            lootTrackerData -> lootTrackerData.uuid,
                            (lootTrackerData, uuid) -> lootTrackerData.uuid = uuid
                    )
            )
            .addField(
                    "looted-locations",
                    SchemaEntries.setEntry(
                            lootTrackerData -> lootTrackerData.lootedLocations,
                            (lootTrackerData, locations) -> lootTrackerData.lootedLocations = locations,
                            (jsonReader, lootTrackerData) -> {
                                String[] locationComponents = jsonReader.nextString().split(";");
                                World world = Bukkit.getWorld(UUID.fromString(locationComponents[0]));
                                int x = Integer.parseInt(locationComponents[1]);
                                int y = Integer.parseInt(locationComponents[2]);
                                int z = Integer.parseInt(locationComponents[3]);
                                return new Location(world, x, y, z);
                            },
                            (location, jsonWriter) -> {
                                String locationString = location.getWorld().getUID() + ";" + location.getBlockX() + ";" + location.getBlockY() + ";" + location.getBlockZ();
                                jsonWriter.value(locationString);
                            }
                    )
            )
            .addField(
                    "looted-chunks",
                    SchemaEntries.setEntry(
                            lootTrackerData -> lootTrackerData.lootedChunks,
                            (lootTrackerData, strings) -> lootTrackerData.lootedChunks = strings,
                            (jsonReader, lootTrackerData) -> jsonReader.nextString(),
                            (string, jsonWriter) -> jsonWriter.value(string)
                    )
            )
            .build();

    private final PlayerLootTracker playerLootTracker;
    private UUID uuid;
    private Set<Location> lootedLocations = new HashSet<>();
    private Set<String> lootedChunks = new HashSet<>();

    public LootTrackerData(PlayerLootTracker playerLootTracker, UUID uuid) {
        this.playerLootTracker = playerLootTracker;
        this.uuid = uuid;
    }

    private LootTrackerData(PlayerLootTracker playerLootTracker) {
        this.playerLootTracker = playerLootTracker;
    }

    public void addLocation(Location location) {
        this.lootedLocations.add(location);
        playerLootTracker.lootTrackerDataSaver.scheduleSave(this);
    }

    public boolean hasLooted(Location location) {
        return lootedLocations.contains(location);
    }

    public void addChunk(Chunk chunk) {
        this.lootedChunks.add(chunk.getWorld().getUID() + ";" + chunk.getX() + ";" + chunk.getZ());
        playerLootTracker.lootTrackerDataSaver.scheduleSave(this);
    }

    public boolean hasLooted(Chunk chunk) {
        return lootedChunks.contains(chunk.getWorld().getUID() + ";" + chunk.getX() + ";" + chunk.getZ());
    }

    public UUID getUuid() {
        return uuid;
    }

    public String toJsonString() {
        try {
            return LootTrackerData.SCHEMA.toJsonString(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static LootTrackerData fromJsonString(PlayerLootTracker playerLootTracker, String jsonString) {
        LootTrackerData lootTrackerData = new LootTrackerData(playerLootTracker);
        try {
            LootTrackerData.SCHEMA.populateFromJsonString(lootTrackerData, jsonString);
            return lootTrackerData;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
