package com.froobworld.nabsuite.modules.admin.greylist;

import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;

import java.io.IOException;
import java.util.UUID;

public class PlayerGreylistData {
    private static final SimpleDataSchema<PlayerGreylistData> SCHEMA = new SimpleDataSchema.Builder<PlayerGreylistData>()
            .addField(
                    "uuid",
                    SchemaEntries.uuidEntry(
                            data -> data.uuid,
                            (data, uuid) -> data.uuid = uuid
                    )
            )
            .addField(
                    "greylisted",
                    SchemaEntries.booleanEntry(
                            data -> data.greylisted,
                            (data, greylisted) -> data.greylisted = greylisted
                    )
            )
            .addField(
                    "requested-removal",
                    SchemaEntries.booleanEntry(
                            data -> data.requestedRemoval,
                            (data, requestedRemoval) -> data.requestedRemoval = requestedRemoval
                    )
            )
            .addField(
                    "informed-of-removal",
                    SchemaEntries.booleanEntry(
                            data -> data.informedOfRemoval,
                            (data, informedOfRemoval) -> data.informedOfRemoval = informedOfRemoval
                    )
            )
            .build();
    private final GreylistManager greylistManager;
    private UUID uuid;
    private boolean greylisted;
    private boolean requestedRemoval;
    private boolean informedOfRemoval;

    private PlayerGreylistData(GreylistManager greylistManager) {
        this.greylistManager = greylistManager;
    }

    public PlayerGreylistData(GreylistManager greylistManager, UUID uuid, boolean greylisted, boolean requestedRemoval) {
        this.greylistManager = greylistManager;
        this.uuid = uuid;
        this.greylisted = greylisted;
        this.requestedRemoval = requestedRemoval;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isGreylisted() {
        return greylisted;
    }

    public boolean hasRequestedRemoval() {
        return requestedRemoval;
    }

    boolean isInformedOfRemoval() {
        return informedOfRemoval;
    }

    void setGreylisted(boolean greylisted) {
        this.greylisted = greylisted;
        greylistManager.dataSaver.scheduleSave(this);
    }

    void setRequestedRemoval(boolean requestedRemoval) {
        this.requestedRemoval = requestedRemoval;
        greylistManager.dataSaver.scheduleSave(this);
    }

    void setInformedOfRemoval(boolean informedOfRemoval) {
        this.informedOfRemoval = informedOfRemoval;
        greylistManager.dataSaver.scheduleSave(this);
    }

    public String toJsonString() {
        try {
            return SCHEMA.toJsonString(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PlayerGreylistData fromJsonString(GreylistManager greylistManager, String jsonString) {
        PlayerGreylistData data = new PlayerGreylistData(greylistManager);
        try {
            SCHEMA.populateFromJsonString(data, jsonString);
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
