package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.command.argument.TicketArgument;
import com.froobworld.nabsuite.modules.admin.ticket.Ticket;
import com.froobworld.nabsuite.util.ComponentUtils;
import com.froobworld.nabsuite.util.ConsoleUtils;
import com.froobworld.nabsuite.util.DurationDisplayer;
import com.froobworld.nabsuite.util.VectorDisplayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;

public class TicketReadCommand extends NabCommand {
    private final AdminModule adminModule;

    public TicketReadCommand(AdminModule adminModule) {
        super(
                "read",
                "Read a ticket.",
                "nabsuite.command.ticket.read",
                CommandSender.class
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Ticket ticket = context.get("ticket");
        context.getSender().sendMessage(Component.text("------- Ticket id " + ticket.getId() + " -------", NamedTextColor.YELLOW));
        context.getSender().sendMessage(
                Component.text("Status: ", NamedTextColor.YELLOW)
                        .append(Component.text(ticket.isOpen() ? "Open" : "Closed", ticket.isOpen() ? NamedTextColor.GREEN : NamedTextColor.RED))
        );
        context.getSender().sendMessage(
                Component.text("Creator: ", NamedTextColor.YELLOW)
                        .append(Component.text(ticket.getCreator().equals(ConsoleUtils.CONSOLE_UUID) ? "System generated" : adminModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(ticket.getCreator()).getLastName(),NamedTextColor.WHITE))
        );
        context.getSender().sendMessage(
                Component.text("Created: ", NamedTextColor.YELLOW)
                        .append(Component.text(DurationDisplayer.asDurationString(System.currentTimeMillis() - ticket.getTimestamp()) + " ago", NamedTextColor.WHITE))
        );
        context.getSender().sendMessage(
                Component.text("Location: ", NamedTextColor.YELLOW)
                        .append(Component.text(ticket.getLocation().getWorld().getName() + " (" + VectorDisplayer.vectorToString(ticket.getLocation().toVector(), true) + ")", NamedTextColor.WHITE))
                        .append(Component.text(" [Teleport]", NamedTextColor.GRAY, TextDecoration.ITALIC).clickEvent(ClickEvent.runCommand("/ticket teleport " + ticket.getId())))
        );
        context.getSender().sendMessage(
                Component.text("Message: ", NamedTextColor.YELLOW)
                        .append(ComponentUtils.clickableUrls(Component.text(ticket.getMessage(), NamedTextColor.WHITE)))
        );
        if (ticket.getNotes().isEmpty()) {
            return;
        }
        context.getSender().sendMessage(Component.text("Notes:", NamedTextColor.YELLOW));
        for (Ticket.TicketNote note : ticket.getNotes()) {
            String created = DurationDisplayer.asDurationString(System.currentTimeMillis() - note.getTimestamp());
            String creatorName = note.getCreator().equals(ConsoleUtils.CONSOLE_UUID) ? "Console" : adminModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(note.getCreator()).getLastName();
            context.getSender().sendMessage(ComponentUtils.clickableUrls(Component.text("- " + creatorName + " (" + created + " ago): " + note.getMessage())));
        }
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(new TicketArgument<>(
                        true,
                        "ticket",
                        adminModule.getTicketManager()
                ));
    }

    @Override
    public String getUsage() {
        return "/ticket read <id>";
    }

}
