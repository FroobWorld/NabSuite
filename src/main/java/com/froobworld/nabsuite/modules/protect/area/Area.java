package com.froobworld.nabsuite.modules.protect.area;

import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;
import com.froobworld.nabsuite.modules.protect.user.User;
import com.froobworld.nabsuite.modules.protect.user.UserManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Area implements AreaLike {
    private static final SimpleDataSchema<Area> SCHEMA = new SimpleDataSchema.Builder<Area>()
            .addField("creator", SchemaEntries.uuidEntry(
                    area -> area.creator,
                    (area, creator) -> area.creator = creator
            ))
            .addField("name", SchemaEntries.stringEntry(
                    area -> area.name,
                    (area, name) -> area.name = name
            ))
            .addField("approved", SchemaEntries.booleanEntry(
                    area -> area.approved,
                    (area, approved) -> area.approved = approved
            ))
            .addField("world", SchemaEntries.worldEntry(
                    area -> area.world,
                    (area, world) -> area.world = world
            ))
            .addField("corner1", SchemaEntries.vectorEntry(
                    area -> area.corner1,
                    (area, corner1) -> area.corner1 = corner1
            ))
            .addField("corner2", SchemaEntries.vectorEntry(
                    area -> area.corner2,
                    (area, corner2) -> area.corner2 = corner2
            ))
            .addField("owners", SchemaEntries.setEntry(
                    area -> area.owners,
                    (area, owners) -> area.owners = owners,
                    (jsonReader, area) -> area.userManager.parseUser(jsonReader),
                    User::writeToJson
            ))
            .addField("managers", SchemaEntries.setEntry(
                    area -> area.managers,
                    (area, managers) -> area.managers = managers,
                    (jsonReader, area) -> area.userManager.parseUser(jsonReader),
                    User::writeToJson
            ))
            .addField("users", SchemaEntries.setEntry(
                    area -> area.users,
                    (area, users) -> area.users = users,
                    (jsonReader, area) -> area.userManager.parseUser(jsonReader),
                    User::writeToJson
            ))
            .addField("flags", SchemaEntries.setEntry(
                    area -> area.flags,
                    (area, flags) -> area.flags = flags,
                    (jsonReader, area) -> jsonReader.nextString(),
                    (flag, jsonWriter) -> jsonWriter.value(flag)
            ))
            .addField("children", SchemaEntries.setEntry(
                    area -> area.children,
                    (area, children) -> area.children = children,
                    (jsonReader, area) -> {
                        Area child = new Area(area.areaManager, area.userManager, area);
                        Area.SCHEMA.populate(child, jsonReader);
                        return child;
                    },
                    (area, jsonWriter) -> Area.SCHEMA.write(area, jsonWriter) // Do not replace with method reference
            ))
            .build();

    private final UserManager userManager;
    private final AreaManager areaManager;
    private UUID creator;
    private String name;
    private boolean approved;
    private World world;
    private Vector corner1;
    private Vector corner2;
    private Set<User> owners;
    private Set<User> managers;
    private Set<User> users;
    private Set<String> flags;
    private final Area parent;
    private Set<Area> children;

    public Area(AreaManager areaManager, UserManager userManager, Area parent, UUID creator, String name, World world, Vector corner1, Vector corner2, User owner, boolean approved, Set<String> flags) {
        this.userManager = userManager;
        this.areaManager = areaManager;
        this.creator = creator;
        this.name = name;
        this.approved = approved;
        this.world = world;
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.owners = new HashSet<>(Collections.singleton(owner));
        this.managers = new HashSet<>();
        this.users = new HashSet<>();
        this.flags = new HashSet<>(flags);
        this.parent = parent;
        this.children = new HashSet<>();
    }

    private Area(AreaManager areaManager, UserManager userManager, Area parent) {
        this.userManager = userManager;
        this.areaManager = areaManager;
        this.parent = parent;
    }

    public UUID getCreator() {
        return creator;
    }

    public String getName() {
        return name;
    }

    public boolean isApproved() {
        return approved;
    }

    public String getLongFormName() {
        return parent != null ? parent.getLongFormName() + ":" + getName() : getName();
    }

    public Area getParent() {
        return parent;
    }

    public World getWorld() {
        return world;
    }

    public Vector getCorner2() {
        return corner2;
    }

    public Vector getCorner1() {
        return corner1;
    }

    public Set<User> getUsers() {
        return users;
    }

    public Set<User> getManagers() {
        return managers;
    }

    public Set<User> getOwners() {
        return owners;
    }

    public boolean isUser(User user) {
        return users.contains(user);
    }

    public boolean isManager(User user) {
        return managers.contains(user);
    }

    public boolean isOwner(User user) {
        return owners.contains(user);
    }

    public boolean isUser(Player player) {
        for (User user : users) {
            if (user.includesPlayer(player)) {
                return true;
            }
        }
        return false;
    }

    public boolean isManager(Player player) {
        for (User manager : managers) {
            if (manager.includesPlayer(player)) {
                return true;
            }
        }
        return false;
    }

    public boolean isOwner(Player player) {
        for (User owner : owners) {
            if (owner.includesPlayer(player)) {
                return true;
            }
        }
        return false;
    }

    public void addUser(User user) {
        users.add(user);
        scheduleSave();
    }

    public void addManager(User user) {
        managers.add(user);
        scheduleSave();
    }

    public void addOwner(User user) {
        owners.add(user);
        scheduleSave();
    }

    public void removeUser(User user) {
        users.remove(user);
        scheduleSave();
    }

    public void removeManager(User user) {
        managers.remove(user);
        scheduleSave();
    }

    public void removeOwner(User user) {
        owners.remove(user);
        scheduleSave();
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
        scheduleSave();
    }

    @Override
    public boolean hasUserRights(Player player) {
        return isUser(player) || isManager(player) || isOwner(player);
    }

    public boolean hasManagerRights(Player player) {
        return isManager(player) || isOwner(player);
    }

    public Set<Area> getChildren() {
        return children;
    }

    public Area getChild(String name) {
        for (Area area : children) {
            if (area.getName().equalsIgnoreCase(name)) {
                return area;
            }
        }
        return null;
    }

    public void addChild(Area child) {
        children.add(child);
        scheduleSave();
    }

    public void deleteChild(Area child) {
        children.remove(child);
        scheduleSave();
    }

    public Set<String> getFlags() {
        return Set.copyOf(flags);
    }

    @Override
    public boolean hasFlag(String flag) {
        return flags.contains(flag.toLowerCase());
    }

    public void addFlag(String flag) {
        flags.add(flag.toLowerCase());
        scheduleSave();
    }

    public void removeFlag(String flag) {
        flags.remove(flag);
        scheduleSave();
    }

    public void setCorners(Vector corner1, Vector corner2) {
        this.corner1 = corner1;
        this.corner2 = corner2;
        scheduleSave();
    }

    @Override
    public boolean containsLocation(Location location) {
        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
        int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
        return location.getWorld().equals(this.world) &&
                location.getBlockX() >= minX && location.getBlockX() <= maxX &&
                location.getBlockY() >= minY && location.getBlockY() <= maxY &&
                location.getBlockZ() >= minZ && location.getBlockZ() <= maxZ;
    }

    public Set<Area> getTopMostArea(Location location) {
        if (!this.isApproved() || !this.containsLocation(location)) {
            return Collections.emptySet();
        }
        Set<Area> subAreas = new HashSet<>();
        for (Area area : children) {
            if (area.isApproved() && area.containsLocation(location)) {
                subAreas.addAll(area.getTopMostArea(location));
            }
        }
        return subAreas.isEmpty() ? Collections.singleton(this) : subAreas;
    }

    private void scheduleSave() {
        if (parent != null) {
            parent.scheduleSave();
            return;
        }
        areaManager.areaSaver.scheduleSave(this);
    }

    public String toJsonString() {
        try {
            return Area.SCHEMA.toJsonString(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Area fromJsonString(AreaManager areaManager, UserManager userManager, String jsonString) {
        Area area = new Area(areaManager, userManager, null);
        try {
            Area.SCHEMA.populateFromJsonString(area, jsonString);
            return area;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
