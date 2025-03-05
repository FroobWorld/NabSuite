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
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import com.froobworld.nabsuite.modules.discord.bot.command.DiscordCommandSender;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class FirstJoinCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public FirstJoinCommand(BasicsModule basicsModule) {
        super(
                "firstjoin",
                "Find when a player first joined the server.",
                "nabsuite.command.firstjoin",
                CommandSender.class,
                "fj"
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        PlayerIdentity playerIdentity = context.get("player");
        PlayerData playerData = basicsModule.getPlayerDataManager().getPlayerData(playerIdentity.getUuid());
        if (context.getSender() instanceof DiscordCommandSender discordSender) {
            // Send as discord timestamp
            discordSender.updateResponse(msg -> msg.editOriginal(
                playerIdentity.getLastName() + " joined <t:"+(playerData.getFirstJoined()/1000)+":R>."
            ));
            return;
        }
        long timeSinceFirstJoin = System.currentTimeMillis() - playerData.getFirstJoined();

        String joinDate = new SimpleDateFormat("dd MMMM yyyy").format(Date.from(Instant.ofEpochMilli(playerData.getFirstJoined())));

        context.getSender().sendMessage(
                Component.text(playerIdentity.getLastName() + " joined " + DurationDisplayer.asDurationString(timeSinceFirstJoin) + " ago.").color(NamedTextColor.YELLOW)
                        .hoverEvent(HoverEvent.showText(Component.text(joinDate)))
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
                                true
                        )
                );
    }
}
