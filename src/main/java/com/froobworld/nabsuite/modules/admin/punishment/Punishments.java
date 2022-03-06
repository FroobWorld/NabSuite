package com.froobworld.nabsuite.modules.admin.punishment;

import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
            .addField("punishment-history", SchemaEntries.listEntry(
                    punishments -> punishments.punishmentHistory,
                    (punishments, punishmentHistory) -> punishments.punishmentHistory = punishmentHistory,
                    (jsonReader, punishments) -> PunishmentLogItem.fromJsonReader(punishments.adminModule.getPlugin().getPlayerIdentityManager(), jsonReader),
                    PunishmentLogItem.SCHEMA::write
            ))
            .build();

    private final AdminModule adminModule;
    private final PunishmentManager punishmentManager;
    private UUID uuid;
    private BanPunishment banPunishment;
    private MutePunishment mutePunishment;
    private JailPunishment jailPunishment;
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
        return banPunishment;
    }

    public MutePunishment getMutePunishment() {
        return mutePunishment;
    }

    public JailPunishment getJailPunishment() {
        return jailPunishment;
    }

    public List<PunishmentLogItem> getPunishmentHistory() {
        return punishmentHistory == null ? Lists.newArrayList() : List.copyOf(punishmentHistory);
    }

    void setBanPunishment(BanPunishment banPunishment) {
        this.banPunishment = banPunishment;
        punishmentManager.punishmentsSaver.scheduleSave(this);
    }

    void setMutePunishment(MutePunishment mutePunishment) {
        this.mutePunishment = mutePunishment;
        punishmentManager.punishmentsSaver.scheduleSave(this);
    }

    void setJailPunishment(JailPunishment jailPunishment) {
        this.jailPunishment = jailPunishment;
        punishmentManager.punishmentsSaver.scheduleSave(this);
    }

    void addPunishmentLogItem(PunishmentLogItem punishmentLogItem) {
        if (punishmentHistory == null) {
            punishmentHistory = new ArrayList<>();
        }
        punishmentHistory.add(punishmentLogItem);
        punishmentManager.punishmentsSaver.scheduleSave(this);
        punishmentManager.getPunishmentLog().addPunishmentLogItem(punishmentLogItem);
    }

    public String toJsonString() {
        try {
            return SCHEMA.toJsonString(this);
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
