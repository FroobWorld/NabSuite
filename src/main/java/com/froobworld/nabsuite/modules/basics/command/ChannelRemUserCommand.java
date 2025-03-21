package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.channel.ChatChannel;
import com.froobworld.nabsuite.modules.basics.channel.ChatChannelManager;
import com.froobworld.nabsuite.modules.basics.command.argument.ChatChannelArgument;
import com.froobworld.nabsuite.modules.protect.command.argument.UserArgument;
import com.froobworld.nabsuite.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChannelRemUserCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public ChannelRemUserCommand(BasicsModule basicsModule) {
        super(
                "remuser",
                "Remove a user from a channel.",
                "nabsuite.command.channel.remuser",
                CommandSender.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        User user = context.get("user");
        ChatChannel channel = context.get("channel");
        channel.removeUser(user);
        context.getSender().sendMessage(Component.text(
                "Removed user from channel.", NamedTextColor.YELLOW
        ));
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(new ChatChannelArgument<>(
                        true,
                        "channel",
                        basicsModule.getChatChannelManager(),
                        new ArgumentPredicate<>(
                                false,
                                (context, channel) -> {
                                    if (context.getSender().hasPermission(ChatChannelManager.EDIT_ALL_CHANNELS_PERMISSION)) {
                                        return true;
                                    } else if (context.getSender() instanceof Player) {
                                        return channel.hasManagerRights((Player) context.getSender());
                                    }
                                    return true;
                                },
                                "You don't have permission to remove users from this channel."
                        )
                ))
                .argument(new UserArgument<>(
                        true,
                        "user",
                        basicsModule.getPlugin(),
                        true,
                        new ArgumentPredicate<>(
                                false,
                                (context, user) -> {
                                    ChatChannel channel = context.get("channel");
                                    return channel.isUser(user);
                                },
                                "That user is not a user of this channel."
                        )
                ));
    }

    @Override
    public String getUsage() {
        return "/channel remuser <channel> <user>";
    }
}
