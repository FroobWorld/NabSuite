package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.command.argument.DeputyPlayerArgument;
import com.froobworld.nabsuite.modules.admin.deputy.DeputyManager;
import com.froobworld.nabsuite.modules.admin.deputy.DeputyPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class DeputyRemoveCommand extends NabCommand {

    private DeputyManager deputyManager;

    public DeputyRemoveCommand(AdminModule adminModule) {
        super(
                "remove",
                "Remove a deputy.",
                "nabsuite.command.deputy.remove",
                CommandSender.class
        );
        this.deputyManager = adminModule.getDeputyManager();
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        DeputyPlayer target = context.get("player");
        deputyManager.removeDeputy(context.getSender(), target.getDeputyLevel(), target.getUuid());
        context.getSender().sendMessage(
                Component.text("Player " + target.getPlayerIdentity().getLastName() + " is no longer a deputy.")
                        .color(NamedTextColor.YELLOW)
        );
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(new DeputyPlayerArgument<>(
                        true,
                        "player",
                        deputyManager,
                        new ArgumentPredicate<>(
                                false,
                                (context, deputy) -> deputy.checkManagePermission(context.getSender()),
                                "You lack permission to remove that deputy."
                        )
                ));
    }

    @Override
    public String getUsage() {
        return "/deputy remove <player>";
    }
}
