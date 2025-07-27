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

import java.util.Optional;

public class TicketDelegateCommand extends NabCommand {
    private final AdminModule adminModule;

    public TicketDelegateCommand(AdminModule adminModule) {
        super(
                "delegate",
                "Delegate a ticket with an optional note.",
                "nabsuite.command.ticket.delegate",
                CommandSender.class
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Ticket ticket = context.get("ticket");
        Optional<String> note = context.getOptional("note");

        String currentLevel = ticket.getLevel();
        int currentIndex = adminModule.getAdminConfig().ticketLevels.get().indexOf(currentLevel);
        if (currentIndex > 0) {
            ticket.setLevel(adminModule.getAdminConfig().ticketLevels.get().get(currentIndex - 1));
            ticket.addNote(ConsoleUtils.getSenderUUID(context.getSender()), "(Delegated ticket" + note.map(s -> " with note: '" + s + "'").orElse("") + ")");
            context.getSender().sendMessage(Component.text("Ticket delegated.", NamedTextColor.YELLOW));
            adminModule.getDiscordStaffLog().updateTicketNotification(ticket);
            adminModule.getStaffTaskManager().notifyNewTask(
                    ticket.getPermission(),
                    // Only notify players that haven't seen the ticket before
                    player -> !player.hasPermission("nabsuite.ticket." + currentLevel)
            );
        } else {
            context.getSender().sendMessage(Component.text("Unable to determine level to delegate to.", NamedTextColor.RED));
        }
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
                                (context, ticket) -> ticket.canDelegate(),
                                "Ticket cannot be delegated"
                        ),
                        new ArgumentPredicate<>(
                                true,
                                (context, ticket) -> ticket.isOpen(),
                                "That ticket is closed"
                        )
                ))
                .argument(StringArgument.<CommandSender>newBuilder("note").greedy().asOptional());
    }

    @Override
    public String getUsage() {
        return "/ticket delegate <id> [note]";
    }

}
