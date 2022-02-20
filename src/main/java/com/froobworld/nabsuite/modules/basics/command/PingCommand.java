package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PingCommand extends NabCommand {

    public PingCommand() {
        super("ping",
                "Pong!",
                "nabsuite.command.ping",
                Player.class);
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player sender = (Player) context.getSender();
        String response = "Pong!";
        TextComponent message = Component.text(response)
                .append(Component.text(" (" + sender.spigot().getPing() + "ms)").color(NamedTextColor.YELLOW));
        sender.sendMessage(message);
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder;
    }

}
