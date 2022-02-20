package com.froobworld.nabsuite.modules.basics.player.mail;

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

public class Mail {
    static final SimpleDataSchema<Mail> SCHEMA = new SimpleDataSchema.Builder<Mail>()
            .addField("timestamp", SchemaEntries.longEntry(
                    mail -> mail.timestamp,
                    (mail, timestamp) -> mail.timestamp = timestamp
            ))
            .addField("sender", SchemaEntries.uuidEntry(
                    mail -> mail.sender,
                    (mail, sender) -> mail.sender = sender
            ))
            .addField("message", SchemaEntries.stringEntry(
                    mail -> mail.message,
                    (mail, message) -> mail.message = message
            ))
            .build();

    private long timestamp;
    private UUID sender;
    private String message;

    private Mail() {}

    public Mail(long timestamp, UUID sender, String message) {
        this.timestamp = timestamp;
        this.sender = sender;
        this.message = message;
    }

    public Component asText(PlayerIdentityManager playerIdentityManager) {
        Component senderName;
        if (sender.equals(ConsoleUtils.CONSOLE_UUID)) {
            senderName = Component.text("Server").color(NamedTextColor.RED);
        } else {
            senderName = playerIdentityManager.getPlayerIdentity(sender).displayName();
        }
        long timeSinceReceipt = System.currentTimeMillis() - timestamp;

        return Component.text("From ", NamedTextColor.GRAY)
                .append(senderName).color(NamedTextColor.GRAY)
                .append(Component.text(" (", NamedTextColor.GRAY))
                .append(Component.text(DurationDisplayer.asMinutesHoursDays(timeSinceReceipt), NamedTextColor.GRAY))
                .append(Component.text(" ago)", NamedTextColor.GRAY))
                .append(Component.text(":", NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.text(message, NamedTextColor.WHITE));
    }

    public long getTimestamp() {
        return timestamp;
    }

    public UUID getSender() {
        return sender;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    static Mail fromJsonReader(JsonReader jsonReader) throws IOException {
        Mail mail = new Mail();
        SCHEMA.populate(mail, jsonReader);
        return mail;
    }
}
