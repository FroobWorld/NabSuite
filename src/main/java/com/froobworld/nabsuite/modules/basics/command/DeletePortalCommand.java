package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.command.argument.PortalArgument;
import com.froobworld.nabsuite.modules.basics.teleport.portal.Portal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class DeletePortalCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public DeletePortalCommand(BasicsModule basicsModule) {
        super(
                "delportal",
                "Delete a portal.",
                "nabsuite.command.delportal",
                CommandSender.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Portal portal = context.get("portal");
        basicsModule.getPortalManager().deletePortal(portal);
        context.getSender().sendMessage(
                Component.text("Portal deleted.").color(NamedTextColor.YELLOW)
        );
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(new PortalArgument<>(
                        true,
                        "portal",
                        basicsModule.getPortalManager()
                ));
    }
}
