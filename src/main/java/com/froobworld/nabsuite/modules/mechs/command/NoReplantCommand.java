package com.froobworld.nabsuite.modules.mechs.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.mechs.MechsModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NoReplantCommand extends NabCommand {
    private final MechsModule mechsModule;

    public NoReplantCommand(MechsModule mechsModule) {
        super(
                "noreplant",
                "Toggle automatic tree replanting.",
                "nabsuite.command.noreplant",
                Player.class
        );
        this.mechsModule = mechsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player sender = (Player) context.getSender();
        boolean current = mechsModule.getTreeManager().replantEnabled(sender);

        mechsModule.getTreeManager().setReplantEnabled(sender, !current);
        if (current) {
            sender.sendMessage(Component.text("Automatic tree replanting disabled temporarily.", NamedTextColor.YELLOW));
        } else {
            sender.sendMessage(Component.text("Automatic tree replanting enabled.", NamedTextColor.YELLOW));
        }
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder;
    }
}
