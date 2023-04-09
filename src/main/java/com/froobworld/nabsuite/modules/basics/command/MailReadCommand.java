package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PageNumberArgument;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.player.mail.MailBox;
import com.froobworld.nabsuite.util.ListPaginator;
import com.froobworld.nabsuite.util.NumberDisplayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class MailReadCommand extends NabCommand {
    private static final int ITEMS_PER_PAGE = 5;
    private final BasicsModule basicsModule;

    public MailReadCommand(BasicsModule basicsModule) {
        super("read",
                "Read your mail.",
                "nabsuite.command.mail.read",
                Player.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        MailBox mailBox = basicsModule.getMailCentre().getMailBox(player.getUniqueId());
        List<Component> mailComponents = mailBox.getMail().stream()
                .map(mail -> mail.asText(basicsModule.getPlugin().getPlayerIdentityManager()))
                .toList();

        if (mailComponents.isEmpty()) {
            player.sendMessage(Component.text("You have no mail.").color(NamedTextColor.YELLOW));
        } else {
            int pageNumber = context.get("page");
            List<Component>[] pages = ListPaginator.paginate(mailComponents, ITEMS_PER_PAGE, true);
            if (pageNumber > pages.length) {
                player.sendMessage(
                        Component.text("Page number exceeds maximum.", NamedTextColor.RED)
                );
                return;
            }
            List<Component> page = pages[pageNumber - 1];
            player.sendMessage(
                    Component.text("You have " + NumberDisplayer.toStringWithModifier(mailComponents.size(), " message.", " messages.", false))
                            .append(Component.text(" Showing page " + pageNumber + "/" + pages.length + "."))
                            .color(NamedTextColor.YELLOW)
            );
            for (Component mail : page) {
                player.sendMessage(
                        Component.text("- ", NamedTextColor.RED)
                                .append(mail)
                );
            }
        }
        mailBox.markAsRead();
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
                                    return basicsModule.getMailCentre().getMailBox(player.getUniqueId()).getMail().size();
                                },
                                ITEMS_PER_PAGE),
                        ArgumentDescription.of("page")
                );
    }

    @Override
    public String getUsage() {
        return "/mail read [page]";
    }

}
