package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.DoubleArgument;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.StringArgument;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.command.argument.predicate.predicates.PatternArgumentPredicate;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.teleport.portal.Portal;
import com.froobworld.nabsuite.modules.basics.teleport.portal.PortalManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetPortalCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public SetPortalCommand(BasicsModule basicsModule) {
        super(
                "setportal",
                "Set a portal at your location.",
                "nabsuite.command.setportal",
                Player.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        String portalName = context.get("name");
        double radius = context.get("radius");
        Portal portal = basicsModule.getPortalManager().createPortal(portalName, radius, player);
        player.sendMessage(
                Component.text("Created portal '" + portal.getName() + "' at your location.").color(NamedTextColor.YELLOW)
        );
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(
                        new StringArgument<>(
                                true,
                                "name",
                                false,
                                new PatternArgumentPredicate<>(
                                        PortalManager.portalNamePattern,
                                        "Name must only contain letters, numbers, underscores and dashes"
                                ),
                                new ArgumentPredicate<>(
                                        true,
                                        (context, string) -> (basicsModule.getPortalManager().getPortal(string) == null),
                                        "Portal already exists"
                                )
                        )
                )
                .argument(DoubleArgument.<CommandSender>newBuilder("radius")
                        .withMin(0)
                        .withMax(10)
                        .build()
                );
    }
}
