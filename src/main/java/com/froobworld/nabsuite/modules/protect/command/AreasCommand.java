package com.froobworld.nabsuite.modules.protect.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PageNumberArgument;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.froobworld.nabsuite.modules.protect.area.Area;
import com.froobworld.nabsuite.util.ListPaginator;
import com.froobworld.nabsuite.util.NumberDisplayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;

public class AreasCommand extends NabCommand {
    private static final int ITEMS_PER_PAGE = 20;
    private final ProtectModule protectModule;

    public AreasCommand(ProtectModule protectModule) {
        super("areas",
                "Display a list of claimed areas.",
                "nabsuite.command.areas",
                CommandSender.class
        );
        this.protectModule = protectModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        List<String> areas = protectModule.getAreaManager().getAreas().stream()
                .map(Area::getName)
                .sorted(String::compareToIgnoreCase)
                .collect(Collectors.toList());

        if (areas.isEmpty()) {
            context.getSender().sendMessage(Component.text("There are no areas.").color(NamedTextColor.YELLOW));
        } else {
            int pageNumber = context.get("page");
            List<String>[] pages = ListPaginator.paginate(areas, ITEMS_PER_PAGE);
            List<String> page = pages[pageNumber - 1];
            context.getSender().sendMessage(
                    Component.text("There " + NumberDisplayer.toStringWithModifierAndPrefix(areas.size(), " area", " areas", "is ", "are ") + ". ")
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
                                context -> protectModule.getAreaManager().getAreas().size(),
                                ITEMS_PER_PAGE
                        ),
                        ArgumentDescription.of("page")
                );
    }

}
