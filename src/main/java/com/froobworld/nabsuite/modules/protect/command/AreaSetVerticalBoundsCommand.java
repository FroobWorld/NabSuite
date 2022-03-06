package com.froobworld.nabsuite.modules.protect.command;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.froobworld.nabsuite.modules.protect.area.Area;
import com.froobworld.nabsuite.modules.protect.area.AreaManager;
import com.froobworld.nabsuite.modules.protect.command.argument.AreaArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AreaSetVerticalBoundsCommand extends NabCommand {
    private final ProtectModule protectModule;

    public AreaSetVerticalBoundsCommand(ProtectModule protectModule) {
        super(
                "setverticalbounds",
                "Set the vertical bounds of an area.",
                "nabsuite.command.area.setverticalbounds",
                CommandSender.class
        );
        this.protectModule = protectModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Area area = context.get("area");
        int yMin = context.get("y-min");
        int yMax = context.get("y-max");

        area.setCorners(area.getCorner1().clone().setY(yMin), area.getCorner2().clone().setY(yMax));
        context.getSender().sendMessage(Component.text("Area now extends between height " + yMin + " and " + yMax + ".", NamedTextColor.YELLOW));
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
                                        "You don't have permission to add managers to this area."
                                )
                        )
                ).argument(IntegerArgument.<CommandSender>newBuilder("y-min")
                        .withMin(-64)
                        .withMax(320)
                        .build()
                ).argument(IntegerArgument.<CommandSender>newBuilder("y-max")
                        .withMin(-64)
                        .withMax(320)
                        .build()
                );
    }

    @Override
    public String getUsage() {
        return "/area setverticalbounds <area> <y-min> <y-max>";
    }
}
