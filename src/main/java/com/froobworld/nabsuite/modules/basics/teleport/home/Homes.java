package com.froobworld.nabsuite.modules.basics.teleport.home;

import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Homes {
    private static final SimpleDataSchema<Homes> SCHEMA = new SimpleDataSchema.Builder<Homes>()
            .addField("uuid", SchemaEntries.uuidEntry(
                    homes -> homes.uuid,
                    (homes, uuid) -> homes.uuid = uuid
            ))
            .addField("homes", SchemaEntries.setEntry(
                    homes -> homes.homes,
                    (homes, homesSet) -> homes.homes = homesSet,
                    (jsonReader, homes) -> Home.fromJsonReader(jsonReader),
                    Home.SCHEMA::write
            ))
            .build();
    private final HomeManager homeManager;
    private UUID uuid;
    private Set<Home> homes;

    public Homes(HomeManager homeManager, UUID uuid) {
        this(homeManager);
        this.uuid = uuid;
        this.homes = new HashSet<>();
    }

    private Homes(HomeManager homeManager) {
        this.homeManager = homeManager;
    }

    public Set<Home> getHomes() {
        return Set.copyOf(homes);
    }

    public UUID getUuid() {
        return uuid;
    }

    public Home getHome(String name) {
        for (Home home : homes) {
            if (home.getName().equalsIgnoreCase(name)) {
                return home;
            }
        }
        return null;
    }

    void addHome(Home home) {
        homes.add(home);
        homeManager.homesSaver.scheduleSave(this);
    }

    void removeHome(Home home) {
        homes.remove(home);
        homeManager.homesSaver.scheduleSave(this);
    }

    public String toJsonString() {
        try {
            return SCHEMA.toJsonString(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Homes fromJsonString(HomeManager homeManager, String jsonString) {
        Homes homes = new Homes(homeManager);
        try {
            SCHEMA.populateFromJsonString(homes, jsonString);
            return homes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
