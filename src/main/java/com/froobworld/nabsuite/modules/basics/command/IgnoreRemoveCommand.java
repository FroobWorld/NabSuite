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

public class IgnoreRemoveCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public IgnoreRemoveCommand(BasicsModule basicsModule) {
        super("remove",
                "Remove a player from your ignore list.",
                "nabsuite.command.ignore",
                Player.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        PlayerData playerData = basicsModule.getPlayerDataManager().getPlayerData(player);
        PlayerIdentity toUnignore = context.get("player");

        playerData.unignore(toUnignore.getUuid());
        player.sendMessage(
                Component.text("Removed " + toUnignore.getLastName() + " from your ignore list.").color(NamedTextColor.YELLOW)
        );
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(
                        new PlayerIdentityArgument<>(
                                true,
                                "player",
                                basicsModule.getPlugin().getPlayerIdentityManager(),
                                false,
                                new ArgumentPredicate<>(false, (context, playerIdentity) -> {
                                    Player sender = (Player) context.getSender();
                                    return basicsModule.getPlayerDataManager().getPlayerData(sender).isIgnoring(playerIdentity.getUuid());
                                }, "Player not ignored")
                        ),
                        ArgumentDescription.of("player")
                );
    }

    @Override
    public String getUsage() {
        return "/ignore remove <player>";
    }

}
