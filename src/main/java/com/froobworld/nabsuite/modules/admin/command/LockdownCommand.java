package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class LockdownCommand extends NabCommand {
    private final AdminModule adminModule;

    public LockdownCommand(AdminModule adminModule) {
        super(
                "lockdown",
                "Prevent new players from joining the server.",
                "nabsuite.command.lockdown",
                CommandSender.class
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        adminModule.getContingencyManager().toggleLockdown();
        if (adminModule.getContingencyManager().isLockdown()) {
            context.getSender().sendMessage(
                    Component.text("Server locked down until next restart. New players will no longer be able to join.", NamedTextColor.RED)
            );
        } else {
            context.getSender().sendMessage(
                    Component.text("Server lockdown lifted.", NamedTextColor.YELLOW)
            );
        }
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder;
    }
}
