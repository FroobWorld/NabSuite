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
import org.bukkit.entity.Player;

public class WarpCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public WarpCommand(BasicsModule basicsModule) {
        super(
                "warp",
                "Teleport to a warp.",
                "nabsuite.command.warp",
                Player.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        Warp warp = context.get("warp");
        basicsModule.getPlayerTeleporter().teleportAsync(player, warp.getLocation()).thenAccept(v -> {
            player.sendMessage(
                    Component.text("Whoosh!").color(NamedTextColor.YELLOW)
            );
        });
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
