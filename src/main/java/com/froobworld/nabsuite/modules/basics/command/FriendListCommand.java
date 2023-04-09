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

public class FriendListCommand extends NabCommand {
    private static final int ITEMS_PER_PAGE = 20;
    private final BasicsModule basicsModule;

    public FriendListCommand(BasicsModule basicsModule) {
        super("list",
                "Display a list of your friends.",
                "nabsuite.command.friend",
                Player.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        PlayerData playerData = basicsModule.getPlayerDataManager().getPlayerData(player);
        List<Component> friends = playerData.getFriends().stream()
                .map(basicsModule.getPlugin().getPlayerIdentityManager()::getPlayerIdentity)
                .sorted((identity1, identity2) -> identity1.getLastName().compareToIgnoreCase(identity2.getLastName()))
                .map(playerIdentity -> {
                    Component name;
                    if (playerIdentity.asPlayer() != null) {
                        name = playerIdentity.asPlayer().displayName();
                    } else {
                        name = Component.text(playerIdentity.getLastName());
                    }
                    if (!basicsModule.getPlayerDataManager().getFriendManager().areFriends(player, playerIdentity.getUuid())) {
                        name = name.append(Component.text("*"));
                    }
                    return name;
                })
                .collect(Collectors.toList());

        if (friends.isEmpty()) {
            player.sendMessage(Component.text("You have no friends. :(").color(NamedTextColor.YELLOW));
        } else {
            int pageNumber = context.get("page");
            List<Component>[] pages = ListPaginator.paginate(friends, ITEMS_PER_PAGE);
            if (pageNumber > pages.length) {
                context.getSender().sendMessage(
                        Component.text("Page number exceeds maximum.", NamedTextColor.RED)
                );
                return;
            }
            List<Component> page = pages[pageNumber - 1];
            player.sendMessage(
                    Component.text("You have " + NumberDisplayer.toStringWithModifier(friends.size(), " friend.", " friends.", false))
                            .append(Component.text(" Showing page " + pageNumber + "/" + pages.length + "."))
                            .color(NamedTextColor.YELLOW)
            );
            player.sendMessage(Component.join(JoinConfiguration.separator(Component.text(", ")), page));
            player.sendMessage(
                    Component.text("Players marked by a * have not added you as a friend.").color(NamedTextColor.GRAY)
            );
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
                                    return basicsModule.getPlayerDataManager().getPlayerData(player).getFriends().size();
                                },
                                ITEMS_PER_PAGE),
                        ArgumentDescription.of("page")
                );
    }

    @Override
    public String getUsage() {
        return "/friend list [page]";
    }

}
