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

public class AntixrayCheckCommand extends NabCommand {
    private final AdminModule adminModule;

    public AntixrayCheckCommand(AdminModule adminModule) {
        super(
                "check",
                "Check if anti-xray is enabled on a player.",
                "nabsuite.command.antixray.check",
                CommandSender.class
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        PlayerIdentity player = context.get("player");
        adminModule.getXrayMonitor().isMarkedAsXrayer(player.getUuid()).thenAcceptAsync(marked -> {
            if (marked) {
                context.getSender().sendMessage(
                        Component.text("Anti-xray is enabled on that player.").color(NamedTextColor.YELLOW)
                );
            } else {
                context.getSender().sendMessage(
                        Component.text("Anti-xray is disabled on that player.").color(NamedTextColor.YELLOW)
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
        return "/antixray check <player>";
    }
}
