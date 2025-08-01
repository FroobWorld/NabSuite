package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.command.argument.TicketArgument;
import com.froobworld.nabsuite.modules.admin.ticket.Ticket;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.util.ConsoleUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class TicketCloseCommand extends NabCommand {
    private final AdminModule adminModule;

    public TicketCloseCommand(AdminModule adminModule) {
        super(
                "close",
                "Close a ticket with a message.",
                "nabsuite.command.ticket.close",
                CommandSender.class
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Ticket ticket = context.get("ticket");
        String message = context.get("message");
        ticket.close(ConsoleUtils.getSenderUUID(context.getSender()), message);
        if (!ticket.getCreator().equals(ConsoleUtils.CONSOLE_UUID)) {
            adminModule.getPlugin().getModule(BasicsModule.class).getMailCentre().sendSystemMail(ticket.getCreator(), "Your ticket with id " + ticket.getId() + " was closed with the message '" + message + "'.");
        }
        context.getSender().sendMessage(Component.text("Ticket closed.", NamedTextColor.YELLOW));
        adminModule.getDiscordStaffLog().sendTicketClosureNotification(ticket, context.getSender(), message);
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
                        ),
                        new ArgumentPredicate<>(
                                true,
                                (context, ticket) -> ticket.isOpen(),
                                "That ticket has already been closed"
                        )
                ))
                .argument(StringArgument.<CommandSender>newBuilder("message").greedy());
    }

    @Override
    public String getUsage() {
        return "/ticket close <id> <message>";
    }

}
