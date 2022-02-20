package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.vanish.VanishManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class VanishCommand extends NabCommand {
    private final AdminModule adminModule;

    public VanishCommand(AdminModule adminModule) {
        super(
                "vanish",
                "Vanish from the screens of other players.",
                "nabsuite.command.vanish",
                Player.class,
                "v"
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        VanishManager vanishManager = adminModule.getVanishManager();
        vanishManager.setVanished(player, !vanishManager.isVanished(player));
        if (vanishManager.isVanished(player)) {
            List<Component> playersVisibleTo = Bukkit.getOnlinePlayers().stream()
                    .filter(otherPlayer -> otherPlayer.hasPermission(VanishManager.VANISH_SEE_PERMISSION))
                    .filter(otherPlayer -> !otherPlayer.equals(player))
                    .map(Player::displayName)
                    .collect(Collectors.toList());

            Component visibleToList = Component.join(JoinConfiguration.separator(Component.text(", ", NamedTextColor.WHITE)), playersVisibleTo);
            if (playersVisibleTo.size() > 0) {
                player.sendMessage(
                        Component.text("You are still visible to: ")
                        .append(visibleToList)
                );
            }
        }
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder;
    }
}
