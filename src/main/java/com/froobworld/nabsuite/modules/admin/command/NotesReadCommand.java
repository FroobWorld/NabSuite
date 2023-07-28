package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PageNumberArgument;
import com.froobworld.nabsuite.command.argument.arguments.PlayerIdentityArgument;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.note.PlayerNotes;
import com.froobworld.nabsuite.util.ListPaginator;
import com.froobworld.nabsuite.util.NumberDisplayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class NotesReadCommand extends NabCommand {
    private static final int ITEMS_PER_PAGE = 5;
    private final AdminModule adminModule;

    public NotesReadCommand(AdminModule adminModule) {
        super("read",
                "Read a player's notes.",
                "nabsuite.command.notes.read",
                CommandSender.class
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        PlayerIdentity player = context.get("player");
        PlayerNotes playerNotes = adminModule.getNoteManager().getNotes(player.getUuid());
        List<Component> noteComponents = playerNotes.getNotes().stream()
                .map(note -> note.asText(adminModule.getPlugin().getPlayerIdentityManager()))
                .toList();

        if (noteComponents.isEmpty()) {
            context.getSender().sendMessage(Component.text("There are no notes for this player.").color(NamedTextColor.YELLOW));
        } else {
            int pageNumber = context.get("page");
            List<Component>[] pages = ListPaginator.paginate(noteComponents, ITEMS_PER_PAGE, true);
            List<Component> page = pages[pageNumber - 1];
            context.getSender().sendMessage(
                    Component.text("Player has " + NumberDisplayer.toStringWithModifier(noteComponents.size(), " note.", " notes.", false))
                            .append(Component.text(" Showing page " + pageNumber + "/" + pages.length + "."))
                            .color(NamedTextColor.YELLOW)
            );
            for (Component note : page) {
                context.getSender().sendMessage(
                        Component.text("- ", NamedTextColor.RED)
                                .append(note)
                );
            }
        }
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(
                        new PlayerIdentityArgument<>(
                                true,
                                "player",
                                adminModule.getPlugin().getPlayerIdentityManager(),
                                true
                        )
                )
                .argument(
                        new PageNumberArgument<>(
                                false,
                                "page",
                                context -> {
                                    PlayerIdentity playerIdentity = context.get("player");
                                    return adminModule.getNoteManager().getNotes(playerIdentity.getUuid()).getNotes().size();
                                },
                                ITEMS_PER_PAGE),
                        ArgumentDescription.of("page")
                );
    }

    @Override
    public String getUsage() {
        return "/notes read <player> [page]";
    }

}
