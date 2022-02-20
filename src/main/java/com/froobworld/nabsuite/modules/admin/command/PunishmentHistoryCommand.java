package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PageNumberArgument;
import com.froobworld.nabsuite.command.argument.arguments.PlayerIdentityArgument;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.punishment.PunishmentLogItem;
import com.froobworld.nabsuite.util.ListPaginator;
import com.froobworld.nabsuite.util.NumberDisplayer;
import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class PunishmentHistoryCommand extends NabCommand {
    private static final int ITEMS_PER_PAGE = 5;
    private final AdminModule adminModule;

    public PunishmentHistoryCommand(AdminModule adminModule) {
        super(
                "punishmenthistory",
                "Check the punishment history of a player.",
                "nabsuite.command.punishmenthistory",
                CommandSender.class
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        PlayerIdentity player = context.get("player");
        int pageNumber = context.get("page");
        List<PunishmentLogItem> punishmentHistory = Lists.reverse(adminModule.getPunishmentManager().getPunishments(player.getUuid()).getPunishmentHistory());
        if (punishmentHistory.isEmpty()) {
            context.getSender().sendMessage(
                    Component.text("That player has not received any punishments.").color(NamedTextColor.YELLOW)
            );
        } else {
            List<PunishmentLogItem>[] pages = ListPaginator.paginate(punishmentHistory, ITEMS_PER_PAGE);
            List<PunishmentLogItem> page = pages[pageNumber - 1];
            context.getSender().sendMessage(
                    Component.text()
            );
            context.getSender().sendMessage(
                    player.displayName()
                            .append(Component.text(" has " + NumberDisplayer.toStringWithModifier(punishmentHistory.size(), " record", " records", false) + ". "))
                            .append(Component.text("Showing page " + pageNumber + "/" + pages.length + ".")).color(NamedTextColor.YELLOW)
            );
            for (PunishmentLogItem record : page) {
                context.getSender().sendMessage(
                        Component.text("- ").append(record.toChatMessage())
                );
            }
        }
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(new PlayerIdentityArgument<>(
                        true,
                        "player",
                        adminModule.getPlugin().getPlayerIdentityManager(),
                        true
                ))
                .argument(new PageNumberArgument<>(
                        false,
                        "page",
                        context -> adminModule.getPunishmentManager().getPunishments(((PlayerIdentity) context.get("player")).getUuid()).getPunishmentHistory().size(),
                        PunishmentHistoryCommand.ITEMS_PER_PAGE
                ));
    }
}
