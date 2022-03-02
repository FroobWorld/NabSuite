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
import org.bukkit.entity.Player;

public class TeleportPortalCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public TeleportPortalCommand(BasicsModule basicsModule) {
        super(
                "tpportal",
                "Teleport to a portal.",
                "nabsuite.command.tpportal",
                Player.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player sender = (Player) context.getSender();
        Portal portal = context.get("portal");
        basicsModule.getPlayerTeleporter().teleport(sender, portal.getLocation());
        basicsModule.getPortalManager().getPortalEnforcer().setPortalImmune(sender);
        sender.sendMessage(Component.text("Whoosh!", NamedTextColor.YELLOW));
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
