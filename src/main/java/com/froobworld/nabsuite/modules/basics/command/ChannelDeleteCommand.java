package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.channel.ChatChannel;
import com.froobworld.nabsuite.modules.basics.channel.ChatChannelManager;
import com.froobworld.nabsuite.modules.basics.command.argument.ChatChannelArgument;
import com.froobworld.nabsuite.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChannelDeleteCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public ChannelDeleteCommand(BasicsModule basicsModule) {
        super(
                "delete",
                "Delete a chat channel.",
                "nabsuite.command.channel.delete",
                CommandSender.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        ChatChannel channel = context.get("channel");
        basicsModule.getChatChannelManager().deleteChannel(channel);
        context.getSender().sendMessage(Component.text(
                "Channel deleted.", NamedTextColor.YELLOW
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
                                        return channel.isOwner((User) context.getSender());
                                    }
                                    return true;
                                },
                                "You don't have permission to delete that channel."
                        )
                ));
    }

    @Override
    public String getUsage() {
        return "/channel delete <channel>";
    }
}
