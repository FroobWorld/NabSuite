package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.DurationArgument;
import com.froobworld.nabsuite.command.argument.arguments.PlayerIdentityArgument;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class AntixrayEnableCommand extends NabCommand {
    private final AdminModule adminModule;

    public AntixrayEnableCommand(AdminModule adminModule) {
        super(
                "enable",
                "Enable anti-xray for a player.",
                "nabsuite.command.antixray.enable",
                CommandSender.class
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        PlayerIdentity player = context.get("player");
        long duration = context.get("duration");
        adminModule.getXrayMonitor().isMarkedAsXrayer(player.getUuid()).thenAcceptAsync(marked -> {
            if (!marked) {
                adminModule.getXrayMonitor().markAsXrayer(player.getUuid(), duration).thenAcceptAsync(result -> {
                    context.getSender().sendMessage(
                            Component.text("Anti-xray enabled for player.").color(NamedTextColor.YELLOW)
                    );
                });
            } else {
                context.getSender().sendMessage(
                        Component.text("Anti-xray is already enabled for that player.").color(NamedTextColor.RED)
                );
            }
        });
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(new PlayerIdentityArgument<>(
                        true,
                        "player",
                        adminModule.getPlugin().getPlayerIdentityManager(),
                        true
                ))
                .argument(new DurationArgument<>(
                        true,
                        "duration"
                ));
    }

    @Override
    public String getUsage() {
        return "/antixray enable <player> <duration>";
    }
}
