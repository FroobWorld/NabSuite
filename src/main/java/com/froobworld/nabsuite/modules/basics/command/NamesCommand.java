package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PlayerIdentityArgument;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.util.NumberDisplayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;

public class NamesCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public NamesCommand(BasicsModule basicsModule) {
        super("names",
                "Lookup a player's previous names.",
                "nabsuite.command.names",
                CommandSender.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        final CommandSender sender = context.getSender();
        PlayerIdentity playerIdentity = context.get("player");
        playerIdentity.getFullNameHistory().handle((nameHistory, exception) -> {
            if (exception != null) {
                sender.sendMessage(
                        Component.text("There was a problem fetching name history for " + playerIdentity.getLastName() + ".").color(NamedTextColor.RED)
                );
                exception.printStackTrace();
                return null;
            }
            List<String> previousNames = nameHistory.getEntries().stream()
                    .map(nameHistoryEntry -> nameHistoryEntry.name)
                    .collect(Collectors.toList());
            sender.sendMessage(
                    Component.text(playerIdentity.getLastName() + " has played with " + NumberDisplayer.toStringWithModifier(previousNames.size(), " name.", " names.", false)).color(NamedTextColor.YELLOW)
            );
            sender.sendMessage(Component.text(String.join(", ", previousNames)));
            return null;
        });
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(new PlayerIdentityArgument<>(true, "player", basicsModule.getPlugin().getPlayerIdentityManager(), true), ArgumentDescription.of("player"));
    }
}
