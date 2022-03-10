package com.froobworld.nabsuite.modules.protect.command;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.parsers.WorldArgument;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.froobworld.nabsuite.modules.protect.area.GlobalArea;
import com.froobworld.nabsuite.modules.protect.area.GlobalSubArea;
import com.froobworld.nabsuite.modules.protect.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.stream.Collectors;

public class GlobalAreaInfoCommand extends NabCommand {
    private final ProtectModule protectModule;

    public GlobalAreaInfoCommand(ProtectModule protectModule) {
        super(
                "info",
                "Get information on a global area.",
                "nabsuite.command.globalarea.info",
                CommandSender.class
        );
        this.protectModule = protectModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        World world = context.get("world");
        GlobalArea area = protectModule.getAreaManager().getGlobalAreaManager().getGlobalArea(world);
        context.getSender().sendMessage(
                Component.text("----- Information for " + world.getName() + " -----", NamedTextColor.YELLOW)
        );
        context.getSender().sendMessage(
                Component.text("Users: ", NamedTextColor.YELLOW)
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
                Component.text("Flags: ", NamedTextColor.YELLOW)
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
                Component.text("Sub areas: ", NamedTextColor.YELLOW)
                        .append(
                                Component.join(
                                        JoinConfiguration.separator(Component.text(", ")),
                                        area.getSubAreas().stream()
                                                .map(GlobalSubArea::getName)
                                                .map(Component::text)
                                                .collect(Collectors.toList())
                                ).color(NamedTextColor.WHITE)
                        )
        );
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder.argument(WorldArgument.newBuilder("world"));
    }

    @Override
    public String getUsage() {
        return "/globalarea info <world>";
    }

}
