package com.froobworld.nabsuite.data.identity;

import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerIdentity {
    private static final SimpleDataSchema<PlayerIdentity> SCHEMA = new SimpleDataSchema.Builder<PlayerIdentity>()
            .addField("uuid", SchemaEntries.uuidEntry(p -> p.uuid, (p, u) -> p.uuid = u))
            .addField("last-name", SchemaEntries.stringEntry(p -> p.lastName, (p, s) -> p.lastName = s))
            .addField("previous-names", SchemaEntries.stringListEntry(p -> p.previousNames, (p, l) -> p.previousNames = l))
            .build();

    private UUID uuid;
    private String lastName;
    private List<String> previousNames;

    private PlayerIdentity() {}

    public PlayerIdentity(UUID uuid, String lastName, List<String> previousNames) {
        this.uuid = uuid;
        this.lastName = lastName;
        this.previousNames = previousNames;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getLastName() {
        return lastName;
    }

    public List<String> getPreviousNames() {
        return new ArrayList<>(previousNames);
    }

    public Player asPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public OfflinePlayer asOfflinePlayer() {
        return Bukkit.getOfflinePlayer(uuid);
    }

    void setLastName(String lastName) {
        this.lastName = lastName;
    }

    void addPreviousName(String previousName) {
        previousNames.add(previousName);
    }

    public Component displayName() {
        Player onlinePlayer = asPlayer();
        return onlinePlayer != null ? onlinePlayer.displayName() : Component.text(getLastName());
    }

    public String toJsonString() {
        try {
            return SCHEMA.toJsonString(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PlayerIdentity fromJsonString(String jsonString) {
        PlayerIdentity playerIdentity = new PlayerIdentity();
        try {
            SCHEMA.populateFromJsonString(playerIdentity, jsonString);
            return playerIdentity;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
