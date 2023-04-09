package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PageNumberArgument;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.jail.Jail;
import com.froobworld.nabsuite.util.ListPaginator;
import com.froobworld.nabsuite.util.NumberDisplayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;

public class JailsCommand extends NabCommand {
    private static final int ITEMS_PER_PAGE = 20;
    private final AdminModule adminModule;

    public JailsCommand(AdminModule adminModule) {
        super("jails",
                "Display a list of jails.",
                "nabsuite.command.jails",
                CommandSender.class
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        List<String> jails = adminModule.getJailManager().getJails().stream()
                .map(Jail::getName)
                .sorted(String::compareToIgnoreCase)
                .collect(Collectors.toList());

        if (jails.isEmpty()) {
            context.getSender().sendMessage(Component.text("There are no jails.").color(NamedTextColor.YELLOW));
        } else {
            int pageNumber = context.get("page");
            List<String>[] pages = ListPaginator.paginate(jails, ITEMS_PER_PAGE);
            if (pageNumber > pages.length) {
                context.getSender().sendMessage(
                        Component.text("Page number exceeds maximum.", NamedTextColor.RED)
                );
                return;
            }
            List<String> page = pages[pageNumber - 1];
            context.getSender().sendMessage(
                    Component.text("There " + NumberDisplayer.toStringWithModifierAndPrefix(jails.size(), " jail", " jails", "is ", "are ") + ". ")
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
                                context -> adminModule.getJailManager().getJails().size(),
                                ITEMS_PER_PAGE
                        ),
                        ArgumentDescription.of("page")
                );
    }

}
