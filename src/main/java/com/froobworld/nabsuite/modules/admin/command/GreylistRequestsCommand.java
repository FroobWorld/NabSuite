package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PageNumberArgument;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.greylist.PlayerGreylistData;
import com.froobworld.nabsuite.util.ListPaginator;
import com.froobworld.nabsuite.util.NumberDisplayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GreylistRequestsCommand extends NabCommand {
    private static final int ITEMS_PER_PAGE = 20;
    private final AdminModule adminModule;

    public GreylistRequestsCommand(AdminModule adminModule) {
        super("greylist",
                "Get a list of players requesting removal from the grey list.",
                "nabsuite.command.greylist.requests",
                CommandSender.class,
                "graylist", "gl"
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        List<Component> requesters = getRequesters().stream()
                .map(adminModule.getPlugin().getPlayerIdentityManager()::getPlayerIdentity)
                .map(PlayerIdentity::displayName)
                .collect(Collectors.toList());

        if (requesters.isEmpty()) {
            context.getSender().sendMessage(Component.text("There are no requests.").color(NamedTextColor.YELLOW));
        } else {
            int pageNumber = context.get("page");
            List<Component>[] pages = ListPaginator.paginate(requesters, ITEMS_PER_PAGE);
            List<Component> page = pages[pageNumber - 1];
            context.getSender().sendMessage(
                    Component.text("There " + NumberDisplayer.toStringWithModifierAndPrefix(requesters.size(), " request", " requests", "is ", "are ") + ". ")
                            .append(Component.text("Showing page " + pageNumber + "/" + pages.length + ".")).color(NamedTextColor.YELLOW)
            );
            context.getSender().sendMessage(Component.join(JoinConfiguration.separator(Component.text(", ")), page));
        }
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .literal("requests")
                .argument(
                        new PageNumberArgument<>(
                                false,
                                "page",
                                context -> getRequesters().size(),
                                ITEMS_PER_PAGE
                        ),
                        ArgumentDescription.of("page")
                );
    }

    private List<UUID> getRequesters() {
        return adminModule.getGreylistManager().getGreylistData().stream()
                .filter(PlayerGreylistData::hasRequestedRemoval)
                .filter(PlayerGreylistData::isGreylisted)
                .map(PlayerGreylistData::getUuid)
                .collect(Collectors.toList());
    }

}
