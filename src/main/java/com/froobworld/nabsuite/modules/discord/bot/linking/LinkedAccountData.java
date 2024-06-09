package com.froobworld.nabsuite.modules.discord.bot.linking;

import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.util.UUID;

public class LinkedAccountData {
    static final SimpleDataSchema<LinkedAccountData> SCHEMA = new SimpleDataSchema.Builder<LinkedAccountData>()
            .addField("minecraft-uuid", SchemaEntries.uuidEntry(
                    linkedAccountData -> linkedAccountData.minecraftUuid,
                    (linkedAccountData, minecraftUuid) -> linkedAccountData.minecraftUuid = minecraftUuid
            ))
            .addField("discord-id", SchemaEntries.stringEntry(
                    linkedAccountData -> linkedAccountData.discordId,
                    (linkedAccountData, discordId) -> linkedAccountData.discordId = discordId
            ))
            .build();

    private UUID minecraftUuid;
    private String discordId;

    private LinkedAccountData() {
    }

    public LinkedAccountData(UUID minecraftUuid, String discordId) {
        this.minecraftUuid = minecraftUuid;
        this.discordId = discordId;
    }

    public UUID getMinecraftUuid() {
        return minecraftUuid;
    }

    public String getDiscordId() {
        return discordId;
    }

    static LinkedAccountData fromJsonReader(JsonReader jsonReader) throws IOException {
        LinkedAccountData linkedAccountData = new LinkedAccountData();
        SCHEMA.populate(linkedAccountData, jsonReader);
        return linkedAccountData;
    }

}
