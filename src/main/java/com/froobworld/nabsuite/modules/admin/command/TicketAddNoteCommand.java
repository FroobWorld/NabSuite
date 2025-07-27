package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.command.argument.TicketArgument;
import com.froobworld.nabsuite.modules.admin.ticket.Ticket;
import com.froobworld.nabsuite.util.ConsoleUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class TicketAddNoteCommand extends NabCommand {
    private final AdminModule adminModule;

    public TicketAddNoteCommand(AdminModule adminModule) {
        super(
                "addnote",
                "Add a note to a ticket.",
                "nabsuite.command.ticket.addnote",
                CommandSender.class
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Ticket ticket = context.get("ticket");
        String message = context.get("message");
        ticket.addNote(ConsoleUtils.getSenderUUID(context.getSender()), message);
        adminModule.getDiscordStaffLog().updateTicketNotification(ticket);
        context.getSender().sendMessage(Component.text("Note added.", NamedTextColor.YELLOW));
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(new TicketArgument<>(
                        true,
                        "ticket",
                        adminModule.getTicketManager(),
                        new ArgumentPredicate<>(
                                true,
                                (context, ticket) -> context.getSender().hasPermission(ticket.getPermission()),
                                "You don't have permission for that ticket"
                        )
                ))
                .argument(StringArgument.<CommandSender>newBuilder("message").greedy());
    }

    @Override
    public String getUsage() {
        return "/ticket addnote <id> <message>";
    }

}
