package com.froobworld.nabsuite.modules.basics.teleport.home;

import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;
import com.google.gson.stream.JsonReader;
import org.bukkit.Location;

import java.io.IOException;

public class Home {
    static final SimpleDataSchema<Home> SCHEMA = new SimpleDataSchema.Builder<Home>()
            .addField("name", SchemaEntries.stringEntry(
                    home -> home.name,
                    (home, name) -> home.name = name
            ))
            .addField("location", SchemaEntries.locationEntry(
                    home -> home.location,
                    (home, location) -> home.location = location
            ))
            .addField("created", SchemaEntries.longEntry(
                    home -> home.created,
                    (home, created) -> home.created = created
            ))
            .build();

    private String name;
    private Location location;
    private long created;

    private Home() {}

    public Home(String name, Location location) {
        this.name = name;
        this.location = location;
        this.created = System.currentTimeMillis();
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public long getTimeCreated() {
        return created;
    }

    static Home fromJsonReader(JsonReader jsonReader) throws IOException {
        Home home = new Home();
        SCHEMA.populate(home, jsonReader);
        return home;
    }

}
