package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.player.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeleportToggleFriendsCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public TeleportToggleFriendsCommand(BasicsModule basicsModule) {
        super(
                "tptoggle",
                "Toggle whether players can teleport to you.",
                "nabsuite.command.tptoggle",
                Player.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player sender = (Player) context.getSender();
        PlayerData playerData = basicsModule.getPlayerDataManager().getPlayerData(sender);
        playerData.setTeleportFriends(!playerData.teleportFriendsEnabled());
        if (playerData.teleportFriendsEnabled()) {
            sender.sendMessage(
                    Component.text("Your friends will now be able to teleport to you.").color(NamedTextColor.YELLOW)
            );
        } else {
            sender.sendMessage(
                    Component.text("Your friends will no longer be able to teleport to you.").color(NamedTextColor.YELLOW)
            );
        }
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .literal("friends");
    }
}
