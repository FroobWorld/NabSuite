package com.froobworld.nabsuite.modules.admin.punishment;

import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;
import com.froobworld.nabsuite.modules.admin.jail.Jail;
import com.froobworld.nabsuite.modules.admin.jail.JailManager;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.util.UUID;

public class JailPunishment {
    static final SimpleDataSchema<JailPunishment> SCHEMA = new SimpleDataSchema.Builder<JailPunishment>()
            .addField("jail", SchemaEntries.stringEntry(jailPunishment -> jailPunishment.jailName, ((jailPunishment, s) -> jailPunishment.jailName = s)))
            .addField("reason", SchemaEntries.stringEntry(jailPunishment -> jailPunishment.reason, (jailPunishment, s) -> jailPunishment.reason = s))
            .addField("mediator", SchemaEntries.uuidEntry(jailPunishment -> jailPunishment.mediator, (jailPunishment, uuid) -> jailPunishment.mediator = uuid))
            .addField("time", SchemaEntries.longEntry(jailPunishment -> jailPunishment.time, (jailPunishment, l) -> jailPunishment.time = l))
            .addField("duration", SchemaEntries.longEntry(jailPunishment -> jailPunishment.duration, (jailPunishment, l) -> jailPunishment.duration = l))
            .addField("confinement", SchemaEntries.booleanEntry(jailPunishment -> jailPunishment.confinement, (jailPunishment, b) -> jailPunishment.confinement = b))
            .build();

    private final JailManager jailManager;
    private String jailName;
    private String reason;
    private UUID mediator;
    private long time;
    private long duration;
    private boolean confinement;

    private JailPunishment(JailManager jailManager) {
        this.jailManager = jailManager;
    }

    JailPunishment(JailManager jailManager, String jailName, String reason, UUID mediator, long time, long duration, boolean confinement) {
        this.jailManager = jailManager;
        this.jailName = jailName;
        this.reason = reason;
        this.mediator = mediator;
        this.time = time;
        this.duration = duration;
        this.confinement = confinement;
    }

    public Jail getJail() {
        return jailManager.getJail(jailName);
    }

    void setJail(Jail jail) {
        this.jailName = jail.getName();
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

    public boolean isConfinement() {
        return confinement;
    }

    static JailPunishment fromJsonReader(JailManager jailManager, JsonReader jsonReader) throws IOException {
        JailPunishment jailPunishment = new JailPunishment(jailManager);
        SCHEMA.populate(jailPunishment, jsonReader);
        return jailPunishment;
    }

}
