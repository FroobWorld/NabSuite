package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.command.argument.TicketArgument;
import com.froobworld.nabsuite.modules.admin.ticket.Ticket;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TicketTeleportCommand extends NabCommand {
    private final AdminModule adminModule;

    public TicketTeleportCommand(AdminModule adminModule) {
        super(
                "teleport",
                "Teleport to a ticket's location.",
                "nabsuite.command.ticket.teleport",
                Player.class
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        Ticket ticket = context.get("ticket");
        adminModule.getPlugin().getModule(BasicsModule.class).getPlayerTeleporter().teleportAsync(player, ticket.getLocation()).thenRun(() -> {
            player.sendMessage(Component.text("Whoosh!", NamedTextColor.YELLOW));
        });
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
        return "/ticket teleport <id>";
    }

}
