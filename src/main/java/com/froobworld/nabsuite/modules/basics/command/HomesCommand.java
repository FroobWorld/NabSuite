package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PageNumberArgument;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.teleport.home.Home;
import com.froobworld.nabsuite.util.ListPaginator;
import com.froobworld.nabsuite.util.NumberDisplayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class HomesCommand extends NabCommand {
    private static final int ITEMS_PER_PAGE = 20;
    private final BasicsModule basicsModule;

    public HomesCommand(BasicsModule basicsModule) {
        super("homes",
                "Display a list of your homes.",
                "nabsuite.command.homes",
                Player.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();

        List<String> homes = basicsModule.getHomeManager().getHomes(player).getHomes().stream()
                .map(Home::getName)
                .sorted(String::compareToIgnoreCase)
                .collect(Collectors.toList());

        if (homes.isEmpty()) {
            context.getSender().sendMessage(Component.text("You have no homes.").color(NamedTextColor.YELLOW));
        } else {
            int pageNumber = context.get("page");
            List<String>[] pages = ListPaginator.paginate(homes, ITEMS_PER_PAGE);
            if (pageNumber > pages.length) {
                player.sendMessage(
                        Component.text("Page number exceeds maximum.", NamedTextColor.RED)
                );
                return;
            }
            List<String> page = pages[pageNumber - 1];
            context.getSender().sendMessage(
                    Component.text("You have " + NumberDisplayer.toStringWithModifier(homes.size(), " home", " homes", false) + ". ")
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
                                context -> basicsModule.getHomeManager().getHomes((Player) context.getSender()).getHomes().size(),
                                ITEMS_PER_PAGE
                        ),
                        ArgumentDescription.of("page")
                );
    }

}
