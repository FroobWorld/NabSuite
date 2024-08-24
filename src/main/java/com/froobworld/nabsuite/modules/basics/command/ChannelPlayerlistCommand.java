package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.channel.ChatChannel;
import com.froobworld.nabsuite.modules.basics.command.argument.ChatChannelArgument;
import com.froobworld.nabsuite.util.NumberDisplayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ChannelPlayerlistCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public ChannelPlayerlistCommand(BasicsModule basicsModule) {
        super(
                "playerlist",
                "List players are in a channel.",
                "nabsuite.command.channel.playerlist",
                CommandSender.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        ChatChannel channel = context.get("channel");
        List<Component> players = Bukkit.getOnlinePlayers().stream()
                .filter(channel::hasUserRights)
                .filter(player -> channel.isJoined(player.getUniqueId()))
                .map(Player::displayName)
                .toList();
        if (players.isEmpty()) {
            context.getSender().sendMessage(Component.text("There are no players in this channel.", NamedTextColor.YELLOW));
            return;
        }
        context.getSender().sendMessage(
                Component.text(
                        "There " + NumberDisplayer.toStringWithModifierAndPrefix(players.size(), " player ", " players ", "is ", "are ") + "in this channel.",
                        NamedTextColor.YELLOW
                )
        );
        context.getSender().sendMessage(
                Component.join(
                        JoinConfiguration.separator(Component.text(", ", NamedTextColor.WHITE)),
                        players
                )
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
        return "/channel playerlist <channel>";
    }
}
