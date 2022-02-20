package com.froobworld.nabsuite.modules.admin.punishment;

import com.froobworld.nabsuite.data.DataLoader;
import com.froobworld.nabsuite.data.DataSaver;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.jail.JailManager;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.io.File;
import java.util.UUID;
import java.util.regex.Pattern;

public class PunishmentManager {
    private static final Pattern fileNamePattern = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.json$");
    final AdminModule adminModule;
    protected final DataSaver punishmentsSaver;
    private final BiMap<UUID, Punishments> punishmentsMap = HashBiMap.create();
    private final File directory;
    private final BanEnforcer banEnforcer;
    private final MuteEnforcer muteEnforcer;
    private final JailEnforcer jailEnforcer;

    public PunishmentManager(AdminModule adminModule) {
        this.adminModule = adminModule;
        directory = new File(adminModule.getDataFolder(), "punishments/");
        punishmentsSaver = new DataSaver(adminModule.getPlugin(), 1200);
        punishmentsMap.putAll(DataLoader.loadAll(
                directory,
                fileName -> fileNamePattern.matcher(fileName.toLowerCase()).matches(),
                bytes -> Punishments.fromJsonString(adminModule, this, new String(bytes)),
                (fileName, punishments) -> punishments.getUuid()
        ));
        punishmentsSaver.start();
        punishmentsSaver.addDataType(Punishments.class, punishments -> punishments.toJsonString().getBytes(), punishments -> new File(directory, punishments.getUuid().toString() + ".json"));
        banEnforcer = new BanEnforcer(adminModule, this);
        muteEnforcer = new MuteEnforcer(adminModule, this);
        jailEnforcer = new JailEnforcer(adminModule, this);
    }

    public void shutdown() {
        punishmentsSaver.stop();
    }

    public Punishments getPunishments(UUID uuid) {
        if (!punishmentsMap.containsKey(uuid)) {
            Punishments punishments = new Punishments(adminModule, this, uuid);
            punishmentsMap.put(uuid, punishments);
            punishmentsSaver.scheduleSave(punishments);
        }
        return punishmentsMap.get(uuid);
    }

    public JailManager getJailManager() {
        return null;
    }

    public BanEnforcer getBanEnforcer() {
        return banEnforcer;
    }

    public MuteEnforcer getMuteEnforcer() {
        return muteEnforcer;
    }

    public JailEnforcer getJailEnforcer() {
        return jailEnforcer;
    }
}
