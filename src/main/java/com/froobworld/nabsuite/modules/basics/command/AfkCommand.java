package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.afk.AfkManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AfkCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public AfkCommand(BasicsModule basicsModule) {
        super(
                "afk",
                "Toggle AFK status.",
                "nabsuite.command.afk",
                Player.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        AfkManager afkManager = basicsModule.getAfkManager();
        afkManager.setAfk(player, !afkManager.isAfk(player), false);
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder;
    }
}
