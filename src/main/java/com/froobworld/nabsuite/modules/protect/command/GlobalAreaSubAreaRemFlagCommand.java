package com.froobworld.nabsuite.modules.protect.command;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.parsers.WorldArgument;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.froobworld.nabsuite.modules.protect.area.GlobalSubArea;
import com.froobworld.nabsuite.modules.protect.command.argument.FlagArgument;
import com.froobworld.nabsuite.modules.protect.command.argument.GlobalSubAreaArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class GlobalAreaSubAreaRemFlagCommand extends NabCommand {
    private final ProtectModule protectModule;

    public GlobalAreaSubAreaRemFlagCommand(ProtectModule protectModule) {
        super(
                "remflag",
                "Remove a flag from a global sub area.",
                "nabsuite.command.globalarea.subarea.remflag",
                CommandSender.class
        );
        this.protectModule = protectModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        GlobalSubArea area = context.get("area");
        String flag = context.get("flag");
        area.removeFlag(flag);
        context.getSender().sendMessage(
                Component.text("Flag removed.").color(NamedTextColor.YELLOW)
        );
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(WorldArgument.newBuilder("world"))
                .argument(
                        new GlobalSubAreaArgument<>(
                                true,
                                "area",
                                protectModule.getAreaManager().getGlobalAreaManager(),
                                context -> context.get("world")
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
                                            GlobalSubArea area = context.get("area");
                                            return area.hasFlag(flag);
                                        },
                                        "The area doesn't have that flag."
                                )
                        )
                );
    }

    @Override
    public String getUsage() {
        return "/globalarea subarea remflag <world> <subarea> <flag>";
    }

}
