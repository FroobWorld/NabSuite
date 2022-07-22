package com.froobworld.nabsuite.modules.admin.notification;

import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.punishment.PunishmentLogItem;
import com.froobworld.nabsuite.modules.admin.ticket.Ticket;
import com.froobworld.nabsuite.modules.protect.area.Area;
import com.froobworld.nabsuite.util.ConsoleUtils;
import com.froobworld.nabsuite.util.DurationDisplayer;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.UUID;

public class DiscordStaffLog {
    private final AdminModule adminModule;

    public DiscordStaffLog(AdminModule adminModule) {
        this.adminModule = adminModule;
    }

    public void sendPunishmentLogItemNotification(PunishmentLogItem punishmentLogItem) {
        DiscordSRV discordSRV = adminModule.getPlugin().getHookManager().getDiscordSRVHook().getDiscordSRV();
        if (discordSRV == null) {
            return;
        }

        Color colour;
        if (punishmentLogItem.getType() == PunishmentLogItem.Type.BAN) {
            colour = Color.RED;
        } else if (punishmentLogItem.getType() == PunishmentLogItem.Type.MUTE || punishmentLogItem.getType() == PunishmentLogItem.Type.JAIL) {
            colour = Color.RED;
        } else {
            colour = Color.MAGENTA;
        }

        TextChannel channel = discordSRV.getDestinationTextChannelForGameChannelName("staff-log");
        if (channel != null) {
            String mediator = ConsoleUtils.CONSOLE_UUID.equals(punishmentLogItem.getMediator()) ? "Console" : adminModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(punishmentLogItem.getMediator()).getLastName();
            String subject = adminModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(punishmentLogItem.getSubject()).getLastName();
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setColor(colour)
                    .setTitle("Punishment log")
                    .setThumbnail(getSkinUrl(punishmentLogItem.getSubject()))
                    .addField("Subject", subject, true)
                    .addField("Type", punishmentLogItem.getType().toString(), true)
                    .addField("Mediator", mediator, true);
            if (punishmentLogItem.getDuration() > 0) {
                embedBuilder.addField("Duration", DurationDisplayer.asDurationString(punishmentLogItem.getDuration()), true);
            }

            if (punishmentLogItem.getReason() != null) {
                embedBuilder.addField("Reason", punishmentLogItem.getReason(), true);
            }
            channel.sendMessageEmbeds(embedBuilder.build()).queue();
        }
    }

    public void sendTicketCreationNotification(Ticket ticket) {
        DiscordSRV discordSRV = adminModule.getPlugin().getHookManager().getDiscordSRVHook().getDiscordSRV();
        if (discordSRV == null) {
            return;
        }

        TextChannel channel = discordSRV.getDestinationTextChannelForGameChannelName("staff-log");
        if (channel != null) {
            String creator = ConsoleUtils.CONSOLE_UUID.equals(ticket.getCreator()) ? "System generated" : adminModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(ticket.getCreator()).getLastName();

            EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Ticket opened")
                    .setColor(Color.CYAN)
                    .setThumbnail(getSkinUrl(ticket.getCreator()))
                    .addField("Creator", creator, true)
                    .addField("Id", ticket.getId() + "", true)
                    .addField("Message", ticket.getMessage(), true);

            channel.sendMessageEmbeds(embedBuilder.build()).queue();
        }
    }

    public void sendTicketClosureNotification(Ticket ticket, CommandSender resolver, String closureMessage) {
        DiscordSRV discordSRV = adminModule.getPlugin().getHookManager().getDiscordSRVHook().getDiscordSRV();
        if (discordSRV == null) {
            return;
        }

        TextChannel channel = discordSRV.getDestinationTextChannelForGameChannelName("staff-log");
        if (channel != null) {
            String resolverName = resolver instanceof Player ? resolver.getName() : "Console";

            EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Ticket closed")
                    .setColor(Color.GREEN)
                    .setThumbnail(getSkinUrl(ConsoleUtils.getSenderUUID(resolver)))
                    .addField("Closed by", resolverName, true)
                    .addField("Id", ticket.getId() + "", true)
                    .addField("Resolution", closureMessage, true);

            channel.sendMessageEmbeds(embedBuilder.build()).queue();
        }
    }

    public void sendAreaRequestNotification(Area area) {
        DiscordSRV discordSRV = adminModule.getPlugin().getHookManager().getDiscordSRVHook().getDiscordSRV();
        if (discordSRV == null) {
            return;
        }

        TextChannel channel = discordSRV.getDestinationTextChannelForGameChannelName("staff-log");
        if (channel != null) {
            String creator = ConsoleUtils.CONSOLE_UUID.equals(area.getCreator()) ? "Console" : adminModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(area.getCreator()).getLastName();

            EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Area requested")
                    .setColor(Color.CYAN)
                    .setThumbnail(getSkinUrl(area.getCreator()))
                    .addField("Creator", creator, true)
                    .addField("Area name", area.getName(), true);
            channel.sendMessageEmbeds(embedBuilder.build()).queue();
        }
    }

    public void sendAreaRequestHandleNotification(CommandSender handler, Area area, boolean approved, String reason) {
        DiscordSRV discordSRV = adminModule.getPlugin().getHookManager().getDiscordSRVHook().getDiscordSRV();
        if (discordSRV == null) {
            return;
        }

        TextChannel channel = discordSRV.getDestinationTextChannelForGameChannelName("staff-log");
        if (channel != null) {
            String handlerName = handler instanceof Player ? handler.getName() : "Console";

            EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Area request " + (approved ? "approved" : "denied"))
                    .setColor(approved ? Color.GREEN : Color.RED)
                    .setThumbnail(getSkinUrl(ConsoleUtils.getSenderUUID(handler)))
                    .addField("Reviewer", handlerName, true)
                    .addField("Area name", area.getName(), true);
            if (reason != null) {
                embedBuilder.addField("Reason", reason, true);
            }
            channel.sendMessageEmbeds(embedBuilder.build()).queue();
        }
    }

    public void sendNotification(UUID subject, Color colour, String notification) {
        DiscordSRV discordSRV = adminModule.getPlugin().getHookManager().getDiscordSRVHook().getDiscordSRV();
        if (discordSRV == null) {
            return;
        }

        TextChannel channel = discordSRV.getDestinationTextChannelForGameChannelName("staff-log");
        if (channel != null) {
            MessageEmbed embed = new EmbedBuilder()
                    .setColor(colour)
                    .setAuthor(notification, null, subject == null ? null : getSkinUrl(subject))
                    .build();
            channel.sendMessageEmbeds(embed).queue();
        }
    }

    private static String getSkinUrl(UUID uuid) {
        if (ConsoleUtils.CONSOLE_UUID.equals(uuid)) {
            return DiscordSRV.getAvatarUrl("ignore", UUID.fromString("f78a4d8d-d51b-4b39-98a3-230f2de0c670"));
        }
        return DiscordSRV.getAvatarUrl("ignore", uuid);
    }

}
