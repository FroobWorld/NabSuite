package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.channel.ChatChannel;
import com.froobworld.nabsuite.modules.basics.command.argument.ChatChannelArgument;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JoinChannelCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public JoinChannelCommand(BasicsModule basicsModule) {
        super(
                "joinchannel",
                "Join a chat channel.",
                "nabsuite.command.joinchannel",
                Player.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player sender = (Player) context.getSender();
        ChatChannel channel = context.get("channel");
        channel.join(sender.getUniqueId());
        basicsModule.getChatChannelManager().getMessageCentre().sendJoinChannelsMessage(sender, channel);
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
                                    if (context.getSender() instanceof Player) {
                                        return !channel.isJoined(((Player) context.getSender()).getUniqueId());
                                    }
                                    return false;
                                },
                                "You are already in that channel."
                        ),
                        new ArgumentPredicate<>(
                                false,
                                (context, channel) -> {
                                    if (context.getSender() instanceof Player) {
                                        return channel.hasUserRights(((Player) context.getSender()));
                                    }
                                    return false;
                                },
                                "You do not have permission to join that channel."
                        )
                ));
    }
}
