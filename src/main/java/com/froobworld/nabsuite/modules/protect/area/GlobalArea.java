package com.froobworld.nabsuite.modules.protect.area;

import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;
import com.froobworld.nabsuite.user.User;
import com.froobworld.nabsuite.user.UserManager;
import com.google.gson.stream.JsonReader;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class GlobalArea implements AreaLike {
    static final SimpleDataSchema<GlobalArea> SCHEMA = new SimpleDataSchema.Builder<GlobalArea>()
            .addField("flags", SchemaEntries.setEntry(
                    globalArea -> globalArea.flags,
                    (globalArea, flags) -> globalArea.flags = flags,
                    (jsonReader, globalArea) -> jsonReader.nextString(),
                    (flag, jsonWriter) -> jsonWriter.value(flag)
            ))
            .addField("users", SchemaEntries.setEntry(
                    globalArea -> globalArea.users,
                    (globalArea, users) -> globalArea.users = users,
                    (jsonReader, globalArea) -> globalArea.userManager.parseUser(jsonReader),
                    User::writeToJson
            ))
            .addField("sub-areas", SchemaEntries.setEntry(
                    globalArea -> globalArea.subAreas,
                    (globalArea, subAreas) -> globalArea.subAreas = subAreas,
                    (jsonReader, globalArea) -> GlobalSubArea.fromJsonReader(globalArea, jsonReader),
                    GlobalSubArea.SCHEMA::write
            ))
            .build();
    final GlobalAreaManager globalAreaManager;
    private final UserManager userManager;
    private Set<String> flags;
    private Set<User> users;
    private Set<GlobalSubArea> subAreas;

    public GlobalArea(GlobalAreaManager globalAreaManager, UserManager userManager) {
        this.globalAreaManager = globalAreaManager;
        this.userManager = userManager;
        this.flags = new HashSet<>();
        this.users = new HashSet<>();
        this.subAreas = new HashSet<>();
    }

    @Override
    public boolean hasFlag(String flag) {
        return flags.contains(flag.toLowerCase());
    }

    @Override
    public boolean containsLocation(Location location) {
        return true;
    }

    @Override
    public boolean hasUserRights(Player player) {
        for (User user : users) {
            if (user.includesPlayer(player)) {
                return true;
            }
        }
        return false;
    }

    public Set<GlobalSubArea> getSubAreas() {
        return Set.copyOf(subAreas);
    }

    public Set<String> getFlags() {
        return Set.copyOf(flags);
    }

    public Set<User> getUsers() {
        return Set.copyOf(users);
    }

    public boolean isUser(User user) {
        return users.contains(user);
    }

    public void addFlag(String flag) {
        flags.add(flag.toLowerCase());
        globalAreaManager.scheduleSave();
    }

    public void removeFlag(String flag) {
        flags.remove(flag.toLowerCase());
        globalAreaManager.scheduleSave();
    }

    public void addUser(User user) {
        users.add(user);
        globalAreaManager.scheduleSave();
    }

    public void removeUser(User user) {
        users.remove(user);
        globalAreaManager.scheduleSave();
    }

    public void addSubArea(GlobalSubArea globalSubArea) {
        subAreas.add(globalSubArea);
        globalAreaManager.scheduleSave();
    }

    public void removeSubArea(GlobalSubArea globalSubArea) {
        subAreas.remove(globalSubArea);
        globalAreaManager.scheduleSave();
    }

    public Set<AreaLike> getTopMostAreas(Location location) {
        Set<AreaLike> areas = new HashSet<>();
        for (GlobalSubArea subArea : subAreas) {
            if (subArea.containsLocation(location)) {
                areas.add(subArea);
            }
        }
        return areas.isEmpty() ? Collections.singleton(this) : areas;
    }

    public static GlobalArea fromJsonReader(GlobalAreaManager globalAreaManager, UserManager userManager, JsonReader jsonReader) {
        GlobalArea globalArea = new GlobalArea(globalAreaManager, userManager);
        try {
            SCHEMA.populate(globalArea, jsonReader);
            return globalArea;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
