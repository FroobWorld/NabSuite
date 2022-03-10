package com.froobworld.nabsuite.modules.protect.command;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.bukkit.parsers.WorldArgument;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.froobworld.nabsuite.modules.protect.area.GlobalSubArea;
import com.froobworld.nabsuite.modules.protect.command.argument.GlobalSubAreaArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class GlobalAreaSubAreaRedefineCommand extends NabCommand {
    private final ProtectModule protectModule;

    public GlobalAreaSubAreaRedefineCommand(ProtectModule protectModule) {
        super(
                "redefine",
                "Redefine a global sub area's bounds.",
                "nabsuite.command.globalarea.subarea.redefine",
                CommandSender.class
        );
        this.protectModule = protectModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        GlobalSubArea area = context.get("area");
        int bound1 = context.get("bound1");
        int bound2 = context.get("bound2");
        area.setBounds(bound1, bound2);
        context.getSender().sendMessage(Component.text("Global sub area redefined.", NamedTextColor.YELLOW));
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
                .argument(IntegerArgument.newBuilder("bound1"))
                .argument(IntegerArgument.newBuilder("bound2"));
    }

    @Override
    public String getUsage() {
        return "/globalarea subarea redefine <world> <subarea> <bound1> <bound2>";
    }
}
