package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PageNumberArgument;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.deputy.DeputyManager;
import com.froobworld.nabsuite.modules.admin.deputy.DeputyPlayer;
import com.froobworld.nabsuite.util.DurationDisplayer;
import com.froobworld.nabsuite.util.ListPaginator;
import com.froobworld.nabsuite.util.NumberDisplayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class DeputyListCommand extends NabCommand {

    private DeputyManager deputyManager;
    private final static int ITEMS_PER_PAGE = 5;

    public DeputyListCommand(AdminModule adminModule) {
        super(
                "list",
                "List deputies.",
                "nabsuite.command.deputy.list",
                CommandSender.class
        );
        this.deputyManager = adminModule.getDeputyManager();
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        List<DeputyPlayer> players = deputyManager.getDeputies().stream()
                .filter(deputyPlayer -> System.currentTimeMillis() < deputyPlayer.getExpiry())
                .filter(deputy -> context.getSender().hasPermission(DeputyManager.LIST_DEPUTY_PREFIX + deputy.getDeputyLevel().getName()))
                .sorted()
                .toList();

        if (players.isEmpty()) {
            context.getSender().sendMessage(Component.text("There are currently no deputies.").color(NamedTextColor.YELLOW));
        } else {
            int pageNumber = context.get("page");
            List<DeputyPlayer>[] pages = ListPaginator.paginate(players, ITEMS_PER_PAGE);
            List<DeputyPlayer> page = pages[pageNumber - 1];

            context.getSender().sendMessage(Component.text("There " + NumberDisplayer.toStringWithModifierAndPrefix(players.size(), " deputy", " deputies", "is ", "are ") + ". ")
                    .append(Component.text("Showing page " + pageNumber + "/" + pages.length + ".")).color(NamedTextColor.YELLOW));

            for (DeputyPlayer row : page) {
                context.getSender().sendMessage(
                        row.getPlayerIdentity().displayName()
                                .append(Component.text( " - " + row.getDeputyLevel().getName() + " deputy ").color(NamedTextColor.WHITE))
                                .append(Component.text(row.getExpiry() == 0 ?
                                                "(no expiry)" :
                                                "(expires in " + DurationDisplayer.asDurationString(row.getExpiry() - System.currentTimeMillis()) + ")")
                                        .color(NamedTextColor.GRAY)
                                )
                );
            }
        }
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(new PageNumberArgument<>(
                                false,
                                "page",
                                context -> deputyManager.getDeputies().stream()
                                        .filter(deputy -> deputy.checkListPermission(context.getSender()))
                                        .toList().size(),
                                ITEMS_PER_PAGE),
                        ArgumentDescription.of("page")
                );
    }

    @Override
    public String getUsage() {
        return "/deputy list [page]";
    }
}
