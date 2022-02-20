package com.froobworld.nabsuite.modules.mechs.trees;

import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class UnnaturalLogRegion {
    static final SimpleDataSchema<UnnaturalLogRegion> SCHEMA = new SimpleDataSchema.Builder<UnnaturalLogRegion>()
            .addField("key", new SimpleDataSchema.SchemaEntry<>(
                    unnaturalLogRegion -> true,
                    (jsonReader, unnaturalLogRegion) -> {
                        jsonReader.beginObject();
                        Integer x = null;
                        Integer z = null;
                        while (jsonReader.hasNext()) {
                            String nextName = jsonReader.nextName();
                            if (nextName.equalsIgnoreCase("x")) {
                                x = jsonReader.nextInt();
                            } else if (nextName.equalsIgnoreCase("z")) {
                                z = jsonReader.nextInt();
                            }
                        }
                        if (x == null || z == null) {
                            throw new IllegalStateException();
                        }
                        unnaturalLogRegion.key = new Key(x, z);
                        jsonReader.endObject();
                    },
                    (unnaturalLogRegion, jsonWriter) -> {
                        jsonWriter.beginObject()
                                .name("x").value(unnaturalLogRegion.key.x())
                                .name("z").value(unnaturalLogRegion.key.z())
                                .endObject();
                    }
            ))
            .addField("log-locations", SchemaEntries.setEntry(
                    unnaturalLogRegion -> unnaturalLogRegion.logLocations,
                    (unnaturalLogRegion, logLocations) -> unnaturalLogRegion.logLocations = logLocations,
                    (jsonReader, unnaturalLogRegion) -> {
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
            ))
            .build();

    private final TreeManager treeManager;
    private Key key;
    private Set<Location> logLocations;

    private UnnaturalLogRegion(TreeManager treeManager) {
        this.treeManager = treeManager;
    }

    public UnnaturalLogRegion(TreeManager treeManager, Key key) {
        this.treeManager = treeManager;
        this.key = key;
        this.logLocations = new HashSet<>();
    }

    public Key getKey() {
        return key;
    }

    public boolean containsLocation(Location location) {
        return logLocations.contains(location);
    }

    public void addLocation(Location location) {
        if (logLocations.add(location)) {
            treeManager.regionDataSaver.scheduleSave(this);
        }
    }

    public void removeLocation(Location location) {
        if (logLocations.remove(location)) {
            treeManager.regionDataSaver.scheduleSave(this);
        }
    }

    public String toJsonString() {
        try {
            return UnnaturalLogRegion.SCHEMA.toJsonString(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static UnnaturalLogRegion fromJsonString(TreeManager treeManager, String jsonString) {
        UnnaturalLogRegion unnaturalLogRegion = new UnnaturalLogRegion(treeManager);
        try {
            UnnaturalLogRegion.SCHEMA.populateFromJsonString(unnaturalLogRegion, jsonString);
            return unnaturalLogRegion;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public record Key(int x, int z) {

        public static Key fromLocation(Location location) {
            return new Key(location.getBlockX() >> 8, location.getBlockZ() >> 8);
        }

    }

}
