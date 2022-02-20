package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.greylist.PlayerGreylistData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ApplyCommand extends NabCommand {
    private final AdminModule adminModule;

    public ApplyCommand(AdminModule adminModule) {
        super(
                "apply",
                "Apply to be removed from the grey list.",
                "nabsuite.command.apply",
                Player.class
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        PlayerGreylistData greylistData = adminModule.getGreylistManager().getGreylistData(player.getUniqueId());
        if (greylistData.isGreylisted()) {
            if (greylistData.hasRequestedRemoval()) {
                player.sendMessage(
                        Component.text("You've already submitted a request for removal from the grey list.")
                                .color(NamedTextColor.YELLOW)
                );
            } else {
                adminModule.getGreylistManager().getGreylistEnforcer().applyForRemoval(player);
                player.sendMessage(
                        Component.text("Thank you, the next available staff member will action your removal request.")
                                .color(NamedTextColor.YELLOW)
                );
            }
        } else {
            player.sendMessage(
                    Component.text("You're not on the grey list, no need to apply.")
                            .color(NamedTextColor.YELLOW)
            );
        }
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder;
    }
}
