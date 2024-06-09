package com.froobworld.nabsuite.modules.discord.bot.linking;

import com.froobworld.nabsuite.data.DataLoader;
import com.froobworld.nabsuite.data.DataSaver;
import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;
import com.froobworld.nabsuite.modules.discord.DiscordModule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LinkedAccountDataManager {
    private static final SimpleDataSchema<LinkedAccountDataManager> SCHEMA = new SimpleDataSchema.Builder<LinkedAccountDataManager>()
            .addField("linked-accounts", SchemaEntries.listEntry(
                    linkedAccountDataManager -> linkedAccountDataManager.linkedAccountDataList,
                    (linkedAccountDataManager, linkedAccountDataList) -> linkedAccountDataManager.linkedAccountDataList = linkedAccountDataList,
                    (jsonReader, linkedAccountDataManager) -> LinkedAccountData.fromJsonReader(jsonReader),
                    LinkedAccountData.SCHEMA::write
            ))
            .build();
    private List<LinkedAccountData> linkedAccountDataList = new ArrayList<>();
    private final File file;
    private final DataSaver dataSaver;

    public LinkedAccountDataManager(DiscordModule discordModule) {
        this.file = new File(discordModule.getDataFolder(), "linked-accounts.json");
        dataSaver = new DataSaver(discordModule.getPlugin(), 1200 * 5);
        dataSaver.addDataType(LinkedAccountDataManager.class, linkedAccountDataManager -> linkedAccountDataManager.toJsonString().getBytes(), p -> file);
        dataSaver.start();
        load();
    }

    public List<LinkedAccountData> getAllLinkedAccounts() {
        return new ArrayList<>(linkedAccountDataList);
    }

    public void addLinkedAccount(UUID minecraftUuid, String discordId) {
        // remove any conflicting linked accounts
        linkedAccountDataList.removeIf(linkedAccountData -> linkedAccountData.getMinecraftUuid().equals(minecraftUuid) || linkedAccountData.getDiscordId() == discordId);

        linkedAccountDataList.add(new LinkedAccountData(minecraftUuid, discordId));
        dataSaver.scheduleSave(this);
    }

    public LinkedAccountData getLinkedAccountData(UUID minecraftUuid) {
        for (LinkedAccountData linkedAccountData : linkedAccountDataList) {
            if (linkedAccountData.getMinecraftUuid().equals(minecraftUuid)) {
                return linkedAccountData;
            }
        }
        return null;
    }

    public LinkedAccountData getLinkedAccountData(String discordId) {
        for (LinkedAccountData linkedAccountData : linkedAccountDataList) {
            if (linkedAccountData.getDiscordId().equals(discordId)) {
                return linkedAccountData;
            }
        }
        return null;
    }

    public void removeLinkedAccount(UUID minecraftUuid) {
        if (linkedAccountDataList.removeIf(linkedAccountData -> linkedAccountData.getMinecraftUuid().equals(minecraftUuid))) {
            dataSaver.scheduleSave(this);
        }
    }

    public void removeLinkedAccount(String discordId) {
        if (linkedAccountDataList.removeIf(linkedAccountData -> linkedAccountData.getDiscordId().equals(discordId))) {
            dataSaver.scheduleSave(this);
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
            return SCHEMA.toJsonString(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private LinkedAccountDataManager populateFromJsonString(String jsonString) {
        try {
            LinkedAccountDataManager.SCHEMA.populateFromJsonString(this, jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

}
