package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PlayerIdentityArgument;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OreIgnoreAddCommand extends NabCommand {

    private final AdminModule adminModule;

    public OreIgnoreAddCommand(AdminModule adminModule) {
        super(
                "add",
                "Ignore ore alerts for player.",
                "nabsuite.command.oreignore",
                Player.class
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player sender = (Player) context.getSender();
        PlayerIdentity target = context.get("player");
        adminModule.getNotificationCentre().setIgnoreSource("ore-alert", sender, target.getUuid(), true);
        sender.sendMessage(Component.text("Now ignoring ore alerts for " + target.getLastName()).color(NamedTextColor.YELLOW));
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(new PlayerIdentityArgument<>(
                        true,
                        "player",
                        adminModule.getPlugin().getPlayerIdentityManager(),
                        true,
                        new ArgumentPredicate<>(false, (context, playerIdentity) -> {
                            Player sender = (Player) context.getSender();
                            return !adminModule.getNotificationCentre().getIgnoredSources("ore-alert", sender).contains(playerIdentity.getUuid());
                        }, "Player already ignored")
                ));
    }

    @Override
    public String getUsage() {
        return "/oreignore add <player>";
    }
}
