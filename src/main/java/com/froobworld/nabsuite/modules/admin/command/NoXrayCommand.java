package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NoXrayCommand extends NabCommand {
    private final AdminModule adminModule;

    public NoXrayCommand(AdminModule adminModule) {
        super(
                "noxray",
                "Confirm you understand the rules on x-ray.",
                "nabsuite.command.noxray",
                Player.class
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        if (adminModule.getXrayMonitor().understandsRules(player)) {
            player.sendMessage(Component.text("You have already confirmed you understand the rules on x-ray.", NamedTextColor.RED));
        } else {
            adminModule.getXrayMonitor().setUnderstandsRules(player);
            player.sendMessage(Component.text("Thank you for confirming you understand that x-raying is against the rules. You can now continue mining.", NamedTextColor.YELLOW));
        }
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder;
    }

}
