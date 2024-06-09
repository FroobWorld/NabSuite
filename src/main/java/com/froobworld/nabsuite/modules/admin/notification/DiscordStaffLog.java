package com.froobworld.nabsuite.modules.admin.notification;

import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.note.PlayerNote;
import com.froobworld.nabsuite.modules.admin.punishment.PunishmentLogItem;
import com.froobworld.nabsuite.modules.admin.ticket.Ticket;
import com.froobworld.nabsuite.modules.discord.DiscordModule;
import com.froobworld.nabsuite.modules.discord.utils.DiscordUtils;
import com.froobworld.nabsuite.modules.protect.area.Area;
import com.froobworld.nabsuite.util.ConsoleUtils;
import com.froobworld.nabsuite.util.DurationDisplayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.UUID;

public class DiscordStaffLog {
    private final AdminModule adminModule;
    private final DiscordModule discordModule;

    public DiscordStaffLog(AdminModule adminModule) {
        this.adminModule = adminModule;
        this.discordModule = adminModule.getPlugin().getModule(DiscordModule.class);
    }

    public void sendPunishmentLogItemNotification(PunishmentLogItem punishmentLogItem) {
        if (discordModule == null) {
            return;
        }

        Color colour;
        if (punishmentLogItem.getType() == PunishmentLogItem.Type.BAN || punishmentLogItem.getType() == PunishmentLogItem.Type.MUTE || punishmentLogItem.getType() == PunishmentLogItem.Type.JAIL || punishmentLogItem.getType() == PunishmentLogItem.Type.RESTRICTED || punishmentLogItem.getType() == PunishmentLogItem.Type.CONFINED) {
            colour = Color.RED;
        } else if (punishmentLogItem.getType() == PunishmentLogItem.Type.WARN || punishmentLogItem.getType() == PunishmentLogItem.Type.NOTE_ADDED) {
            colour = Color.YELLOW;
        } else {
            colour = Color.MAGENTA;
        }

        TextChannel channel = discordModule.getDiscordBot().getStaffLogChannel();
        if (channel != null) {
            String mediator = ConsoleUtils.CONSOLE_UUID.equals(punishmentLogItem.getMediator()) ? "Console" : adminModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(punishmentLogItem.getMediator()).getLastName();
            String subject = adminModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(punishmentLogItem.getSubject()).getLastName();
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setColor(colour)
                    .setTitle("Punishment log")
                    .setThumbnail(getSkinUrl(punishmentLogItem.getSubject()))
                    .addField("Subject", MarkdownSanitizer.escape(subject), true)
                    .addField("Type", punishmentLogItem.getType().toString(), true)
                    .addField("Mediator", MarkdownSanitizer.escape(mediator), true);
            if (punishmentLogItem.getDuration() > 0) {
                embedBuilder.addField("Duration", DurationDisplayer.asDurationString(punishmentLogItem.getDuration()), true);
            }

            if (punishmentLogItem.getReason() != null) {
                embedBuilder.addField("Reason", MarkdownSanitizer.escape(punishmentLogItem.getReason()), true);
            }
            channel.sendMessageEmbeds(embedBuilder.build()).queue();
        }
    }

    public void sendTicketCreationNotification(Ticket ticket) {
        if (discordModule == null) {
            return;
        }

        TextChannel channel = discordModule.getDiscordBot().getStaffLogChannel();
        if (channel != null) {
            String creator = ConsoleUtils.CONSOLE_UUID.equals(ticket.getCreator()) ? "System generated" : adminModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(ticket.getCreator()).getLastName();

            EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Ticket opened")
                    .setColor(Color.CYAN)
                    .setThumbnail(getSkinUrl(ticket.getCreator()))
                    .addField("Creator", MarkdownSanitizer.escape(creator), true)
                    .addField("Id", ticket.getId() + "", true)
                    .addField("Message", MarkdownSanitizer.escape(ticket.getMessage()), true);

            channel.sendMessageEmbeds(embedBuilder.build()).queue();
        }
    }

    public void sendTicketClosureNotification(Ticket ticket, CommandSender resolver, String closureMessage) {
        if (discordModule == null) {
            return;
        }

        TextChannel channel = discordModule.getDiscordBot().getStaffLogChannel();
        if (channel != null) {
            String resolverName = resolver instanceof Player ? resolver.getName() : "Console";

            EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Ticket closed")
                    .setColor(Color.GREEN)
                    .setThumbnail(getSkinUrl(ConsoleUtils.getSenderUUID(resolver)))
                    .addField("Closed by", MarkdownSanitizer.escape(resolverName), true)
                    .addField("Id", ticket.getId() + "", true)
                    .addField("Resolution", MarkdownSanitizer.escape(closureMessage), true);

            channel.sendMessageEmbeds(embedBuilder.build()).queue();
        }
    }

    public void sendAreaRequestNotification(Area area) {
        if (discordModule == null) {
            return;
        }

        TextChannel channel = discordModule.getDiscordBot().getStaffLogChannel();
        if (channel != null) {
            String creator = ConsoleUtils.CONSOLE_UUID.equals(area.getCreator()) ? "Console" : adminModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(area.getCreator()).getLastName();

            EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Area requested")
                    .setColor(Color.CYAN)
                    .setThumbnail(getSkinUrl(area.getCreator()))
                    .addField("Creator", MarkdownSanitizer.escape(creator), true)
                    .addField("Area name", MarkdownSanitizer.escape(area.getName()), true);
            channel.sendMessageEmbeds(embedBuilder.build()).queue();
        }
    }

    public void sendAreaRequestHandleNotification(CommandSender handler, Area area, boolean approved, String reason) {
        if (discordModule == null) {
            return;
        }

        TextChannel channel = discordModule.getDiscordBot().getStaffLogChannel();
        if (channel != null) {
            String handlerName = handler instanceof Player ? handler.getName() : "Console";

            EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Area request " + (approved ? "approved" : "denied"))
                    .setColor(approved ? Color.GREEN : Color.RED)
                    .setThumbnail(getSkinUrl(ConsoleUtils.getSenderUUID(handler)))
                    .addField("Reviewer", MarkdownSanitizer.escape(handlerName), true)
                    .addField("Area name", MarkdownSanitizer.escape(area.getName()), true);
            if (reason != null) {
                embedBuilder.addField("Reason", MarkdownSanitizer.escape(reason), true);
            }
            channel.sendMessageEmbeds(embedBuilder.build()).queue();
        }
    }

    public void sendNoteCreationNotification(PlayerNote note, String subjectName, String creatorName) {
        if (discordModule == null) {
            return;
        }

        TextChannel channel = discordModule.getDiscordBot().getStaffLogChannel();
        if (channel != null) {

            EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Note created")
                    .setColor(Color.YELLOW)
                    .setThumbnail(getSkinUrl(note.getSubject()))
                    .addField("Subject", MarkdownSanitizer.escape(subjectName), true)
                    .addField("Creator", MarkdownSanitizer.escape(creatorName), true)
                    .addField("Note", MarkdownSanitizer.escape(note.getMessage()), true);

            channel.sendMessageEmbeds(embedBuilder.build()).queue();
        }
    }

    public void sendNotification(UUID subject, Color colour, String notification) {
        if (discordModule == null) {
            return;
        }

        TextChannel channel = discordModule.getDiscordBot().getStaffLogChannel();
        if (channel != null) {
            MessageEmbed embed = new EmbedBuilder()
                    .setColor(colour)
                    .setAuthor(notification, null, subject == null ? null : getHeadUrl(subject))
                    .build();
            channel.sendMessageEmbeds(embed).queue();
        }
    }

    private static String getHeadUrl(UUID uuid) {
        if (ConsoleUtils.CONSOLE_UUID.equals(uuid)) {
            return DiscordUtils.getAvatarUrl(UUID.fromString("f78a4d8d-d51b-4b39-98a3-230f2de0c670"), 128);
        }
        return DiscordUtils.getAvatarUrl(uuid, 128);
    }

    private static String getSkinUrl(UUID uuid) {
        if (ConsoleUtils.CONSOLE_UUID.equals(uuid)) {
            return DiscordUtils.getAvatarUrl(UUID.fromString("f78a4d8d-d51b-4b39-98a3-230f2de0c670"), 64);
        }
        return DiscordUtils.getAvatarUrl(uuid, 128);
    }

}
