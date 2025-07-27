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

public class TicketEscalateCommand extends NabCommand {
    private final AdminModule adminModule;

    public TicketEscalateCommand(AdminModule adminModule) {
        super(
                "escalate",
                "Escalate a ticket with an optional note.",
                "nabsuite.command.ticket.escalate",
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
        if (currentIndex >= 0 && currentIndex < adminModule.getAdminConfig().ticketLevels.get().size() - 1) {
            ticket.setLevel(adminModule.getAdminConfig().ticketLevels.get().get(currentIndex + 1));
            ticket.addNote(ConsoleUtils.getSenderUUID(context.getSender()), "(Escalated ticket" + note.map(s -> " with note: '" + s + "'").orElse("") + ")");
            context.getSender().sendMessage(Component.text("Ticket escalated.", NamedTextColor.YELLOW));
            adminModule.getDiscordStaffLog().updateTicketNotification(ticket);
            adminModule.getStaffTaskManager().notifyNewTask(ticket.getPermission());
        } else {
            context.getSender().sendMessage(Component.text("Unable to determine level to escalate to.", NamedTextColor.RED));
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
                                (context, ticket) -> ticket.canEscalate(),
                                "Ticket cannot be escalated"
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
        return "/ticket escalate <id> [note]";
    }

}
