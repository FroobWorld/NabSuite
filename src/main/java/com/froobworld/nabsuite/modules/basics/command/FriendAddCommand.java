package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PlayerIdentityArgument;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.player.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FriendAddCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public FriendAddCommand(BasicsModule basicsModule) {
        super("add",
                "Add a player as a friend.",
                "nabsuite.command.friend",
                Player.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        PlayerData playerData = basicsModule.getPlayerDataManager().getPlayerData(player);
        PlayerIdentity friend = context.get("player");
        if (player.getUniqueId().equals(friend.getUuid())) {
            player.sendMessage(
                    Component.text("That's just sad.").color(NamedTextColor.RED)
            );
            return;
        }

        playerData.addFriend(friend.getUuid());
        player.sendMessage(
                Component.text("Added " + friend.getLastName() + " as a friend.").color(NamedTextColor.YELLOW)
        );
        if (friend.asPlayer() != null) {
            friend.asPlayer().sendMessage(
                    player.displayName()
                            .append(Component.text(" has added you as a friend.")).color(NamedTextColor.YELLOW)
            );
        }
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(
                        new PlayerIdentityArgument<>(
                                true,
                                "player",
                                basicsModule.getPlugin().getPlayerIdentityManager(),
                                true,
                                new ArgumentPredicate<>(false, (context, playerIdentity) -> {
                                    Player sender = (Player) context.getSender();
                                    return !basicsModule.getPlayerDataManager().getPlayerData(sender).isFriend(playerIdentity.getUuid());
                                }, "Player already a friend")
                        ),
                        ArgumentDescription.of("player")
                );
    }

    @Override
    public String getUsage() {
        return "/friend add <player>";
    }
}
