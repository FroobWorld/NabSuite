package com.froobworld.nabsuite.modules.admin.punishment;

import com.froobworld.nabsuite.data.DataLoader;
import com.froobworld.nabsuite.data.DataSaver;
import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;
import com.froobworld.nabsuite.modules.admin.AdminModule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PunishmentLog {
    private static final SimpleDataSchema<PunishmentLog> SCHEMA = new SimpleDataSchema.Builder<PunishmentLog>()
            .addField("punishment-log-items", SchemaEntries.listEntry(
                    punishmentLog -> punishmentLog.punishmentLogItems,
                    (punishmentLog, punishmentLogItems) -> punishmentLog.punishmentLogItems = punishmentLogItems,
                    (jsonReader, punishmentLog) -> PunishmentLogItem.fromJsonReader(punishmentLog.adminModule.getPlugin().getPlayerIdentityManager(), jsonReader),
                    PunishmentLogItem.SCHEMA::write
            ))
            .build();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final AdminModule adminModule;
    private List<PunishmentLogItem> punishmentLogItems = new ArrayList<>();
    private final File file;
    private final DataSaver dataSaver;

    public PunishmentLog(AdminModule adminModule) {
        this.adminModule = adminModule;
        this.file = new File(adminModule.getDataFolder(), "punishment-log.json");
        dataSaver = new DataSaver(adminModule.getPlugin(), 1200 * 5);
        dataSaver.addDataType(PunishmentLog.class, punishmentLog -> punishmentLog.toJsonString().getBytes(), p -> file);
        dataSaver.start();
        load();
    }

    public void addPunishmentLogItem(PunishmentLogItem punishmentLogItem) {
        lock.writeLock().lock();
        try {
            punishmentLogItems.add(punishmentLogItem);
        } finally {
            lock.writeLock().unlock();
        }
        dataSaver.scheduleSave(this);
        adminModule.getDiscordStaffLog().sendPunishmentLogItemNotification(punishmentLogItem);
    }

    public List<PunishmentLogItem> getPunishmentHistory() {
        lock.readLock().lock();
        try {
            return List.copyOf(punishmentLogItems);
        } finally {
            lock.readLock().unlock();
        }
    }

    void shutdown() {
        dataSaver.stop();
    }

    private void load() {
        if (!file.exists()) {
            return;
        }
        DataLoader.load(file, bytes -> this.populateFromJsonString(new String(bytes)));
    }

    private String toJsonString() {
        try {
            lock.readLock().lock();
            try {
                return SCHEMA.toJsonString(this);
            } finally {
                lock.readLock().unlock();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private PunishmentLog populateFromJsonString(String jsonString) {
        try {
            PunishmentLog.SCHEMA.populateFromJsonString(this, jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

}
