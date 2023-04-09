package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PageNumberArgument;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.teleport.portal.Portal;
import com.froobworld.nabsuite.util.ListPaginator;
import com.froobworld.nabsuite.util.NumberDisplayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;

public class PortalsCommand extends NabCommand {
    private static final int ITEMS_PER_PAGE = 20;
    private final BasicsModule basicsModule;

    public PortalsCommand(BasicsModule basicsModule) {
        super("portals",
                "Display a list of portals.",
                "nabsuite.command.portals",
                CommandSender.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        List<String> portals = basicsModule.getPortalManager().getPortals().stream()
                .map(Portal::getName)
                .sorted(String::compareToIgnoreCase)
                .collect(Collectors.toList());

        if (portals.isEmpty()) {
            context.getSender().sendMessage(Component.text("There are no portals.").color(NamedTextColor.YELLOW));
        } else {
            int pageNumber = context.get("page");
            List<String>[] pages = ListPaginator.paginate(portals, ITEMS_PER_PAGE);
            if (pageNumber > pages.length) {
                context.getSender().sendMessage(
                        Component.text("Page number exceeds maximum.", NamedTextColor.RED)
                );
                return;
            }
            List<String> page = pages[pageNumber - 1];
            context.getSender().sendMessage(
                    Component.text("There " + NumberDisplayer.toStringWithModifierAndPrefix(portals.size(), " portal", " portals", "is ", "are ") + ". ")
                            .append(Component.text("Showing page " + pageNumber + "/" + pages.length + ".")).color(NamedTextColor.YELLOW)
            );
            context.getSender().sendMessage(Component.text(String.join(", ", page)));
        }
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(
                        new PageNumberArgument<>(
                                false,
                                "page",
                                context -> basicsModule.getPortalManager().getPortals().size(),
                                ITEMS_PER_PAGE
                        ),
                        ArgumentDescription.of("page")
                );
    }

}
