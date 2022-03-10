package com.froobworld.nabsuite.modules.protect.command;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.parsers.WorldArgument;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.froobworld.nabsuite.modules.protect.command.argument.GlobalSubAreaArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class GlobalAreaSubAreaDeleteCommand extends NabCommand {
    private final ProtectModule protectModule;

    public GlobalAreaSubAreaDeleteCommand(ProtectModule protectModule) {
        super(
                "delete",
                "Delete a global sub area.",
                "nabsuite.command.globalarea.subarea.delete",
                CommandSender.class
        );
        this.protectModule = protectModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        protectModule.getAreaManager().getGlobalAreaManager().removeGlobalSubArea(context.get("world"), context.get("area"));
        context.getSender().sendMessage(Component.text("Global sub area deleted.", NamedTextColor.YELLOW));
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(WorldArgument.newBuilder("world"))
                .argument(new GlobalSubAreaArgument<>(
                        true,
                        "area",
                        protectModule.getAreaManager().getGlobalAreaManager(),
                        context -> context.get("world")
                ));
    }

    @Override
    public String getUsage() {
        return "/globalarea subarea delete <world> <subarea>";
    }
}
