package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PageNumberArgument;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.help.HelpManager;
import com.froobworld.nabsuite.util.ListPaginator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class HelpCommand extends NabCommand {
    private static final int ITEMS_PER_PAGE = 8;
    private final BasicsModule basicsModule;

    public HelpCommand(BasicsModule basicsModule) {
        super(
                "help",
                "Get a list of commands.",
                "nabsuite.command.help",
                CommandSender.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        List<HelpManager.HelpObject> helpObjects = basicsModule.getHelpManager().getHelpObjects(context.getSender());

        if (helpObjects.isEmpty()) {
            context.getSender().sendMessage(Component.text("There are no commands to display.", NamedTextColor.RED));
        } else {
            int pageNumber = context.get("page");
            List<HelpManager.HelpObject>[] pages = ListPaginator.paginate(helpObjects, ITEMS_PER_PAGE);
            if (pageNumber > pages.length) {
                context.getSender().sendMessage(
                        Component.text("Page number exceeds maximum.", NamedTextColor.RED)
                );
                return;
            }
            List<HelpManager.HelpObject> page = pages[pageNumber - 1];
            context.getSender().sendMessage(
                    Component.text("--------- ", NamedTextColor.RED)
                            .append(Component.text("Help page " + pageNumber + "/" + pages.length, NamedTextColor.WHITE))
                            .append(Component.text(" ---------", NamedTextColor.RED))
            );
            for (HelpManager.HelpObject helpObject : page) {
                context.getSender().sendMessage(helpObject.shortDescription());
            }
        }
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(new PageNumberArgument<>(
                        false,
                        "page",
                        (context) -> {
                            List<HelpManager.HelpObject> helpObjects = basicsModule.getHelpManager().getHelpObjects(context.getSender());
                            return ListPaginator.numberOfPages(helpObjects, ITEMS_PER_PAGE);
                        }
                ));
    }
}
