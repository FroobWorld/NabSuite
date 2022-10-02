package com.froobworld.nabsuite.modules.nabmode.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.nabmode.NabModeModule;
import com.froobworld.nabsuite.modules.nabmode.nabdimension.NabModeManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NabModeCommand extends NabCommand {
    private final NabModeModule nabModeModule;

    public NabModeCommand(NabModeModule nabModeModule) {
        super(
                "nabmode",
                "Toggle nab mode for yourself.",
                "nabsuite.command.nabmode",
                Player.class
        );
        this.nabModeModule = nabModeModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        NabModeManager nabModeManager = nabModeModule.getNabModeManager();
        nabModeManager.setNabMode(player, !nabModeManager.isNabMode(player));
        if (nabModeManager.isNabMode(player)) {
            player.sendMessage(Component.text("Nab mode enabled. You are now officially a nab.", NamedTextColor.YELLOW));
        } else {
            player.sendMessage(Component.text("Nab mode disabled. You are still a nab.", NamedTextColor.YELLOW));
        }
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder;
    }
}
