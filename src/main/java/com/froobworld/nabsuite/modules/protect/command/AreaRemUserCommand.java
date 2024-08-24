package com.froobworld.nabsuite.modules.protect.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.froobworld.nabsuite.modules.protect.area.Area;
import com.froobworld.nabsuite.modules.protect.area.AreaManager;
import com.froobworld.nabsuite.modules.protect.command.argument.AreaArgument;
import com.froobworld.nabsuite.modules.protect.command.argument.UserArgument;
import com.froobworld.nabsuite.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AreaRemUserCommand extends NabCommand {
    private final ProtectModule protectModule;

    public AreaRemUserCommand(ProtectModule protectModule) {
        super(
                "remuser",
                "Remove a user from an area.",
                "nabsuite.command.area.remuser",
                CommandSender.class
        );
        this.protectModule = protectModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Area area = context.get("area");
        User user = context.get("user");
        area.removeUser(user);
        context.getSender().sendMessage(
                Component.text("User removed from area.").color(NamedTextColor.YELLOW)
        );
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(
                        new AreaArgument<>(
                                true,
                                "area",
                                protectModule.getAreaManager(),
                                new ArgumentPredicate<>(
                                        false,
                                        (context, area) -> {
                                            if (context.getSender().hasPermission(AreaManager.EDIT_ALL_AREAS_PERMISSION)) {
                                                return true;
                                            } else if (context.getSender() instanceof Player) {
                                                return area.hasManagerRights((Player) context.getSender());
                                            }
                                            return true;
                                        },
                                        "You don't have permission to remove users to this area."
                                )
                        )
                )
                .argument(
                        new UserArgument<>(
                                true,
                                "user",
                                protectModule.getPlugin(),
                                false,
                                new ArgumentPredicate<>(
                                        false,
                                        (context, user) -> {
                                            Area area = context.get("area");
                                            return area.isUser(user);
                                        },
                                        "That user is not a user of the area."
                                )
                        )
                );
    }

    @Override
    public String getUsage() {
        return "/area remuser <area> <user>";
    }
}
