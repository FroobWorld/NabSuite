package com.froobworld.nabsuite.modules.admin.punishment;

import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Punishments {
    private static final SimpleDataSchema<Punishments> SCHEMA = new SimpleDataSchema.Builder<Punishments>()
            .addField("uuid", SchemaEntries.uuidEntry(
                    punishments -> punishments.uuid,
                    (punishments, uuid) -> punishments.uuid = uuid
            ))
            .addField(
                    "ban",
                    punishments -> punishments.banPunishment != null,
                    (jsonReader, punishments) -> punishments.banPunishment = BanPunishment.fromJsonReader(jsonReader),
                    (punishments, jsonWriter) -> BanPunishment.SCHEMA.write(punishments.banPunishment, jsonWriter)
            )
            .addField(
                    "mute",
                    punishments -> punishments.mutePunishment != null,
                    (jsonReader, punishments) -> punishments.mutePunishment = MutePunishment.fromJsonReader(jsonReader),
                    (punishments, jsonWriter) -> MutePunishment.SCHEMA.write(punishments.mutePunishment, jsonWriter)
            )
            .addField(
                    "jail",
                    punishments -> punishments.jailPunishment != null,
                    (jsonReader, punishments) -> punishments.jailPunishment = JailPunishment.fromJsonReader(punishments.punishmentManager.getJailManager(), jsonReader),
                    (punishments, jsonWriter) -> JailPunishment.SCHEMA.write(punishments.jailPunishment, jsonWriter)
            )
            .addField(
                    "restriction",
                    punishments -> punishments.restrictionPunishment != null,
                    (jsonReader, punishments) -> punishments.restrictionPunishment = RestrictionPunishment.fromJsonReader(jsonReader),
                    (punishments, jsonWriter) -> RestrictionPunishment.SCHEMA.write(punishments.restrictionPunishment, jsonWriter)
            )
            .addField("punishment-history", SchemaEntries.listEntry(
                    punishments -> punishments.punishmentHistory,
                    (punishments, punishmentHistory) -> punishments.punishmentHistory = punishmentHistory,
                    (jsonReader, punishments) -> PunishmentLogItem.fromJsonReader(punishments.adminModule.getPlugin().getPlayerIdentityManager(), jsonReader),
                    PunishmentLogItem.SCHEMA::write
            ))
            .build();

    public final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final AdminModule adminModule;
    private final PunishmentManager punishmentManager;
    private UUID uuid;
    private BanPunishment banPunishment;
    private MutePunishment mutePunishment;
    private JailPunishment jailPunishment;
    private RestrictionPunishment restrictionPunishment;
    private List<PunishmentLogItem> punishmentHistory;

    private Punishments(AdminModule adminModule, PunishmentManager punishmentManager) {
        this.adminModule = adminModule;
        this.punishmentManager = punishmentManager;
    }

    public Punishments(AdminModule adminModule, PunishmentManager punishmentManager, UUID uuid) {
        this.adminModule = adminModule;
        this.punishmentManager = punishmentManager;
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public BanPunishment getBanPunishment() {
        lock.readLock().lock();
        try {
            return banPunishment;
        } finally {
            lock.readLock().unlock();
        }
    }

    public MutePunishment getMutePunishment() {
        lock.readLock().lock();
        try {
            return mutePunishment;
        } finally {
            lock.readLock().unlock();
        }
    }

    public JailPunishment getJailPunishment() {
        lock.readLock().lock();
        try {
            return jailPunishment;
        } finally {
            lock.readLock().unlock();
        }
    }

    public RestrictionPunishment getRestrictionPunishment() {
        lock.readLock().lock();
        try {
            return restrictionPunishment;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<PunishmentLogItem> getPunishmentHistory() {
        lock.readLock().lock();
        try {
            return punishmentHistory == null ? Lists.newArrayList() : List.copyOf(punishmentHistory);
        } finally {
            lock.readLock().unlock();
        }
    }

    void setBanPunishment(BanPunishment banPunishment) {
        lock.writeLock().lock();
        this.banPunishment = banPunishment;
        lock.writeLock().unlock();
        punishmentManager.punishmentsSaver.scheduleSave(this);
    }

    void setMutePunishment(MutePunishment mutePunishment) {
        lock.writeLock().lock();
        this.mutePunishment = mutePunishment;
        lock.writeLock().unlock();
        punishmentManager.punishmentsSaver.scheduleSave(this);
    }

    void setJailPunishment(JailPunishment jailPunishment) {
        lock.writeLock().lock();
        this.jailPunishment = jailPunishment;
        lock.writeLock().unlock();
        punishmentManager.punishmentsSaver.scheduleSave(this);
    }

    void setRestrictionPunishment(RestrictionPunishment restrictionPunishment) {
        lock.writeLock().lock();
        this.restrictionPunishment = restrictionPunishment;
        lock.writeLock().unlock();
        punishmentManager.punishmentsSaver.scheduleSave(this);
    }

    void addPunishmentLogItem(PunishmentLogItem punishmentLogItem) {
        lock.writeLock().lock();
        try {
            if (punishmentHistory == null) {
                punishmentHistory = new ArrayList<>();
            }
            punishmentHistory.add(punishmentLogItem);
        } finally {
            lock.writeLock().unlock();
        }
        punishmentManager.punishmentsSaver.scheduleSave(this);
        punishmentManager.getPunishmentLog().addPunishmentLogItem(punishmentLogItem);
    }

    public String toJsonString() {
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

    public static Punishments fromJsonString(AdminModule adminModule, PunishmentManager punishmentManager, String jsonString) {
        Punishments punishments = new Punishments(adminModule, punishmentManager);
        try {
            SCHEMA.populateFromJsonString(punishments, jsonString);
            return punishments;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
