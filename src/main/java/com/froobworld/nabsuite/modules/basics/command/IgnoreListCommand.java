package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PageNumberArgument;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.player.PlayerData;
import com.froobworld.nabsuite.util.ListPaginator;
import com.froobworld.nabsuite.util.NumberDisplayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class IgnoreListCommand extends NabCommand {
    private static final int ITEMS_PER_PAGE = 20;
    private final BasicsModule basicsModule;

    public IgnoreListCommand(BasicsModule basicsModule) {
        super("list",
                "Display a list of players you are ignoring.",
                "nabsuite.command.ignore",
                Player.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        PlayerData playerData = basicsModule.getPlayerDataManager().getPlayerData(player);
        List<Component> ignored = playerData.getIgnored().stream()
                .map(basicsModule.getPlugin().getPlayerIdentityManager()::getPlayerIdentity)
                .sorted((identity1, identity2) -> identity1.getLastName().compareToIgnoreCase(identity2.getLastName()))
                .map(playerIdentity -> {
                    Component name;
                    if (playerIdentity.asPlayer() != null) {
                        name = playerIdentity.asPlayer().displayName();
                    } else {
                        name = Component.text(playerIdentity.getLastName());
                    }
                    return name;
                })
                .collect(Collectors.toList());

        if (ignored.isEmpty()) {
            player.sendMessage(Component.text("You are not ignoring anyone.").color(NamedTextColor.YELLOW));
        } else {
            int pageNumber = context.get("page");
            List<Component>[] pages = ListPaginator.paginate(ignored, ITEMS_PER_PAGE);
            List<Component> page = pages[pageNumber - 1];
            player.sendMessage(
                    Component.text("You are ignoring " + NumberDisplayer.toStringWithModifier(ignored.size(), " player.", " players.", false))
                            .append(Component.text(" Showing page " + pageNumber + "/" + pages.length + "."))
                            .color(NamedTextColor.YELLOW)
            );
            player.sendMessage(Component.join(JoinConfiguration.separator(Component.text(", ")), page));
        }
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(
                        new PageNumberArgument<>(
                                false,
                                "page",
                                context -> {
                                    Player player = (Player) context.getSender();
                                    return basicsModule.getPlayerDataManager().getPlayerData(player).getIgnored().size();
                                    },
                                ITEMS_PER_PAGE),
                        ArgumentDescription.of("page")
                );
    }

    @Override
    public String getUsage() {
        return "/ignore list [page]";
    }

}
