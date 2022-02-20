package com.froobworld.nabsuite.modules.protect.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.froobworld.nabsuite.util.VectorDisplayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Corner1Command extends NabCommand {
    private final ProtectModule protectModule;

    public Corner1Command(ProtectModule protectModule) {
        super(
                "corner1",
                "Set corner one as your current location.",
                "nabsuite.command.corner",
                Player.class,
                "position1", "pos1", "p1"
        );
        this.protectModule = protectModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player sender = (Player) context.getSender();
        protectModule.getPlayerSelectionManager().setCorner1(sender, sender.getLocation());
        sender.sendMessage(
                Component.text("Corner one set as (" + VectorDisplayer.vectorToString(sender.getLocation().toVector(), true) + ").").color(NamedTextColor.YELLOW)
        );
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder;
    }
}
