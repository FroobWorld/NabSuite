package com.froobworld.nabsuite.modules.admin.notification;

import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.deputy.DeputyPlayer;
import com.froobworld.nabsuite.modules.admin.note.PlayerNote;
import com.froobworld.nabsuite.modules.admin.punishment.PunishmentLogItem;
import com.froobworld.nabsuite.modules.admin.ticket.Ticket;
import com.froobworld.nabsuite.modules.discord.DiscordModule;
import com.froobworld.nabsuite.modules.discord.bot.command.DiscordCommandSender;
import com.froobworld.nabsuite.modules.discord.utils.DiscordUtils;
import com.froobworld.nabsuite.modules.protect.area.Area;
import com.froobworld.nabsuite.util.ConsoleUtils;
import com.froobworld.nabsuite.util.DurationDisplayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.OfflinePlayer;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DiscordStaffLog {
    private final AdminModule adminModule;
    private final DiscordModule discordModule;

    public DiscordStaffLog(AdminModule adminModule) {
        this.adminModule = adminModule;
        this.discordModule = adminModule.getPlugin().getModule(DiscordModule.class);
    }

    public void postStartup() {
        if (this.discordModule != null) {
            this.discordModule.getDiscordBot().addButtonListener("stafflog-ticket", this::showTicketModalWithMessage);
            this.discordModule.getDiscordBot().addModalListener("stafflog-ticket", this::handleTicketModal);
        }
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
                    .addField("Subject", DiscordUtils.escapeMarkdown(subject), true)
                    .addField("Type", punishmentLogItem.getType().toString(), true)
                    .addField("Mediator", DiscordUtils.escapeMarkdown(mediator), true);
            if (punishmentLogItem.getDuration() > 0) {
                embedBuilder.addField("Duration", DurationDisplayer.asDurationString(punishmentLogItem.getDuration()), true);
            }

            if (punishmentLogItem.getReason() != null) {
                embedBuilder.addField("Reason", DiscordUtils.escapeMarkdown(punishmentLogItem.getReason()), true);
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
            channel.sendMessage(buildTicketNotification(ticket))
                    .onSuccess(result -> Bukkit.getScheduler().runTask(adminModule.getPlugin(), () -> ticket.setStaffLogId(result.getIdLong())))
                    .queue();
        }
    }

    public void updateTicketNotification(Ticket ticket) {
        if (discordModule == null || ticket.getStaffLogId() == null) {
            return;
        }
        TextChannel channel = discordModule.getDiscordBot().getStaffLogChannel();
        if (channel != null) {
            channel.editMessageById(ticket.getStaffLogId(), MessageEditBuilder.fromCreateData(buildTicketNotification(ticket)).build()).queue();
        }
    }

    private MessageCreateData buildTicketNotification(Ticket ticket) {
        String creator = ConsoleUtils.CONSOLE_UUID.equals(ticket.getCreator()) ? "System generated" : adminModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(ticket.getCreator()).getLastName();

        String notes = ticket.getNotes().stream().map(
                note ->  "**" +
                        DiscordUtils.escapeMarkdown(ConsoleUtils.CONSOLE_UUID.equals(note.getCreator()) ? "Console" : adminModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(note.getCreator()).getLastName()) +
                        "**: " +
                        DiscordUtils.escapeMarkdown(note.getMessage())
        ).collect(Collectors.joining("\n"));
        EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Ticket opened")
                .setColor(Color.CYAN)
                .setThumbnail(getSkinUrl(ticket.getCreator()))
                .addField("Creator", DiscordUtils.escapeMarkdown(creator), true)
                .addField("Id", ticket.getId() + "", true)
                .addField("Message", DiscordUtils.escapeMarkdown(ticket.getMessage()), true);
        if (!notes.isBlank()) {
                embedBuilder.addField("Notes", notes, false);
        }

        MessageCreateBuilder message = new MessageCreateBuilder();
        message.addEmbeds(embedBuilder.build());

        if (ticket.isOpen()) {
            List<ItemComponent> buttons = new LinkedList<>();
            if (ticket.canDelegate()) {
                buttons.add(Button.primary("stafflog-ticket:delegate:" + ticket.getId(), "Delegate"));
            }
            /* Might get busy/confusing with too many actions? Escalate will probably not be used a lot
            if (ticket.canEscalate()) {
                buttons.add(Button.primary("stafflog-ticket:escalate:" + ticket.getId(), "Escalate"));
            }*/
            buttons.add(Button.secondary("stafflog-ticket:addnote:" + ticket.getId(), "Add Note"));
            buttons.add(Button.danger("stafflog-ticket:close:" + ticket.getId(), "Close"));
            message.addComponents(ActionRow.of(buttons));
        }

        return message.build();
    }

    public void sendTicketClosureNotification(Ticket ticket, CommandSender resolver, String closureMessage) {
        if (discordModule == null) {
            return;
        }
        updateTicketNotification(ticket);

        TextChannel channel = discordModule.getDiscordBot().getStaffLogChannel();
        if (channel != null) {
            String resolverName = resolver instanceof OfflinePlayer ? resolver.getName() : "Console";

            EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Ticket closed")
                    .setColor(Color.GREEN)
                    .setThumbnail(getSkinUrl(ConsoleUtils.getSenderUUID(resolver)))
                    .addField("Closed by", DiscordUtils.escapeMarkdown(resolverName), true)
                    .addField("Id", ticket.getId() + "", true)
                    .addField("Resolution", DiscordUtils.escapeMarkdown(closureMessage), true);

            channel.sendMessageEmbeds(embedBuilder.build()).queue();
        }
    }

    public void sendDeputyChangeNotification(CommandSender sender, DeputyPlayer previous, DeputyPlayer current) {
        if (discordModule == null) {
            return;
        }

        TextChannel channel = discordModule.getDiscordBot().getStaffLogChannel();
        if (channel != null && (previous != null || current != null)) {
            String resolverName = sender instanceof OfflinePlayer ? sender.getName() : "Console";
            DeputyPlayer deputy = previous == null ? current : previous;

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setThumbnail(getSkinUrl(deputy.getUuid()))
                    .addField("Player", DiscordUtils.escapeMarkdown(deputy.getPlayerIdentity().getLastName()), true)
                    .addField("Deputy level", DiscordUtils.escapeMarkdown(deputy.getDeputyLevel().getName()), true)
                    .addField("Mediator", DiscordUtils.escapeMarkdown(resolverName), true);

            if (current == null) {
                embedBuilder.setColor(Color.RED)
                        .setTitle("Deputy removed")
                        .addField("Reason", DiscordUtils.escapeMarkdown(previous.getExpiry() < System.currentTimeMillis() ?
                                "Expired" :
                                "Manually removed"
                        ), true);
            } else {
                String durationString = DurationDisplayer.asDurationString(
                        // Round up to the nearest minute
                        Math.ceilDiv(current.getExpiry() - System.currentTimeMillis(), 60000) * 60000
                );
                embedBuilder.setColor(Color.GREEN)
                        .setTitle(previous == null ? "Deputy added" : "Deputy renewed")
                        .addField("Duration", DiscordUtils.escapeMarkdown(durationString + " (expires " + "<t:" + current.getExpiry() / 1000 + ":f>)"), true);
            }

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
                    .addField("Creator", DiscordUtils.escapeMarkdown(creator), true)
                    .addField("Area name", DiscordUtils.escapeMarkdown(area.getName()), true);
            channel.sendMessageEmbeds(embedBuilder.build()).queue();
        }
    }

    public void sendAreaRequestHandleNotification(CommandSender handler, Area area, boolean approved, String reason) {
        if (discordModule == null) {
            return;
        }

        TextChannel channel = discordModule.getDiscordBot().getStaffLogChannel();
        if (channel != null) {
            String handlerName = handler instanceof OfflinePlayer ? handler.getName() : "Console";

            EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Area request " + (approved ? "approved" : "denied"))
                    .setColor(approved ? Color.GREEN : Color.RED)
                    .setThumbnail(getSkinUrl(ConsoleUtils.getSenderUUID(handler)))
                    .addField("Reviewer", DiscordUtils.escapeMarkdown(handlerName), true)
                    .addField("Area name", DiscordUtils.escapeMarkdown(area.getName()), true);
            if (reason != null) {
                embedBuilder.addField(approved ? "Message" : "Reason", DiscordUtils.escapeMarkdown(reason), true);
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
                    .addField("Subject", DiscordUtils.escapeMarkdown(subjectName), true)
                    .addField("Creator", DiscordUtils.escapeMarkdown(creatorName), true)
                    .addField("Note", DiscordUtils.escapeMarkdown(note.getMessage()), true);

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
            return DiscordUtils.getAvatarUrl(UUID.fromString("f78a4d8d-d51b-4b39-98a3-230f2de0c670"), 64);
        }
        return DiscordUtils.getAvatarUrl(uuid, 64);
    }

    private static String getSkinUrl(UUID uuid) {
        if (ConsoleUtils.CONSOLE_UUID.equals(uuid)) {
            return DiscordUtils.getAvatarUrl(UUID.fromString("f78a4d8d-d51b-4b39-98a3-230f2de0c670"), 64);
        }
        return DiscordUtils.getAvatarUrl(uuid, 64);
    }

    private void handleTicketModal(DiscordCommandSender sender, ModalInteractionEvent event) {
        try {
            String[] parts = event.getModalId().split(":");
            String action = parts.length > 1 ? parts[1].replaceAll("[^a-zA-Z0-9]", "") : "";
            int id = parts.length > 2 ? Integer.parseInt(parts[2]) : -1;
            if (id <= 0) {
                throw new NumberFormatException();
            }
            ModalMapping messageMapping = event.getValue("message");
            String message = messageMapping != null ?
                    messageMapping.getAsString().replaceAll("[\r\n]", " ") :
                    "";
            event.deferReply(true).queue(hook -> {
                sender.setHook(hook);
                Bukkit.getScheduler().runTask(adminModule.getPlugin(), () -> {
                    try {
                        Ticket ticket = adminModule.getTicketManager().getTicket(id);
                        if (ticket == null || !ticket.isOpen()) {
                            sender.replyError("Ticket not found or already closed");
                            return;
                        }

                        String command = "ticket "+action+" " + ticket.getId() + " " + message;
                        adminModule.getPlugin().getSLF4JLogger().info(
                                "DiscordStaffLog: {} issued server command: /{}",
                                sender.getName(),
                                command
                        );
                        Bukkit.dispatchCommand(sender, command);
                    } catch (Throwable e) {
                        sender.replyError(e.getMessage());
                    }
                });
            });

        } catch (NumberFormatException e) {
            event.reply("Invalid ticket id")
                    .setEphemeral(true)
                    .queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
        }

    }

    private void showTicketModalWithMessage(DiscordCommandSender sender, ButtonInteractionEvent event) {
        try {
            String[] parts = event.getComponentId().split(":");
            String action = parts.length > 1 ? parts[1] : "";
            event.replyModal(Modal.create(event.getComponentId(), switch(action) {
                                case "addnote" -> "Add Note to Ticket";
                                case "close" -> "Close Ticket";
                                case "delegate" -> "Delegate Ticket";
                                case "escalate" -> "Escalate Ticket";
                                default -> throw new IllegalArgumentException();
                            })
                    .addComponents(ActionRow.of(
                            TextInput.create("message", "Message", TextInputStyle.PARAGRAPH)
                                    .setRequired(action.equals("addnote") || action.equals("close"))
                                    .setMaxLength(255)
                                    .build()
                    ))
                    .build()
            ).queue();

        } catch (Exception e) {
            event.reply("Invalid action or ticket id")
                    .setEphemeral(true)
                    .queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
        }
    }

}
