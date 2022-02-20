package com.froobworld.nabsuite.modules.protect.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.froobworld.nabsuite.modules.protect.area.AreaManager;
import com.froobworld.nabsuite.modules.protect.command.argument.AreaArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DelAreaCommand extends NabCommand {
    private final ProtectModule protectModule;

    public DelAreaCommand(ProtectModule protectModule) {
        super(
                "delarea",
                "Delete an area.",
                "nabsuite.command.delarea",
                CommandSender.class
        );
        this.protectModule = protectModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        protectModule.getAreaManager().deleteArea(context.get("area"));
        context.getSender().sendMessage(
                Component.text("Area deleted.").color(NamedTextColor.YELLOW)
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
                                                return area.isOwner((Player) context.getSender());
                                            }
                                            return true;
                                        },
                                        "You don't have permission to delete this area."
                                )
                        )
                );
    }
}
