package com.froobworld.nabsuite.modules.admin.punishment;

import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.util.UUID;

public class BanPunishment {
    static final SimpleDataSchema<BanPunishment> SCHEMA = new SimpleDataSchema.Builder<BanPunishment>()
            .addField("reason", SchemaEntries.stringEntry(banPunishment -> banPunishment.reason, (banPunishment, s) -> banPunishment.reason = s))
            .addField("mediator", SchemaEntries.uuidEntry(banPunishment -> banPunishment.mediator, (banPunishment, uuid) -> banPunishment.mediator = uuid))
            .addField("time", SchemaEntries.longEntry(banPunishment -> banPunishment.time, (banPunishment, l) -> banPunishment.time = l))
            .addField("duration", SchemaEntries.longEntry(banPunishment -> banPunishment.duration, (banPunishment, l) -> banPunishment.duration = l))
            .build();

    private String reason;
    private UUID mediator;
    private long time;
    private long duration;

    private BanPunishment() {}

    BanPunishment(String reason, UUID mediator, long time, long duration) {
        this.reason = reason;
        this.mediator = mediator;
        this.time = time;
        this.duration = duration;
    }

    public String getReason() {
        return reason;
    }

    public UUID getMediator() {
        return mediator;
    }

    public long getTime() {
        return time;
    }

    public long getDuration() {
        return duration;
    }

    public boolean isPermanent() {
        return this.duration < 0;
    }

    static BanPunishment fromJsonReader(JsonReader jsonReader) throws IOException {
        BanPunishment banPunishment = new BanPunishment();
        SCHEMA.populate(banPunishment, jsonReader);
        return banPunishment;
    }

}
