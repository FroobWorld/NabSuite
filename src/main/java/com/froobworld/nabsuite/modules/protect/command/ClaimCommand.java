package com.froobworld.nabsuite.modules.protect.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.NumberArgument;
import com.froobworld.nabsuite.command.argument.arguments.StringArgument;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.froobworld.nabsuite.modules.protect.area.Area;
import com.froobworld.nabsuite.modules.protect.area.AreaManager;
import com.froobworld.nabsuite.modules.protect.user.PlayerUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClaimCommand extends NabCommand {
    private final ProtectModule protectModule;

    public ClaimCommand(ProtectModule protectModule) {
        super(
                "claim",
                "Claim an area in a given radius.",
                "nabsuite.command.claim",
                Player.class
        );
        this.protectModule = protectModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player sender = (Player) context.getSender();
        int radius = context.get("radius");
        String name = context.get("name");
        Location corner1 = sender.getLocation().subtract(radius, 0, radius);
        Location corner2 = sender.getLocation().add(radius, 0, radius);
        corner1.setY(-64);
        corner2.setY(320);
        boolean autoApproved = sender.hasPermission(AreaManager.EDIT_ALL_AREAS_PERMISSION) || name.contains(":");
        Area area = protectModule.getAreaManager().createArea(
                sender.getUniqueId(),
                name,
                corner1.getWorld(),
                corner1.toVector(),
                corner2.toVector(),
                new PlayerUser(protectModule, sender.getUniqueId()),
                autoApproved
        );
        if (area.isApproved()) {
            sender.sendMessage(
                    Component.text("Area claimed.").color(NamedTextColor.YELLOW)
            );
        } else {
            sender.sendMessage(
                    Component.text("Area claimed pending approval from a staff member.", NamedTextColor.YELLOW)
            );
        }
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(
                        new NumberArgument<>(
                                true,
                                "radius",
                                context -> 1,
                                context -> 50
                        )
                )
                .argument(new StringArgument<>(
                        true,
                        "name",
                        false,
                        new ArgumentPredicate<>(
                                true,
                                (context, name) -> {
                                    if (name.contains(":")) {
                                        String parentName = name.substring(0, name.lastIndexOf(":"));
                                        return protectModule.getAreaManager().getArea(parentName) != null;
                                    }
                                    return true;
                                },
                                "The parent area does not exist."
                        ),
                        new ArgumentPredicate<>(
                                false,
                                (context, name) -> {
                                    if (name.contains(":")) {
                                        String parentName = name.substring(0, name.lastIndexOf(":"));
                                        Area parentArea = protectModule.getAreaManager().getArea(parentName);
                                        if (parentArea != null) {
                                            return context.getSender().hasPermission(AreaManager.EDIT_ALL_AREAS_PERMISSION) || parentArea.isOwner((Player) context.getSender());
                                        }
                                    }
                                    return true;
                                },
                                "You don't have permission to make child areas of this area."
                        ),
                        new ArgumentPredicate<>(
                                true,
                                (context, name) -> {
                                    String[] nameSplit = name.split(":");
                                    return AreaManager.areaNamePattern.matcher(nameSplit[nameSplit.length - 1]).matches();
                                },
                                "Name must only contain letters, numbers, underscores and dashes."
                        ),
                        new ArgumentPredicate<>(
                                true,
                                (context, name) -> protectModule.getAreaManager().getArea(name) == null,
                                "An area by that name already exists."
                        )
                ));
    }
}
