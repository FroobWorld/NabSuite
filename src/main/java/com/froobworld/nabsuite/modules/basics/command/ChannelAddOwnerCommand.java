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

public class ChannelAddOwnerCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public ChannelAddOwnerCommand(BasicsModule basicsModule) {
        super(
                "addowner",
                "Add an owner to a channel.",
                "nabsuite.command.channel.addowner",
                CommandSender.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        User user = context.get("user");
        ChatChannel channel = context.get("channel");
        channel.addOwner(user);
        context.getSender().sendMessage(Component.text(
                "User added to channel as owner.", NamedTextColor.YELLOW
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
                                (context, channel) -> context.getSender().hasPermission(ChatChannelManager.EDIT_ALL_CHANNELS_PERMISSION),
                                "You don't have permission to add owners to this channel."
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
                                    return !channel.isOwner(user);
                                },
                                "That user is already an owner of this channel."
                        )
                ));
    }

    @Override
    public String getUsage() {
        return "/channel addowner <channel> <user>";
    }
}
