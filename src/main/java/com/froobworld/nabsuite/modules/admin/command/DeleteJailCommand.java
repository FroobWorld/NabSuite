package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.command.argument.JailArgument;
import com.froobworld.nabsuite.modules.admin.jail.Jail;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class DeleteJailCommand extends NabCommand {
    private final AdminModule adminModule;

    public DeleteJailCommand(AdminModule adminModule) {
        super(
                "deljail",
                "Delete a jail.",
                "nabsuite.command.deljail",
                CommandSender.class
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Jail jail = context.get("jail");
        adminModule.getJailManager().deleteJail(jail);
        context.getSender().sendMessage(
                Component.text("Jail deleted.").color(NamedTextColor.YELLOW)
        );
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(new JailArgument<>(
                        true,
                        "jail",
                        adminModule.getJailManager()
                ));
    }
}
