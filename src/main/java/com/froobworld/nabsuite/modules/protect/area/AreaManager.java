package com.froobworld.nabsuite.modules.protect.area;

import com.froobworld.nabsuite.data.DataLoader;
import com.froobworld.nabsuite.data.DataSaver;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.tasks.StaffTask;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.froobworld.nabsuite.modules.protect.area.flag.Flags;
import com.froobworld.nabsuite.modules.protect.area.flag.enforcers.*;
import com.froobworld.nabsuite.modules.protect.area.visualiser.AreaVisualiser;
import com.froobworld.nabsuite.user.User;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class AreaManager {
    public static final String EDIT_ALL_AREAS_PERMISSION = "nabsuite.editallareas";
    private static final Set<String> defaultFlags = Set.of(Flags.NO_BUILD, Flags.NO_INTERACT, Flags.NO_EXPLODE, Flags.NO_FIRE_SPREAD, Flags.NO_FIRE_DESTROY);
    public static final Pattern areaNamePattern = Pattern.compile("^[a-zA-z0-9-_]+$");
    private static final Pattern fileNamePattern = Pattern.compile("^[a-zA-z0-9-_]+\\.json$");
    protected final DataSaver areaSaver;
    private final ProtectModule protectModule;
    private final BiMap<String, Area> areaMap = HashBiMap.create();
    private final File directory;
    private final GlobalAreaManager globalAreaManager;
    private final AreaNotificationManager areaNotificationManager;
    private final AreaVisualiser areaVisualiser;

    public AreaManager(ProtectModule protectModule) {
        this.protectModule = protectModule;
        directory = new File(protectModule.getDataFolder(), "areas/");
        areaSaver = new DataSaver(protectModule.getPlugin(), 1200);
        areaMap.putAll(DataLoader.loadAll(
                directory,
                fileName -> fileNamePattern.matcher(fileName.toLowerCase()).matches(),
                bytes -> Area.fromJsonString(this, protectModule.getPlugin().getUserManager(), new String(bytes)),
                (fileName, area) -> area.getName().toLowerCase()
        ));
        areaSaver.start();
        areaSaver.addDataType(Area.class, area -> area.toJsonString().getBytes(), area -> new File(directory, area.getName() + ".json"));
        initiateFlagEnforcers();
        globalAreaManager = new GlobalAreaManager(protectModule);
        areaNotificationManager = new AreaNotificationManager();
        this.areaVisualiser = new AreaVisualiser(protectModule);
    }

    public void postStartup() {
        Supplier<List<StaffTask>> areaRequestTaskSupplier = () -> {
            List<StaffTask> tasks = new ArrayList<>();
            List<String> areasRequiringApproval = getAreas().stream()
                    .filter(Predicate.not(Area::isApproved))
                    .map(Area::getName)
                    .sorted()
                    .toList();
            for (String area : areasRequiringApproval) {
                StaffTask task = new StaffTask(
                        "nabsuite.command.area.approve",
                        Component.text("Requested area '" + area + "' requires reviewing (/area review).")
                                .clickEvent(ClickEvent.suggestCommand("/area review " + area))
                );
                tasks.add(task);
            }
            return tasks;
        };
        protectModule.getPlugin().getModule(AdminModule.class).getStaffTaskManager().addStaffTaskSupplier(areaRequestTaskSupplier);
    }

    public void shutdown() {
        areaSaver.stop();
        globalAreaManager.shutdown();
    }

    public Area createArea(UUID creator, String name, World world, Vector corner1, Vector corner2, User owner, boolean approved) {
        if (getArea(name) != null) {
            throw new IllegalStateException("Area with that name already exists");
        }
        Area parent = null;
        if (name.contains(":")) {
            parent = getArea(name.substring(0, name.lastIndexOf(":")));
            String[] nameSplit = name.split(":");
            name = nameSplit[nameSplit.length - 1];
        }
        if (!areaNamePattern.matcher(name).matches()) {
            throw new IllegalArgumentException("Name does not match pattern: " + areaNamePattern);
        }
        Area area = new Area(this, protectModule.getPlugin().getUserManager(), parent, creator, name, world, corner1, corner2, owner, approved, defaultFlags);
        if (parent != null) {
            parent.addChild(area);
        } else {
            areaMap.put(name.toLowerCase(), area);
            areaSaver.scheduleSave(area);
        }
        if (!area.isApproved()) {
            protectModule.getPlugin().getModule(AdminModule.class).getStaffTaskManager().notifyNewTask("nabsuite.command.area.approve");
            AdminModule adminModule = protectModule.getPlugin().getModule(AdminModule.class);
            if (adminModule != null) {
                adminModule.getDiscordStaffLog().sendAreaRequestNotification(area);
            }
        }
        return area;
    }

    public void deleteArea(Area area) {
        if (area.getParent() != null) {
            area.getParent().deleteChild(area);
        } else {
            areaMap.remove(area.getName().toLowerCase());
            areaSaver.scheduleDeletion(area);
        }
    }

    public Area getArea(String name) {
        if (name.contains(":")) {
            String[] nameSplit = name.split(":", 2);
            Area area = areaMap.get(nameSplit[0].toLowerCase());
            for (String nextName : nameSplit[1].split(":")) {
                if (area == null) {
                    return null;
                }
                area = area.getChild(nextName);
            }
            return area;
        } else {
            return areaMap.get(name.toLowerCase());
        }
    }

    public Set<Area> getAreas() {
        return areaMap.values();
    }

    public Set<AreaLike> getTopMostAreasAtLocation(Location location) {
        Set<AreaLike> areaSet = new HashSet<>();
        for (Area area : areaMap.values()) {
            if (area.isApproved()) {
                areaSet.addAll(area.getTopMostArea(location));
            }
        }
        if (areaSet.isEmpty()) {
            return globalAreaManager.getTopMostAreasAtLocation(location);
        }
        return areaSet;
    }

    public boolean isAreaAtLocation(Location location) {
        for (Area area : areaMap.values()) {
            if (area.isApproved() && area.containsLocation(location)) {
                return true;
            }
        }
        return false;
    }

    public Set<Area> getAreasNear(Location location, int radius) {
        Set<Area> areaSet = new HashSet<>();
        for (Area area: areaMap.values()) {
            if (area.isApproved()) {
                areaSet.addAll(area.getAreasNear(location, radius));
            }
        }
        return areaSet;
    }

    public GlobalAreaManager getGlobalAreaManager() {
        return globalAreaManager;
    }

    private void initiateFlagEnforcers() {
        Set.of(
                new NoBuildFlagEnforcer(this),
                new NoInteractFlagEnforcer(this, protectModule.getVehicleTracker()),
                new NoExplodeFlagEnforcer(this),
                new NoFireDestroyFlagEnforcer(this),
                new NoFireSpreadFlagEnforcer(this),
                new NoMobDamageFlagEnforcer(this),
                new NoMobTargetFlagEnforcer(this),
                new NoMobSpawnFlagEnforcer(this),
                new NoMobGriefFlagEnforcer(this),
                new NoPvpFlagEnforcer(this),
                new KeepInventoryFlagEnforcer(this),
                new NoWitherFlagEnforcer(this),
                new NoHomeFlagEnforcer(this)
        ).forEach(enforcer -> Bukkit.getPluginManager().registerEvents(enforcer, protectModule.getPlugin()));
    }

    public AreaNotificationManager getAreaNotificationManager() {
        return areaNotificationManager;
    }

    public AreaVisualiser getAreaVisualiser() {
        return areaVisualiser;
    }
}
