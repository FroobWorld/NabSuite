package com.froobworld.nabsuite.modules.mechs.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.mechs.MechsModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BorderWarningCommand extends NabCommand {
    private final MechsModule mechsModule;

    public BorderWarningCommand(MechsModule mechsModule) {
        super(
                "borderwarning",
                "Confirm you understand the border warning.",
                "nabsuite.command.borderwarning",
                Player.class
        );
        this.mechsModule = mechsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        if (mechsModule.getWorldBorderManager().acceptedBorderRegionWarning(player)) {
            player.sendMessage(
                    Component.text("You have already accepted the border region warning.", NamedTextColor.RED)
            );
        } else {
            mechsModule.getWorldBorderManager().acceptBorderRegionWarning(player);
            player.sendMessage(
                    Component.text("Thank you for accepting the border region warning.", NamedTextColor.YELLOW)
            );
        }
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder;
    }
}
