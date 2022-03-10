package com.froobworld.nabsuite.modules.protect.command;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.parsers.WorldArgument;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.froobworld.nabsuite.modules.protect.area.GlobalArea;
import com.froobworld.nabsuite.modules.protect.command.argument.FlagArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

public class GlobalAreaRemFlagCommand extends NabCommand {
    private final ProtectModule protectModule;

    public GlobalAreaRemFlagCommand(ProtectModule protectModule) {
        super(
                "remflag",
                "Remove a flag from a global area.",
                "nabsuite.command.globalarea.remflag",
                CommandSender.class
        );
        this.protectModule = protectModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        World world = context.get("world");
        String flag = context.get("flag");
        protectModule.getAreaManager().getGlobalAreaManager().getGlobalArea(world).removeFlag(flag);
        context.getSender().sendMessage(
                Component.text("Flag removed.").color(NamedTextColor.YELLOW)
        );
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(WorldArgument.newBuilder("world"))
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
                                            GlobalArea area = protectModule.getAreaManager().getGlobalAreaManager().getGlobalArea(context.get("world"));
                                            return area.hasFlag(flag);
                                        },
                                        "The area doesn't have that flag."
                                )
                        )
                );
    }

    @Override
    public String getUsage() {
        return "/globalarea remflag <world> <flag>";
    }

}
