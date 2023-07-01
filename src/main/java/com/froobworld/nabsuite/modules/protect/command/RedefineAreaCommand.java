package com.froobworld.nabsuite.modules.protect.command;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.flags.CommandFlag;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.froobworld.nabsuite.modules.protect.area.Area;
import com.froobworld.nabsuite.modules.protect.area.AreaManager;
import com.froobworld.nabsuite.modules.protect.command.argument.AreaArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RedefineAreaCommand extends NabCommand {
    private final ProtectModule protectModule;

    public RedefineAreaCommand(ProtectModule protectModule) {
        super(
                "redefinearea",
                "Redefine an existing area.",
                "nabsuite.command.redefinearea",
                Player.class
        );
        this.protectModule = protectModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player sender = (Player) context.getSender();
        Area area = context.get("area");
        Location corner1 = protectModule.getPlayerSelectionManager().getCorner1(sender);
        Location corner2 = protectModule.getPlayerSelectionManager().getCorner2(sender);
        if (corner1 == null || corner2 == null) {
            sender.sendMessage(Component.text("You need to set two corners first.", NamedTextColor.RED));
            return;
        }
        if (!corner1.getWorld().equals(corner2.getWorld())) {
            sender.sendMessage(Component.text("The two corners you have selected are in different worlds.", NamedTextColor.RED));
        }
        if (!corner1.getWorld().equals(area.getWorld())) {
            sender.sendMessage(Component.text("The corners you selected are not in the same world as the area.", NamedTextColor.RED));
        }
        boolean useSelectedVerticalBounds = context.flags().isPresent("no-extend-vertical");
        area.setCorners(
                corner1.toVector().setY(useSelectedVerticalBounds ? corner1.getY() : -64),
                corner2.toVector().setY(useSelectedVerticalBounds ? corner2.getY() : 320)
        );
        sender.sendMessage(Component.text("Area redefined.", NamedTextColor.YELLOW));
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
                                        "You don't have permission to redefine this area."
                                )
                        )
                )
                .flag(
                        CommandFlag.newBuilder("no-extend-vertical").build()
                );
    }
}
