package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.channel.ChatChannel;
import com.froobworld.nabsuite.modules.basics.command.argument.ChatChannelArgument;
import com.froobworld.nabsuite.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.stream.Collectors;

public class ChannelInfoCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public ChannelInfoCommand(BasicsModule basicsModule) {
        super(
                "info",
                "Display information on a channel.",
                "nabsuite.command.channel.info",
                CommandSender.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        ChatChannel channel = context.get("channel");
        context.getSender().sendMessage(
                Component.text("----- Information for " + channel.getName() + " -----").color(NamedTextColor.YELLOW)
        );

        Component owners = channel.getOwners().isEmpty() ? Component.text("(none)") :
                Component.join(
                        JoinConfiguration.separator(Component.text(", ")),
                        channel.getOwners().stream()
                                .map(User::asDecoratedComponent)
                                .collect(Collectors.toList())
                ).color(NamedTextColor.WHITE);
        context.getSender().sendMessage(
                Component.text("Owners: ").color(NamedTextColor.YELLOW)
                        .append(owners.color(NamedTextColor.WHITE))
        );

        Component managers = channel.getManagers().isEmpty() ? Component.text("(none)") :
                Component.join(
                        JoinConfiguration.separator(Component.text(", ")),
                        channel.getManagers().stream()
                                .map(User::asDecoratedComponent)
                                .collect(Collectors.toList())
                ).color(NamedTextColor.WHITE);
        context.getSender().sendMessage(
                Component.text("Managers: ").color(NamedTextColor.YELLOW)
                        .append(managers.color(NamedTextColor.WHITE))
        );

        Component users = channel.getUsers().isEmpty() ? Component.text("(none)") :
                Component.join(
                        JoinConfiguration.separator(Component.text(", ")),
                        channel.getUsers().stream()
                                .map(User::asDecoratedComponent)
                                .collect(Collectors.toList())
                ).color(NamedTextColor.WHITE);
        context.getSender().sendMessage(
                Component.text("Users: ").color(NamedTextColor.YELLOW)
                        .append(users.color(NamedTextColor.WHITE))
        );
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(
                        new ChatChannelArgument<>(
                                true,
                                "channel",
                                basicsModule.getChatChannelManager()
                        )
                );
    }

    @Override
    public String getUsage() {
        return "/channel info <channel>";
    }
}
