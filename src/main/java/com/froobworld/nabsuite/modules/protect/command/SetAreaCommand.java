package com.froobworld.nabsuite.modules.protect.command;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.flags.CommandFlag;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
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

public class SetAreaCommand extends NabCommand {
    private final ProtectModule protectModule;

    public SetAreaCommand(ProtectModule protectModule) {
        super(
                "setarea",
                "Set an area.",
                "nabsuite.command.setarea",
                Player.class
        );
        this.protectModule = protectModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player sender = (Player) context.getSender();
        Location corner1 = protectModule.getPlayerSelectionManager().getCorner1(sender);
        Location corner2 = protectModule.getPlayerSelectionManager().getCorner2(sender);
        if (corner1 == null || corner2 == null) {
            sender.sendMessage(
                    Component.text("You need to set two corners first.").color(NamedTextColor.RED)
            );
            return;
        }
        if (!corner1.getWorld().equals(corner2.getWorld())) {
            sender.sendMessage(
                    Component.text("The two corners you have selected are in different worlds.").color(NamedTextColor.RED)
            );
        }
        String name = context.get("name");
        boolean autoApproved = sender.hasPermission(AreaManager.EDIT_ALL_AREAS_PERMISSION) || name.contains(":");
        boolean useSelectedVerticalBounds = context.flags().isPresent("no-extend-vertical");
        Area area = protectModule.getAreaManager().createArea(
                sender.getUniqueId(),
                name,
                corner1.getWorld(),
                corner1.toVector().setY(useSelectedVerticalBounds ? corner1.getY() : -64),
                corner2.toVector().setY(useSelectedVerticalBounds ? corner2.getY() : 320),
                new PlayerUser(protectModule, sender.getUniqueId()),
                autoApproved
        );
        if (area.isApproved()) {
            sender.sendMessage(
                    Component.text("Area set.").color(NamedTextColor.YELLOW)
            );
        } else {
            sender.sendMessage(
                    Component.text("Area set pending approval from a staff member.", NamedTextColor.YELLOW)
            );
        }
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
                ))
                .flag(
                        CommandFlag.newBuilder("no-extend-vertical").build()
                );
    }
}
