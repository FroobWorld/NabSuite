package com.froobworld.nabsuite.modules.admin.jail;

import com.froobworld.nabsuite.data.DataLoader;
import com.froobworld.nabsuite.data.DataSaver;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Set;
import java.util.regex.Pattern;

public class JailManager {
    public static final Pattern jailNamePattern = Pattern.compile("^[a-zA-z0-9-_]+$");
    private static final Pattern fileNamePattern = Pattern.compile("^[a-zA-z0-9-_]+\\.json$");
    protected final DataSaver jailSaver;
    private final BiMap<String, Jail> jailMap = HashBiMap.create();
    private final File directory;

    public JailManager(AdminModule adminModule) {
        directory = new File(adminModule.getDataFolder(), "jails/");
        jailSaver = new DataSaver(adminModule.getPlugin(), 1200);
        jailMap.putAll(DataLoader.loadAll(
                directory,
                fileName -> fileNamePattern.matcher(fileName.toLowerCase()).matches(),
                bytes -> Jail.fromJsonString(new String(bytes)),
                (fileName, jail) -> jail.getName().toLowerCase()
        ));
        jailSaver.start();
        jailSaver.addDataType(Jail.class, jail -> jail.toJsonString().getBytes(), jail -> new File(directory, jail.getName() + ".json"));
    }

    public void shutdown() {
        jailSaver.stop();
    }

    public Jail createJail(String name, double radius, Player creator) {
        if (!jailNamePattern.matcher(name).matches()) {
            throw new IllegalArgumentException("Name does not match pattern: " + jailNamePattern);
        }
        if (jailMap.containsKey(name.toLowerCase())) {
            throw new IllegalStateException("Jail with that name already exists");
        }
        Jail jail = new Jail(name, creator.getLocation(), radius, creator.getUniqueId());
        jailMap.put(name.toLowerCase(), jail);
        jailSaver.scheduleSave(jail);
        return jail;
    }

    public void deleteJail(Jail jail) {
        jailMap.remove(jail.getName().toLowerCase());
        jailSaver.scheduleDeletion(jail);
    }

    public Jail getJail(String name) {
        return jailMap.get(name.toLowerCase());
    }

    public Set<Jail> getJails() {
        return jailMap.values();
    }

}
