package com.froobworld.nabsuite.modules.admin.punishment;

import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.util.UUID;

public class RestrictionPunishment {
    static final SimpleDataSchema<RestrictionPunishment> SCHEMA = new SimpleDataSchema.Builder<RestrictionPunishment>()
            .addField("reason", SchemaEntries.stringEntry(restrictionPunishment -> restrictionPunishment.reason, (restrictionPunishment, s) -> restrictionPunishment.reason = s))
            .addField("mediator", SchemaEntries.uuidEntry(restrictionPunishment -> restrictionPunishment.mediator, (restrictionPunishment, uuid) -> restrictionPunishment.mediator = uuid))
            .addField("time", SchemaEntries.longEntry(restrictionPunishment -> restrictionPunishment.time, (restrictionPunishment, l) -> restrictionPunishment.time = l))
            .build();

    private String reason;
    private UUID mediator;
    private long time;

    private RestrictionPunishment() {}

    RestrictionPunishment(String reason, UUID mediator, long time) {
        this.reason = reason;
        this.mediator = mediator;
        this.time = time;
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

    static RestrictionPunishment fromJsonReader(JsonReader jsonReader) throws IOException {
        RestrictionPunishment restrictionPunishment = new RestrictionPunishment();
        SCHEMA.populate(restrictionPunishment, jsonReader);
        return restrictionPunishment;
    }

}
