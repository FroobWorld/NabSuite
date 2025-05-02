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
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class DeputyRemoveCommand extends NabCommand {

    private AdminModule adminModule;
    private DeputyManager deputyManager;

    public DeputyRemoveCommand(AdminModule adminModule) {
        super(
                "remove",
                "Remove a deputy.",
                "nabsuite.command.deputy.remove",
                CommandSender.class
        );
        this.adminModule = adminModule;
        this.deputyManager = adminModule.getDeputyManager();
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        DeputyPlayer target = context.get("player");
        deputyManager.removeDeputy(target.getDeputyLevel(), target.getUuid()).handleAsync((v, exception) -> {
            if (exception != null) {
                adminModule.getPlugin().getSLF4JLogger().error("Failed to remove deputy", exception);
                context.getSender().sendMessage(Component.text("An error occurred.").color(NamedTextColor.RED));
            } else {
                context.getSender().sendMessage(
                        Component.text("Player " + target.getPlayerIdentity().getLastName() + " is no longer a deputy.")
                                .color(NamedTextColor.YELLOW)
                );
                adminModule.getDiscordStaffLog().sendDeputyChangeNotification(context.getSender(), target, null);
            }
            return null;
        }, Bukkit.getScheduler().getMainThreadExecutor(adminModule.getPlugin()));
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
