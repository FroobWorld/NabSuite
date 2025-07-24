package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.command.argument.TicketArgument;
import com.froobworld.nabsuite.modules.admin.ticket.Ticket;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.UUID;

public class TicketTeleportCommand extends NabCommand {
    private final AdminModule adminModule;
    private final Cache<Integer, UUID> recentTeleports = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(5)).build();

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
        UUID recentTeleport = recentTeleports.getIfPresent(ticket.getId());
        boolean coordinateWarning = ticket.isOpen() && recentTeleport != null && recentTeleport != player.getUniqueId();
        if (recentTeleport == null) {
            recentTeleports.put(ticket.getId(), player.getUniqueId());
        }

        adminModule.getPlugin().getModule(BasicsModule.class).getPlayerTeleporter().teleportAsync(player, ticket.getLocation()).thenRun(() -> {
            player.sendMessage(Component.text("Whoosh!", NamedTextColor.YELLOW));

            if (coordinateWarning && Bukkit.getPlayer(recentTeleport) instanceof Player other) {
                player.sendMessage(other.displayName()
                        .append(Component.text(" recently teleported to this ticket, please coordinate review.").color(NamedTextColor.YELLOW)));
            }
        });
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
                                (sender, ticket) -> ticket.getLocation() != null,
                                "Ticket does not have a location."
                        )
                ));
    }

    @Override
    public String getUsage() {
        return "/ticket teleport <id>";
    }

}
