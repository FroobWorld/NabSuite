package com.froobworld.nabsuite.modules.admin.note;

import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;
import com.froobworld.nabsuite.data.identity.PlayerIdentityManager;
import com.froobworld.nabsuite.util.ConsoleUtils;
import com.froobworld.nabsuite.util.DurationDisplayer;
import com.google.gson.stream.JsonReader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.IOException;
import java.util.UUID;

public class PlayerNote {
    static final SimpleDataSchema<PlayerNote> SCHEMA = new SimpleDataSchema.Builder<PlayerNote>()
            .addField("subject", SchemaEntries.uuidEntry(
                    note -> note.subject,
                    (note, subject) -> note.subject = subject
            ))
            .addField("creator", SchemaEntries.uuidEntry(
                    note -> note.creator,
                    (note, creator) -> note.creator = creator
            ))
            .addField("message", SchemaEntries.stringEntry(
                    note -> note.message,
                    (note, message) -> note.message = message
            ))
            .addField("created", SchemaEntries.longEntry(
                    note -> note.created,
                    (note, created) -> note.created = created
            ))
            .build();

    private UUID subject;
    private UUID creator;
    private String message;
    private long created;

    private PlayerNote() {}

    public PlayerNote(UUID subject, UUID creator, String message) {
        this.subject = subject;
        this.creator = creator;
        this.message = message;
        this.created = System.currentTimeMillis();
    }

    public UUID getSubject() {
        return subject;
    }

    public UUID getCreator() {
        return creator;
    }

    public String getMessage() {
        return message;
    }

    public long getTimeCreated() {
        return created;
    }

    public Component asText(PlayerIdentityManager playerIdentityManager) {
        Component creatorName;
        if (creator.equals(ConsoleUtils.CONSOLE_UUID)) {
            creatorName = Component.text("Server").color(NamedTextColor.RED);
        } else {
            creatorName = playerIdentityManager.getPlayerIdentity(creator).displayName();
        }
        long timeSinceReceipt = System.currentTimeMillis() - created;

        return Component.text("By ", NamedTextColor.GRAY)
                .append(creatorName).color(NamedTextColor.GRAY)
                .append(Component.text(" (", NamedTextColor.GRAY))
                .append(Component.text(DurationDisplayer.asDurationString(timeSinceReceipt), NamedTextColor.GRAY))
                .append(Component.text(" ago)", NamedTextColor.GRAY))
                .append(Component.text(":", NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.text(message, NamedTextColor.WHITE));
    }

    static PlayerNote fromJsonReader(JsonReader jsonReader) throws IOException {
        PlayerNote note = new PlayerNote();
        SCHEMA.populate(note, jsonReader);
        return note;
    }

}
