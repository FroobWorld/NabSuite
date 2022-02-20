package com.froobworld.nabsuite.modules.basics.teleport.warp;

import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;
import org.bukkit.Location;

import java.io.IOException;
import java.util.UUID;

public class Warp {
    private static final SimpleDataSchema<Warp> SCHEMA = new SimpleDataSchema.Builder<Warp>()
            .addField("name", SchemaEntries.stringEntry(
                    warp -> warp.name,
                    (warp, name) -> warp.name = name
            ))
            .addField("location", SchemaEntries.locationEntry(
                    warp -> warp.location,
                    (warp, location) -> warp.location = location
            ))
            .addField("creator", SchemaEntries.uuidEntry(
                    warp -> warp.creator,
                    (warp, creator) -> warp.creator = creator
            ))
            .addField("created", SchemaEntries.longEntry(
                    warp -> warp.created,
                    (warp, created) -> warp.created = created
            ))
            .build();

    private final WarpManager warpManager;
    private String name;
    private Location location;
    private UUID creator;
    private long created;

    private Warp(WarpManager warpManager) {
        this.warpManager = warpManager;
    }

    public Warp(WarpManager warpManager, String name, Location location, UUID creator) {
        this.warpManager = warpManager;
        this.name = name;
        this.location = location;
        this.creator = creator;
        this.created = System.currentTimeMillis();
    }

    public Location getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    public UUID getCreator() {
        return creator;
    }

    public long getTimeCreated() {
        return created;
    }

    public String toJsonString() {
        try {
            return SCHEMA.toJsonString(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Warp fromJsonString(WarpManager warpManager, String jsonString) {
        Warp warp = new Warp(warpManager);
        try {
            SCHEMA.populateFromJsonString(warp, jsonString);
            return warp;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
