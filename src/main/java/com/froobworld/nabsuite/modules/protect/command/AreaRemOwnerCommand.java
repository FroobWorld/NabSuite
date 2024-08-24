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

public class AreaRemOwnerCommand extends NabCommand {
    private final ProtectModule protectModule;

    public AreaRemOwnerCommand(ProtectModule protectModule) {
        super(
                "remowner",
                "Remove an owner from an area.",
                "nabsuite.command.area.remowner",
                CommandSender.class
        );
        this.protectModule = protectModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Area area = context.get("area");
        User user = context.get("user");
        area.removeOwner(user);
        context.getSender().sendMessage(
                Component.text("User removed from area as owner.").color(NamedTextColor.YELLOW)
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
                                        (context, area) -> context.getSender().hasPermission(AreaManager.EDIT_ALL_AREAS_PERMISSION),
                                        "You don't have permission to remove owners from this area."
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
                                            return area.isOwner(user);
                                        },
                                        "That user is not an owner of the area."
                                )
                        )
                );
    }

    @Override
    public String getUsage() {
        return "/area remowner <area> <user>";
    }
}
