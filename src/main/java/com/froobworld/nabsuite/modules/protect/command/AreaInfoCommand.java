package com.froobworld.nabsuite.modules.protect.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.froobworld.nabsuite.modules.protect.area.Area;
import com.froobworld.nabsuite.modules.protect.command.argument.AreaArgument;
import com.froobworld.nabsuite.modules.protect.user.User;
import com.froobworld.nabsuite.util.VectorDisplayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.stream.Collectors;

public class AreaInfoCommand extends NabCommand {
    private final ProtectModule protectModule;

    public AreaInfoCommand(ProtectModule protectModule) {
        super(
                "info",
                "Display information on an area.",
                "nabsuite.command.area.info",
                CommandSender.class
        );
        this.protectModule = protectModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Area area = context.get("area");
        context.getSender().sendMessage(
                Component.text("----- Information for " + area.getName() + " -----").color(NamedTextColor.YELLOW)
        );
        context.getSender().sendMessage(
                Component.text("World: ").color(NamedTextColor.YELLOW)
                        .append(Component.text(area.getWorld().getName()).color(NamedTextColor.WHITE))
        );
        context.getSender().sendMessage(
                Component.text("Bounds: ").color(NamedTextColor.YELLOW)
                        .append(Component.text("(" + VectorDisplayer.vectorToString(area.getCorner1(), true) + "), ").color(NamedTextColor.WHITE))
                        .append(Component.text("(" + VectorDisplayer.vectorToString(area.getCorner2(), true) + ")").color(NamedTextColor.WHITE))
        );
        context.getSender().sendMessage(
                Component.text("Owners: ").color(NamedTextColor.YELLOW)
                .append(
                        Component.join(
                                JoinConfiguration.separator(Component.text(", ")),
                                area.getOwners().stream()
                                        .map(User::asDecoratedComponent)
                                        .collect(Collectors.toList())
                        ).color(NamedTextColor.WHITE)
                )

        );
        context.getSender().sendMessage(
                Component.text("Managers: ").color(NamedTextColor.YELLOW)
                        .append(
                                Component.join(
                                        JoinConfiguration.separator(Component.text(", ")),
                                        area.getManagers().stream()
                                                .map(User::asDecoratedComponent)
                                                .collect(Collectors.toList())
                                ).color(NamedTextColor.WHITE)
                        )

        );
        context.getSender().sendMessage(
                Component.text("Users: ").color(NamedTextColor.YELLOW)
                        .append(
                                Component.join(
                                        JoinConfiguration.separator(Component.text(", ")),
                                        area.getUsers().stream()
                                                .map(User::asDecoratedComponent)
                                                .collect(Collectors.toList())
                                ).color(NamedTextColor.WHITE)
                        )

        );
        context.getSender().sendMessage(
                Component.text("Flags: ").color(NamedTextColor.YELLOW)
                        .append(
                                Component.join(
                                        JoinConfiguration.separator(Component.text(", ")),
                                        area.getFlags().stream()
                                                .map(Component::text)
                                                .collect(Collectors.toList())
                        ).color(NamedTextColor.WHITE)
                )
        );
        context.getSender().sendMessage(
                Component.text("Children: ").color(NamedTextColor.YELLOW)
                        .append(
                                Component.join(
                                        JoinConfiguration.separator(Component.text(", ")),
                                        area.getChildren().stream()
                                                .map(Area::getName)
                                                .map(Component::text)
                                                .collect(Collectors.toList())
                                ).color(NamedTextColor.WHITE)
                        )
        );
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(
                        new AreaArgument<>(
                                true,
                                "area",
                                protectModule.getAreaManager()
                        )
                );
    }

    @Override
    public String getUsage() {
        return "/area info <area>";
    }
}
