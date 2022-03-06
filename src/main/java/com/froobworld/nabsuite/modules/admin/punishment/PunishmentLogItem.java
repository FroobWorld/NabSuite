package com.froobworld.nabsuite.modules.admin.punishment;

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
        Component mediator = this.mediator.equals(ConsoleUtils.CONSOLE_UUID) ? Component.text("Console", NamedTextColor.BLUE) : Component.text(playerIdentityManager.getPlayerIdentity(this.mediator).getLastName(), NamedTextColor.BLUE);
        Component subject = Component.text(playerIdentityManager.getPlayerIdentity(this.subject).getLastName(), NamedTextColor.RED);
        Component action = Component.empty();
        if (type == Type.BAN) {
            action = Component.text("banned", NamedTextColor.RED);
        } else if (type == Type.MUTE) {
            action = Component.text("muted", NamedTextColor.RED);
        } else if (type == Type.JAIL) {
            action = Component.text("jailed", NamedTextColor.RED);
        } else if (type == Type.UNBAN_AUTOMATIC || type == Type.UNBAN_MANUAL) {
            action = Component.text("unbanned", NamedTextColor.RED);
        } else if (type == Type.UNMUTE_AUTOMATIC || type == Type.UNMUTE_MANUAL) {
            action = Component.text("unmuted", NamedTextColor.RED);
        } else if (type == Type.UNJAIL_AUTOMATIC || type == Type.UNJAIL_MANUAL) {
            action = Component.text("unjailed", NamedTextColor.RED);
        }
        action = Component.text(" was ", NamedTextColor.WHITE).append(action);
        if (duration > 0) {
            action = action.append(Component.text(" for ", NamedTextColor.WHITE))
                    .append(Component.text(DurationDisplayer.asDurationString(duration)));
        }
        if (type == Type.UNBAN_AUTOMATIC || type == Type.UNMUTE_AUTOMATIC || type == Type.UNJAIL_AUTOMATIC) {
            action = action.append(Component.text(" automatically", NamedTextColor.WHITE));
        } else {
            action = action.append(Component.text(" by ", NamedTextColor.WHITE)).append(mediator);
        }
        if (reason != null) {
            action = action.append(Component.text(" for \"", NamedTextColor.WHITE))
                    .append(Component.text(reason))
                    .append(Component.text("\"", NamedTextColor.WHITE));
        }
        action = action.append(Component.space())
                .append(Component.text(DurationDisplayer.asDurationString(System.currentTimeMillis() - time), NamedTextColor.WHITE))
                .append(Component.text(" ago.", NamedTextColor.WHITE));
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
