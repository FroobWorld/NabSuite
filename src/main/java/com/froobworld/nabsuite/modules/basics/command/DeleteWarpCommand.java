package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.command.argument.WarpArgument;
import com.froobworld.nabsuite.modules.basics.teleport.warp.Warp;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class DeleteWarpCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public DeleteWarpCommand(BasicsModule basicsModule) {
        super(
                "delwarp",
                "Delete a warp.",
                "nabsuite.command.delwarp",
                CommandSender.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Warp warp = context.get("warp");
        basicsModule.getWarpManager().deleteWarp(warp);
        context.getSender().sendMessage(
                Component.text("Warp deleted.").color(NamedTextColor.YELLOW)
        );
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(new WarpArgument<>(
                        true,
                        "warp",
                        basicsModule.getWarpManager()
                ));
    }
}
