package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NoStealingCommand extends NabCommand {
    private final AdminModule adminModule;

    public NoStealingCommand(AdminModule adminModule) {
        super(
                "nostealing",
                "Confirm you understand the rules on stealing.",
                "nabsuite.command.nostealing",
                Player.class
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        if (adminModule.getTheftPreventionManager().understandsRules(player)) {
            player.sendMessage(Component.text("You already have access to chests.", NamedTextColor.RED));
        } else {
            adminModule.getTheftPreventionManager().setUnderstands(player);
            player.sendMessage(Component.text("Thank you. You can now access chests.", NamedTextColor.YELLOW));
        }
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder;
    }

}
