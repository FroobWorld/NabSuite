package com.froobworld.nabsuite.modules.admin.jail;

import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;
import org.bukkit.Location;

import java.io.IOException;
import java.util.UUID;

public class Jail {
    private static final SimpleDataSchema<Jail> SCHEMA = new SimpleDataSchema.Builder<Jail>()
            .addField("name", SchemaEntries.stringEntry(
                    jail -> jail.name,
                    (jail, name) -> jail.name = name
            ))
            .addField("location", SchemaEntries.locationEntry(
                    jail -> jail.location,
                    (jail, location) -> jail.location = location
            ))
            .addField("radius", SchemaEntries.doubleEntry(
                    jail -> jail.radius,
                    (jail, radius) -> jail.radius = radius
            ))
            .addField("creator", SchemaEntries.uuidEntry(
                    jail -> jail.creator,
                    (jail, creator) -> jail.creator = creator
            ))
            .addField("created", SchemaEntries.longEntry(
                    jail -> jail.created,
                    (jail, created) -> jail.created = created
            ))
            .build();

    private String name;
    private Location location;
    private double radius;
    private UUID creator;
    private long created;

    private Jail() {}

    public Jail(String name, Location location, double radius, UUID creator) {
        this.name = name;
        this.location = location;
        this.radius = radius;
        this.creator = creator;
        this.created = System.currentTimeMillis();
    }

    public Location getLocation() {
        return location;
    }

    public double getRadius() {
        return radius;
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

    public static Jail fromJsonString(String jsonString) {
        Jail jail = new Jail();
        try {
            SCHEMA.populateFromJsonString(jail, jsonString);
            return jail;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
