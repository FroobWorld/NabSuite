package com.froobworld.nabsuite.modules.protect.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.froobworld.nabsuite.modules.protect.area.Area;
import com.froobworld.nabsuite.modules.protect.area.AreaManager;
import com.froobworld.nabsuite.modules.protect.area.flag.Flags;
import com.froobworld.nabsuite.modules.protect.command.argument.AreaArgument;
import com.froobworld.nabsuite.modules.protect.command.argument.FlagArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AreaAddFlagCommand extends NabCommand {
    private final ProtectModule protectModule;

    public AreaAddFlagCommand(ProtectModule protectModule) {
        super(
                "addflag",
                "Add a flag to an area.",
                "nabsuite.command.area.addflag",
                CommandSender.class
        );
        this.protectModule = protectModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Area area = context.get("area");
        String flag = context.get("flag");
        area.addFlag(flag);
        context.getSender().sendMessage(
                Component.text("Flag added.").color(NamedTextColor.YELLOW)
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
                                        "You don't have permission to add flags to this area."
                                )
                        )
                )
                .argument(
                        new FlagArgument<>(
                                true,
                                "flag",
                                new ArgumentPredicate<>(
                                        false,
                                        (context, flag) -> context.getSender().hasPermission("nabsuite.flag." + flag),
                                        "You don't have permission to use that flag."
                                ),
                                new ArgumentPredicate<>(
                                        false,
                                        (context, flag) -> {
                                            Area area = context.get("area");
                                            return !area.hasFlag(flag);
                                        },
                                        "The area already has that flag."
                                ),
                                new ArgumentPredicate<>(
                                        false,
                                        (context, flag) -> {
                                            Area area = context.get("area");
                                            return !flag.equals(Flags.INHERIT_USERS) || area.getParent() != null;
                                        },
                                        "The flag is only available for subareas."
                                )
                        )
                );
    }

    @Override
    public String getUsage() {
        return "/area addflag <area> <flag>";
    }

}
