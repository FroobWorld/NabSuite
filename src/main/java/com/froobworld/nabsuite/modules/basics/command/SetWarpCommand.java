package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.StringArgument;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.command.argument.predicate.predicates.PatternArgumentPredicate;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.teleport.warp.Warp;
import com.froobworld.nabsuite.modules.basics.teleport.warp.WarpManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetWarpCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public SetWarpCommand(BasicsModule basicsModule) {
        super(
                "setwarp",
                "Set a warp at your location.",
                "nabsuite.command.setwarp",
                Player.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        String warpName = context.get("name");
        Warp warp = basicsModule.getWarpManager().createWarp(warpName, player);
        player.sendMessage(
                Component.text("Created warp '" + warp.getName() + "' at your location.").color(NamedTextColor.YELLOW)
        );
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(
                        new StringArgument<>(
                                true,
                                "name",
                                true,
                                new PatternArgumentPredicate<>(
                                        WarpManager.warpNamePattern,
                                        "Name must only contain letters, numbers, underscores and dashes"
                                ),
                                new ArgumentPredicate<>(
                                        true,
                                        (context, string) -> (basicsModule.getWarpManager().getWarp(string) == null),
                                        "Warp already exists"
                                )
                        ),
                        ArgumentDescription.of("name")
                );
    }
}
