package com.froobworld.nabsuite.modules.admin.punishment;

import com.froobworld.nabsuite.data.SchemaEntries;
import com.froobworld.nabsuite.data.SimpleDataSchema;
import com.froobworld.nabsuite.data.identity.PlayerIdentityManager;
import com.froobworld.nabsuite.util.ConsoleUtils;
import com.froobworld.nabsuite.util.DurationDisplayer;
import com.google.gson.stream.JsonReader;
import net.kyori.adventure.text.Component;

import java.io.IOException;
import java.util.UUID;

public class PunishmentLogItem {
    static final SimpleDataSchema<PunishmentLogItem> SCHEMA = new SimpleDataSchema.Builder<PunishmentLogItem>()
            .addField("subject", SchemaEntries.uuidEntry(
                    punishmentLogItem -> punishmentLogItem.subject,
                    (punishmentLogItem, subject) -> punishmentLogItem.subject = subject
            ))
            .addField("mediator", SchemaEntries.uuidEntry(
                    punishmentLogItem -> punishmentLogItem.mediator,
                    (punishmentLogItem, mediator) -> punishmentLogItem.mediator = mediator
            ))
            .addField("type", SchemaEntries.enumEntry(
                    Type.class,
                    punishmentLogItem -> punishmentLogItem.type,
                    (punishmentLogItem, type) -> punishmentLogItem.type = type
            ))
            .addField("time", SchemaEntries.longEntry(
                    punishmentLogItem -> punishmentLogItem.time,
                    (punishmentLogItem, time) -> punishmentLogItem.time = time
            ))
            .addField("duration", SchemaEntries.longEntry(
                    punishmentLogItem -> punishmentLogItem.duration,
                    (punishmentLogItem, duration) -> punishmentLogItem.duration = duration
            ))
            .addField("reason", SchemaEntries.stringEntry(
                    punishmentLogItem -> punishmentLogItem.reason,
                    (punishmentLogItem, reason) -> punishmentLogItem.reason = reason
            ))
            .build();

    private final PlayerIdentityManager playerIdentityManager;
    private UUID subject;
    private UUID mediator;
    private Type type;
    private long time;
    private long duration;
    private String reason;

    private PunishmentLogItem(PlayerIdentityManager playerIdentityManager) {
        this.playerIdentityManager = playerIdentityManager;
    }

    PunishmentLogItem(PlayerIdentityManager playerIdentityManager, Type type, UUID subject, UUID mediator, long time, long duration, String reason) {
        this.playerIdentityManager = playerIdentityManager;
        this.type = type;
        this.subject = subject;
        this.mediator = mediator;
        this.time = time;
        this.duration = duration;
        this.reason = reason;
    }

    public Component toChatMessage() {
        Component mediator = this.mediator.equals(ConsoleUtils.CONSOLE_UUID) ? Component.text("Console") : playerIdentityManager.getPlayerIdentity(this.mediator).displayName();
        Component subject = playerIdentityManager.getPlayerIdentity(this.subject).displayName();
        Component action = Component.empty();
        if (type == Type.BAN) {
            action = Component.text(" was banned");
        } else if (type == Type.MUTE) {
            action = Component.text(" was muted");
        } else if (type == Type.JAIL) {
            action = Component.text(" was jailed");
        } else if (type == Type.UNBAN_AUTOMATIC || type == Type.UNBAN_MANUAL) {
            action = Component.text(" was unbanned");
        } else if (type == Type.UNMUTE_AUTOMATIC || type == Type.UNMUTE_MANUAL) {
            action = Component.text(" was unmuted");
        } else if (type == Type.UNJAIL_AUTOMATIC || type == Type.UNJAIL_MANUAL) {
            action = Component.text(" was unjailed");
        }
        if (duration > 0) {
            action = action.append(Component.text(" for "))
                    .append(Component.text(DurationDisplayer.asDurationString(duration)));
        }
        if (type == Type.UNBAN_AUTOMATIC || type == Type.UNMUTE_AUTOMATIC || type == Type.UNJAIL_AUTOMATIC) {
            action = action.append(Component.text(" automatically"));
        } else {
            action = action.append(Component.text(" by ")).append(mediator);
        }
        if (reason != null) {
            action = action.append(Component.text(" for \""))
                    .append(Component.text(reason))
                    .append(Component.text("\""));
        }
        action = action.append(Component.space())
                .append(Component.text(DurationDisplayer.asDurationString(System.currentTimeMillis() - time)))
                .append(Component.text(" ago."));
        return subject.append(action);
    }

    public enum Type {
        BAN,
        MUTE,
        JAIL,
        UNBAN_AUTOMATIC,
        UNBAN_MANUAL,
        UNMUTE_AUTOMATIC,
        UNMUTE_MANUAL,
        UNJAIL_AUTOMATIC,
        UNJAIL_MANUAL
    }

    static PunishmentLogItem fromJsonReader(PlayerIdentityManager playerIdentityManager, JsonReader jsonReader) throws IOException {
        PunishmentLogItem punishmentLogItem = new PunishmentLogItem(playerIdentityManager);
        SCHEMA.populate(punishmentLogItem, jsonReader);
        return punishmentLogItem;
    }

}
