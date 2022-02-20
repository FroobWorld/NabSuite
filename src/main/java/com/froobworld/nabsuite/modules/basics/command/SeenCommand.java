package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PlayerIdentityArgument;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.player.PlayerData;
import com.froobworld.nabsuite.util.DurationDisplayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class SeenCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public SeenCommand(BasicsModule basicsModule) {
        super(
                "seen",
                "Check when a player last played on the server.",
                "nabsuite.command.seen",
                CommandSender.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        PlayerIdentity playerIdentity = context.get("player");
        PlayerData playerData = basicsModule.getPlayerDataManager().getPlayerData(playerIdentity.getUuid());
        long timeSinceLastPlay = System.currentTimeMillis() - playerData.getLastPlayed();
        if (playerIdentity.asPlayer() != null) {
            context.getSender().sendMessage(
                    Component.text("That player is online right now, silly.").color(NamedTextColor.YELLOW)
            );
        } else {
            context.getSender().sendMessage(
                    Component.text(playerIdentity.getLastName() + " last played " + DurationDisplayer.asMinutesHoursDays(timeSinceLastPlay) + " ago.").color(NamedTextColor.YELLOW)
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
                                false
                        )
                );
    }
}
