package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.teleport.request.TeleportRequestHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeleportAcceptCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public TeleportAcceptCommand(BasicsModule basicsModule) {
        super(
                "tpaccept",
                "Accept another player's teleport request.",
                "nabsuite.command.tpaccept",
                Player.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player sender = (Player) context.getSender();
        TeleportRequestHandler.RequestedTeleport requestedTeleport = basicsModule.getTeleportRequestHandler().getRequestedTeleport(sender);
        if (requestedTeleport == null || !requestedTeleport.isValid()) {
            sender.sendMessage(
                    Component.text("You have no teleport requests to accept.").color(NamedTextColor.RED)
            );
            return;
        }
        requestedTeleport.carryOut();
        requestedTeleport.getRequester().sendMessage(
                sender.displayName().append(
                        Component.text(" accepted your teleport request.")
                ).color(NamedTextColor.YELLOW)
        );
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder;
    }
}
