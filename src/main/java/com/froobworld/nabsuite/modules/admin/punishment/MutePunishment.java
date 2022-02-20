package com.froobworld.nabsuite.modules.admin.punishment;

import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.util.UUID;

public class MutePunishment {
    static final SimpleDataSchema<MutePunishment> SCHEMA = new SimpleDataSchema.Builder<MutePunishment>()
            .addField("reason", SchemaEntries.stringEntry(mutePunishment -> mutePunishment.reason, (mutePunishment, s) -> mutePunishment.reason = s))
            .addField("mediator", SchemaEntries.uuidEntry(mutePunishment -> mutePunishment.mediator, (mutePunishment, uuid) -> mutePunishment.mediator = uuid))
            .addField("time", SchemaEntries.longEntry(mutePunishment -> mutePunishment.time, (mutePunishment, l) -> mutePunishment.time = l))
            .addField("duration", SchemaEntries.longEntry(mutePunishment -> mutePunishment.duration, (mutePunishment, l) -> mutePunishment.duration = l))
            .build();

    private String reason;
    private UUID mediator;
    private long time;
    private long duration;

    private MutePunishment() {}

    MutePunishment(String reason, UUID mediator, long time, long duration) {
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

    static MutePunishment fromJsonReader(JsonReader jsonReader) throws IOException {
        MutePunishment mutePunishment = new MutePunishment();
        SCHEMA.populate(mutePunishment, jsonReader);
        return mutePunishment;
    }

}
