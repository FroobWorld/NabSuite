package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PlayerIdentityArgument;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class AntixrayDisableCommand extends NabCommand {
    private final AdminModule adminModule;

    public AntixrayDisableCommand(AdminModule adminModule) {
        super(
                "disable",
                "Disable anti-xray for a player.",
                "nabsuite.command.antixray.disable",
                CommandSender.class
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        PlayerIdentity player = context.get("player");
        adminModule.getXrayMonitor().isMarkedAsXrayer(player.getUuid()).thenAcceptAsync(marked -> {
            if (marked) {
                adminModule.getXrayMonitor().unmarkAsXrayer(player.getUuid()).thenAcceptAsync(result -> {
                    context.getSender().sendMessage(
                            Component.text("Anti-xray disabled for player.").color(NamedTextColor.YELLOW)
                    );
                });
            } else {
                context.getSender().sendMessage(
                        Component.text("Anti-xray is not enabled on that player.").color(NamedTextColor.RED)
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
                ));
    }

    @Override
    public String getUsage() {
        return "/antixray disable <player>";
    }
}
