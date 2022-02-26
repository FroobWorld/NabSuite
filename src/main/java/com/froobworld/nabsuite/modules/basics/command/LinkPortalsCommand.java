package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.command.argument.PortalArgument;
import com.froobworld.nabsuite.modules.basics.teleport.portal.Portal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class LinkPortalsCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public LinkPortalsCommand(BasicsModule basicsModule) {
        super(
                "linkportals",
                "Link two portals together.",
                "nabsuite.command.linkportals",
                CommandSender.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Portal portal1 = context.get("portal1");
        Portal portal2 = context.get("portal2");
        portal1.setLink(portal2);
        context.getSender().sendMessage(Component.text("Portals linked.", NamedTextColor.YELLOW));
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(new PortalArgument<>(
                        true,
                        "portal1",
                        basicsModule.getPortalManager()
                ))
                .argument(new PortalArgument<>(
                        true,
                        "portal2",
                        basicsModule.getPortalManager(),
                        new ArgumentPredicate<>(
                                false,
                                (context, portal) -> !portal.equals(context.get("portal1")),
                                "The two portals must be different."
                        )
                ));
    }
}
