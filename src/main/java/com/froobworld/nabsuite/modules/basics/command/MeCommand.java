package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MeCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public MeCommand(BasicsModule basicsModule) {
        super("me",
                "Display a message as an action.",
                "nabsuite.command.me",
                Player.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player sender = (Player) context.getSender();
        String action = context.get("action");
        Component message = Component.text("* ")
                .append(sender.displayName())
                .append(Component.text(" " + action));

        if (basicsModule.getPlugin().getModule(AdminModule.class).getPunishmentManager().getMuteEnforcer().testMute(sender, true)) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!basicsModule.getPlayerDataManager().getIgnoreManager().isIgnoring(player, sender)) {
                player.sendMessage(message);
            }
        }
        Bukkit.getConsoleSender().sendMessage(message);
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(
                        StringArgument.greedy("action"),
                        ArgumentDescription.of("action")
                );
    }
}
