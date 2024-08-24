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

public class AreaAddOwnerCommand extends NabCommand {
    private final ProtectModule protectModule;

    public AreaAddOwnerCommand(ProtectModule protectModule) {
        super(
                "addowner",
                "Add an owner to an area.",
                "nabsuite.command.area.addowner",
                CommandSender.class
        );
        this.protectModule = protectModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Area area = context.get("area");
        User user = context.get("user");
        area.addOwner(user);
        context.getSender().sendMessage(
                Component.text("User added to area as owner.").color(NamedTextColor.YELLOW)
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
                                        "You don't have permission to add owners to this area."
                                )
                        )
                )
                .argument(
                        new UserArgument<>(
                                true,
                                "user",
                                protectModule.getPlugin(),
                                true,
                                new ArgumentPredicate<>(
                                        false,
                                        (context, user) -> {
                                            Area area = context.get("area");
                                            return !area.isOwner(user);
                                        },
                                        "That user is already an owner of the area."
                                )
                        )
                );
    }

    @Override
    public String getUsage() {
        return "/area addowner <area> <user>";
    }
}
