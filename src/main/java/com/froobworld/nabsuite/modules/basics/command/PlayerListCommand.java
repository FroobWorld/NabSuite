package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.basics.util.PlayerList;
import com.froobworld.nabsuite.util.NumberDisplayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class PlayerListCommand extends NabCommand {

    public PlayerListCommand() {
        super(
                "playerlist",
                "Get a list of online players.",
                "nabsuite.command.playerlist",
                CommandSender.class,
                "players", "who", "online", "list"
        );
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        if (Bukkit.getOnlinePlayers().size() == 0) {
            sender.sendMessage(
                    Component.text("There are no players online.").color(NamedTextColor.YELLOW)
            );
            return;
        }
        sender.sendMessage(
                Component.text("There " + NumberDisplayer.toStringWithModifierAndPrefix(Bukkit.getOnlinePlayers().size(), " player ", " players ", "is ", "are ") + "online.")
                        .color(NamedTextColor.YELLOW)
        );
        sender.sendMessage(PlayerList.getPlayerList());
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder;
    }
}
