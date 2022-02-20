package com.froobworld.nabsuite.modules.mechs.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.mechs.MechsModule;
import com.froobworld.nabsuite.modules.mechs.pvp.PvpManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PvpCommand extends NabCommand {
    private final MechsModule mechsModule;

    public PvpCommand(MechsModule mechsModule) {
        super(
                "pvp",
                "Toggle PvP for yourself.",
                "nabsuite.command.pvp",
                Player.class
        );
        this.mechsModule = mechsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player sender = (Player) context.getSender();
        PvpManager pvpManager = mechsModule.getPvpManager();
        pvpManager.setPvpEanbled(sender, !pvpManager.pvpEnabled(sender));
        if (pvpManager.pvpEnabled(sender)) {
            sender.sendMessage(Component.text("PvP enabled. You may now fight other players.", NamedTextColor.YELLOW));
        } else {
            sender.sendMessage(Component.text("PvP disabled. You can no longer fight other players.", NamedTextColor.YELLOW));
        }
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder;
    }
}
