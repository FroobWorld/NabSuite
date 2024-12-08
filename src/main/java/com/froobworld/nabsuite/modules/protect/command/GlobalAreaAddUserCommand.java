package com.froobworld.nabsuite.modules.protect.command;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.parsers.WorldArgument;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.froobworld.nabsuite.modules.protect.command.argument.UserArgument;
import com.froobworld.nabsuite.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

public class GlobalAreaAddUserCommand extends NabCommand {
    private final ProtectModule protectModule;

    public GlobalAreaAddUserCommand(ProtectModule protectModule) {
        super(
                "adduser",
                "Add a user to a global area.",
                "nabsuite.command.globalarea.adduser",
                CommandSender.class
        );
        this.protectModule = protectModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        World world = context.get("world");
        User user = context.get("user");
        protectModule.getAreaManager().getGlobalAreaManager().getGlobalArea(world).addUser(user);
        context.getSender().sendMessage(
                Component.text("User added to global area.").color(NamedTextColor.YELLOW)
        );
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(WorldArgument.newBuilder("world"))
                .argument(
                        new UserArgument<>(
                                true,
                                "user",
                                protectModule.getPlugin(),
                                true,
                                new ArgumentPredicate<>(
                                        false,
                                        (context, user) -> {
                                            World world = context.get("world");
                                            return !protectModule.getAreaManager().getGlobalAreaManager().getGlobalArea(world).isUser(user);
                                        },
                                        "That user is already a user of the global area."
                                )
                        )
                );
    }

    @Override
    public String getUsage() {
        return "/globalarea adduser <world> <user>";
    }
}
