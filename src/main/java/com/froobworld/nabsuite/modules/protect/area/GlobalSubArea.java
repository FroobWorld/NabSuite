package com.froobworld.nabsuite.modules.protect.area;

import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;
import com.google.gson.stream.JsonReader;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class GlobalSubArea implements AreaLike {
    static final SimpleDataSchema<GlobalSubArea> SCHEMA = new SimpleDataSchema.Builder<GlobalSubArea>()
            .addField("name", SchemaEntries.stringEntry(
                    globalSubArea -> globalSubArea.name,
                    (globalSubArea, name) -> globalSubArea.name = name
            ))
            .addField("flags", SchemaEntries.setEntry(
                    globalSubArea -> globalSubArea.flags,
                    (globalSubArea, flags) -> globalSubArea.flags = flags,
                    (jsonReader, globalSubArea) -> jsonReader.nextString(),
                    (flag, jsonWriter) -> jsonWriter.value(flag)
            ))
            .addField("bound1", SchemaEntries.integerEntry(
                    globalSubArea -> globalSubArea.bound1,
                    (globalSubarea, bound1) -> globalSubarea.bound1 = bound1
            ))
            .addField("bound2", SchemaEntries.integerEntry(
                    globalSubArea -> globalSubArea.bound2,
                    (globalSubarea, bound2) -> globalSubarea.bound2 = bound2
            ))
            .build();
    private final GlobalArea parent;
    private String name;
    private int bound1;
    private int bound2;
    private Set<String> flags;

    public GlobalSubArea(GlobalArea parent, String name, int bound1, int bound2) {
        this.parent = parent;
        this.name = name;
        this.bound1 = bound1;
        this.bound2 = bound2;
        this.flags = new HashSet<>();
    }

    private GlobalSubArea(GlobalArea parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public int getBound2() {
        return bound2;
    }

    public int getBound1() {
        return bound1;
    }

    public Set<String> getFlags() {
        return Set.copyOf(flags);
    }

    public void addFlag(String flag) {
        flags.add(flag.toLowerCase());
        parent.globalAreaManager.scheduleSave();
    }

    public void removeFlag(String flag) {
        flags.remove(flag.toLowerCase());
        parent.globalAreaManager.scheduleSave();
    }

    public void setBounds(int bound1, int bound2) {
        this.bound1 = bound1;
        this.bound2 = bound2;
        parent.globalAreaManager.scheduleSave();
    }

    @Override
    public boolean hasFlag(String flag) {
        return flags.contains(flag.toLowerCase());
    }

    @Override
    public boolean containsLocation(Location location) {
        return location.getY() >= Math.min(bound1, bound2) && location.getY() <= Math.max(bound1, bound2);
    }

    @Override
    public boolean hasUserRights(Player player) {
        return parent.hasUserRights(player);
    }

    static GlobalSubArea fromJsonReader(GlobalArea parent, JsonReader jsonReader) {
        GlobalSubArea subArea = new GlobalSubArea(parent);
        try {
            GlobalSubArea.SCHEMA.populate(subArea, jsonReader);
            return subArea;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
