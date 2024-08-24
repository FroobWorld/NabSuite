package com.froobworld.nabsuite.modules.protect.command;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.parsers.WorldArgument;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.froobworld.nabsuite.modules.protect.area.GlobalArea;
import com.froobworld.nabsuite.modules.protect.area.GlobalSubArea;
import com.froobworld.nabsuite.modules.protect.command.argument.GlobalSubAreaArgument;
import com.froobworld.nabsuite.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.stream.Collectors;

public class GlobalAreaSubAreaInfoCommand extends NabCommand {
    private final ProtectModule protectModule;

    public GlobalAreaSubAreaInfoCommand(ProtectModule protectModule) {
        super(
                "info",
                "Get info a global sub area.",
                "nabsuite.command.globalarea.subarea.info",
                CommandSender.class
        );
        this.protectModule = protectModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        GlobalArea parent = protectModule.getAreaManager().getGlobalAreaManager().getGlobalArea(context.get("world"));
        GlobalSubArea subArea = context.get("area");
        context.getSender().sendMessage(
                Component.text("----- Information for " + subArea.getName() + " -----", NamedTextColor.YELLOW)
        );
        context.getSender().sendMessage(
                Component.text("Users (inherited from parent): ", NamedTextColor.YELLOW)
                        .append(
                                Component.join(
                                        JoinConfiguration.separator(Component.text(", ")),
                                        parent.getUsers().stream()
                                                .map(User::asDecoratedComponent)
                                                .collect(Collectors.toList())
                                ).color(NamedTextColor.WHITE)
                        )

        );
        context.getSender().sendMessage(
                Component.text("Flags: ", NamedTextColor.YELLOW)
                        .append(
                                Component.join(
                                        JoinConfiguration.separator(Component.text(", ")),
                                        subArea.getFlags().stream()
                                                .map(Component::text)
                                                .collect(Collectors.toList())
                                ).color(NamedTextColor.WHITE)
                        )
        );
        context.getSender().sendMessage(
                Component.text("Bounds: ", NamedTextColor.YELLOW)
                        .append(Component.text(Math.min(subArea.getBound1(), subArea.getBound2()) + " to " + Math.max(subArea.getBound1(), subArea.getBound2()), NamedTextColor.WHITE))
        );
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(WorldArgument.newBuilder("world"))
                .argument(new GlobalSubAreaArgument<>(
                        true,
                        "area",
                        protectModule.getAreaManager().getGlobalAreaManager(),
                        context -> context.get("world")
                ));
    }

    @Override
    public String getUsage() {
        return "/globalarea subarea info <world> <subarea>";
    }
}
