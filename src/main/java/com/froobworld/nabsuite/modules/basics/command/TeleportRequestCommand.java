package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PlayerArgument;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeleportRequestCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public TeleportRequestCommand(BasicsModule basicsModule) {
        super(
                "tpa",
                "Request to teleport to another player.",
                "nabsuite.command.tpa",
                Player.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player sender = (Player) context.getSender();
        Player subject =  context.get("player");
        basicsModule.getTeleportRequestHandler().requestTeleportTo(sender, subject);
        sender.sendMessage(
                Component.text("Teleport request sent.").color(NamedTextColor.YELLOW)
        );

        subject.sendMessage(
                sender.displayName().append(
                        Component.text(" has requested to teleport to you.")
                ).color(NamedTextColor.YELLOW)
        );
        subject.sendMessage(
                Component.text("Use ").append(
                        Component.text("/tpaccept").color(NamedTextColor.GREEN).clickEvent(ClickEvent.runCommand("/tpaccept"))
                ).append(
                        Component.text(" to accept.")
                ).color(NamedTextColor.YELLOW)
        );
        subject.sendMessage(
                Component.text("Use ").append(
                        Component.text("/tpdeny").color(NamedTextColor.RED).clickEvent(ClickEvent.runCommand("/tpdeny"))
                ).append(
                        Component.text(" to decline.")
                ).color(NamedTextColor.YELLOW)
        );
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(new PlayerArgument<>(
                        true,
                        "player",
                        new ArgumentPredicate<>(
                                false,
                                (context, player) -> basicsModule.getPlayerDataManager().getPlayerData(player).teleportRequestsEnabled(),
                                "Player has teleport requests disabled"
                        )
                ));
    }
}
