package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.StringArgument;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.channel.ChatChannelManager;
import com.froobworld.nabsuite.user.User;
import com.froobworld.nabsuite.util.ConsoleUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChannelCreateCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public ChannelCreateCommand(BasicsModule basicsModule) {
        super(
                "create",
                "Create a chat channel.",
                "nabsuite.command.channel.create",
                CommandSender.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        String name = context.get("name");
        User owner = null;
        if (context.getSender() instanceof Player) {
            owner = basicsModule.getPlugin().getUserManager().newPlayerUser(((Player) context.getSender()).getUniqueId());
        }
        basicsModule.getChatChannelManager().createChannel(ConsoleUtils.getSenderUUID(context.getSender()), name, owner);
        context.getSender().sendMessage(Component.text(
                "Channel created.", NamedTextColor.YELLOW
        ));
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(new StringArgument<>(
                        true,
                        "name",
                        false,
                        new ArgumentPredicate<>(
                                true,
                                (context, name) -> ChatChannelManager.channelNamePattern.matcher(name).matches(),
                                "Name must only contain lowercase letters and numbers and contain 3 to 10 characters."
                        ),
                        new ArgumentPredicate<>(
                                true,
                                (context, name) -> basicsModule.getChatChannelManager().getChannel(name) == null,
                                "A channel by that name already exists."
                        )
                ));
    }

    @Override
    public String getUsage() {
        return "/channel create <channel>";
    }
}
