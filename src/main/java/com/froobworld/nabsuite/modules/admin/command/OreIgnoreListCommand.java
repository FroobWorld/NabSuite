package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PageNumberArgument;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.util.ListPaginator;
import com.froobworld.nabsuite.util.NumberDisplayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class OreIgnoreListCommand extends NabCommand {
    private static final int ITEMS_PER_PAGE = 20;
    private final AdminModule adminModule;

    public OreIgnoreListCommand(AdminModule adminModule) {
        super(
                "list",
                "List players with ignored ore alerts.",
                "nabsuite.command.oreignore",
                Player.class
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        List<Component> ignored = adminModule.getNotificationCentre().getIgnoredSources("ore-alert", player).stream()
                .map(adminModule.getPlugin().getPlayerIdentityManager()::getPlayerIdentity)
                .sorted((identity1, identity2) -> identity1.getLastName().compareToIgnoreCase(identity2.getLastName()))
                .map(PlayerIdentity::displayName)
                .collect(Collectors.toList());

        if (ignored.isEmpty()) {
            player.sendMessage(Component.text("You are not ignoring ore alerts from anyone.").color(NamedTextColor.YELLOW));
        } else {
            int pageNumber = context.get("page");
            List<Component>[] pages = ListPaginator.paginate(ignored, ITEMS_PER_PAGE);
            List<Component> page = pages[pageNumber - 1];
            player.sendMessage(
                    Component.text("You are ignoring ore alerts from " + NumberDisplayer.toStringWithModifier(ignored.size(), " player.", " players.", false))
                            .append(Component.text(" Page " + pageNumber + "/" + pages.length + "."))
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
                                    return adminModule.getNotificationCentre().getIgnoredSources("ore-alert", player).size();
                                },
                                ITEMS_PER_PAGE),
                        ArgumentDescription.of("page")
                );
    }

    @Override
    public String getUsage() {
        return "/oreignore list [page]";
    }

}
