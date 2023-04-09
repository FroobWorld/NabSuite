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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

    public final ReadWriteLock lock;
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
        this.lock = parent == null ? new ReentrantReadWriteLock() : parent.lock;
    }

    private Area(AreaManager areaManager, UserManager userManager, Area parent) {
        this.userManager = userManager;
        this.areaManager = areaManager;
        this.parent = parent;
        this.lock = parent == null ? new ReentrantReadWriteLock() : parent.lock;
    }

    public UUID getCreator() {
        lock.readLock().lock();
        try {
            return creator;
        } finally {
            lock.readLock().unlock();
        }
    }

    public String getName() {
        lock.readLock().lock();
        try {
            return name;
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isApproved() {
        lock.readLock().lock();
        try {
            return approved;
        } finally {
            lock.readLock().unlock();
        }
    }

    public String getLongFormName() {
        lock.readLock().lock();
        try {
            return parent != null ? parent.getLongFormName() + ":" + getName() : getName();
        } finally {
            lock.readLock().unlock();
        }
    }

    public Area getParent() {
        lock.readLock().lock();
        try {
            return parent;
        } finally {
            lock.readLock().unlock();
        }
    }

    public World getWorld() {
        lock.readLock().lock();
        try {
            return world;
        } finally {
            lock.readLock().unlock();
        }
    }

    public Vector getCorner2() {
        lock.readLock().lock();
        try {
            return corner2;
        } finally {
            lock.readLock().unlock();
        }
    }

    public Vector getCorner1() {
        lock.readLock().lock();
        try {
            return corner1;
        } finally {
            lock.readLock().unlock();
        }
    }

    public Set<User> getUsers() {
        lock.readLock().lock();
        try {
            return users;
        } finally {
            lock.readLock().unlock();
        }
    }

    public Set<User> getManagers() {
        lock.readLock().lock();
        try {
            return managers;
        } finally {
            lock.readLock().unlock();
        }
    }

    public Set<User> getOwners() {
        lock.readLock().lock();
        try {
            return owners;
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isUser(User user) {
        lock.readLock().lock();
        try {
            return users.contains(user);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isManager(User user) {
        lock.readLock().lock();
        try {
            return managers.contains(user);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isOwner(User user) {
        lock.readLock().lock();
        try {
            return owners.contains(user);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isUser(Player player) {
        lock.readLock().lock();
        try {
            for (User user : users) {
                if (user.includesPlayer(player)) {
                    return true;
                }
            }
        } finally {
            lock.readLock().unlock();
        }
        return false;
    }

    public boolean isManager(Player player) {
        lock.readLock().lock();
        try {
            for (User manager : managers) {
                if (manager.includesPlayer(player)) {
                    return true;
                }
            }
        } finally {
            lock.readLock().unlock();
        }
        return false;
    }

    public boolean isOwner(Player player) {
        lock.readLock().lock();
        try {
            for (User owner : owners) {
                if (owner.includesPlayer(player)) {
                    return true;
                }
            }
        } finally {
            lock.readLock().unlock();
        }
        return false;
    }

    public void addUser(User user) {
        lock.writeLock().lock();
        try {
            users.add(user);
        } finally {
            lock.writeLock().unlock();
        }
        scheduleSave();
    }

    public void addManager(User user) {
        lock.writeLock().lock();
        try {
            managers.add(user);
        } finally {
            lock.writeLock().unlock();
        }
        scheduleSave();
    }

    public void addOwner(User user) {
        lock.writeLock().lock();
        try {
            owners.add(user);
        } finally {
            lock.writeLock().unlock();
        }
        scheduleSave();
    }

    public void removeUser(User user) {
        lock.writeLock().lock();
        try {
            users.remove(user);
        } finally {
            lock.writeLock().unlock();
        }
        scheduleSave();
    }

    public void removeManager(User user) {
        lock.writeLock().lock();
        try {
            managers.remove(user);
        } finally {
            lock.writeLock().unlock();
        }
        scheduleSave();
    }

    public void removeOwner(User user) {
        lock.writeLock().lock();
        try {
            owners.remove(user);
        } finally {
            lock.writeLock().unlock();
        }
        scheduleSave();
    }

    public void setApproved(boolean approved) {
        lock.writeLock().lock();
        this.approved = approved;
        lock.writeLock().unlock();
        scheduleSave();
    }

    @Override
    public boolean hasUserRights(Player player) {
        lock.readLock().lock();
        try {
            return isUser(player) || isManager(player) || isOwner(player);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean hasManagerRights(Player player) {
        lock.readLock().lock();
        try {
            return isManager(player) || isOwner(player);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Set<Area> getChildren() {
        lock.readLock().lock();
        try {
            return Set.copyOf(children);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Area getChild(String name) {
        lock.readLock().lock();
        try {
            for (Area area : children) {
                if (area.getName().equalsIgnoreCase(name)) {
                    return area;
                }
            }
        } finally {
            lock.readLock().unlock();
        }
        return null;
    }

    public void addChild(Area child) {
        lock.writeLock().lock();
        try {
            children.add(child);
        } finally {
            lock.writeLock().unlock();
        }
        scheduleSave();
    }

    public void deleteChild(Area child) {
        lock.writeLock().lock();
        try {
            children.remove(child);
        } finally {
            lock.writeLock().unlock();
        }
        scheduleSave();
    }

    public Set<String> getFlags() {
        lock.readLock().lock();
        try {
            return Set.copyOf(flags);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean hasFlag(String flag) {
        lock.readLock().lock();
        try {
            return flags.contains(flag.toLowerCase());
        } finally {
            lock.readLock().unlock();
        }
    }

    public void addFlag(String flag) {
        lock.writeLock().lock();
        try {
            flags.add(flag.toLowerCase());
        } finally {
            lock.writeLock().unlock();
        }
        scheduleSave();
    }

    public void removeFlag(String flag) {
        lock.writeLock().lock();
        try {
            flags.remove(flag);
        } finally {
            lock.writeLock().unlock();
        }
        scheduleSave();
    }

    public void setCorners(Vector corner1, Vector corner2) {
        lock.writeLock().lock();
        this.corner1 = corner1;
        this.corner2 = corner2;
        lock.writeLock().unlock();
        scheduleSave();
    }

    @Override
    public boolean containsLocation(Location location) {
        lock.readLock().lock();
        try {
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
        } finally {
            lock.readLock().unlock();
        }
    }

    public Set<Area> getTopMostArea(Location location) {
        lock.readLock().lock();
        try {
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
        } finally {
            lock.readLock().unlock();
        }
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
            lock.readLock().lock();
            try {
                return Area.SCHEMA.toJsonString(this);
            } finally {
                lock.readLock().unlock();
            }
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
