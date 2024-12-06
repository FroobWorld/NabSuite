package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class RandomTeleportStatusCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public RandomTeleportStatusCommand(BasicsModule basicsModule) {
        super(
                "status",
                "Show random teleport status.",
                "nabsuite.command.rtp.status",
                CommandSender.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        List<TextComponent> status = basicsModule.getRandomTeleportManager().getRandomTeleportStatus().stream().map(
                s -> Component.text(s.getWorld().getName())
                        .append(Component.text(" ("))
                        .append(s.getPregenerateMax() == 0 ?
                                Component.text("disabled").color(NamedTextColor.GRAY) :
                                Component.text(s.getPregenerated() + "/" + s.getPregenerateMax())
                                        .color(
                                                s.getPregenerated() >= s.getPregenerateMax() ? NamedTextColor.GREEN :
                                                s.getPregenerated() == 0 ? NamedTextColor.RED : NamedTextColor.YELLOW)
                        )
                        .append(Component.text(")"))
                        .color(NamedTextColor.WHITE)
        ).toList();
        context.getSender().sendMessage(
                Component.text("RTP Pre-generation status: ", NamedTextColor.YELLOW)
                    .append(
                            status.isEmpty() ?
                                    Component.text("No active worlds").color(NamedTextColor.GRAY) :
                                    Component.join(JoinConfiguration.separator(Component.text(", ")), status)
                    )
        );
    }
    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder;
    }

    @Override
    public String getUsage() {
        return "/rtp status";
    }
}
