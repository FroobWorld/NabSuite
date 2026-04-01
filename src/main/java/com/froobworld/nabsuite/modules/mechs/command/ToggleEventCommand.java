package com.froobworld.nabsuite.modules.mechs.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.mechs.MechsModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleEventCommand extends NabCommand {
    private final MechsModule mechsModule;

    public ToggleEventCommand(MechsModule mechsModule) {
        super(
                "toggleevent",
                "Toggle the current event on or off.",
                "nabsuite.command.toggleevent",
                Player.class
        );
        this.mechsModule = mechsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        if (mechsModule.getHolidayEventManager().currentEvent() == null) {
            player.sendMessage(
                    Component.text("There are no currently active events.", NamedTextColor.RED)
            );
            return;
        }
        if (mechsModule.getHolidayEventManager().toggleEvent(player)) {
            player.sendMessage(
                    Component.text("Event enabled.", NamedTextColor.YELLOW)
            );
        } else {
            player.sendMessage(
                    Component.text("Event disabled.", NamedTextColor.YELLOW)
            );
        }
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder;
    }
}
