package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.flags.CommandFlag;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PageNumberArgument;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.channel.ChatChannel;
import com.froobworld.nabsuite.util.ListPaginator;
import com.froobworld.nabsuite.util.NumberDisplayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class ChannelsCommand extends NabCommand {
    private static final int ITEMS_PER_PAGE = 20;
    private final BasicsModule basicsModule;

    public ChannelsCommand(BasicsModule basicsModule) {
        super("channels",
                "Display a list of chat channels.",
                "nabsuite.command.channels",
                CommandSender.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        boolean showAll = context.flags().hasFlag("all");

        List<String> channels = basicsModule.getChatChannelManager().getChannels().stream()
                .filter(channel -> {
                    if (showAll) {
                        return true;
                    }
                    if (context.getSender() instanceof Player) {
                        return channel.hasUserRights((Player) context.getSender());
                    }
                    return true;
                })
                .map(ChatChannel::getName)
                .sorted(String::compareToIgnoreCase)
                .collect(Collectors.toList());

        if (channels.isEmpty()) {
            context.getSender().sendMessage(Component.text(
                    showAll ? "There are no channels." : "There are no channels that you can join.", NamedTextColor.YELLOW)
            );
        } else {
            int pageNumber = context.get("page");
            List<String>[] pages = ListPaginator.paginate(channels, ITEMS_PER_PAGE);
            List<String> page = pages[pageNumber - 1];
            context.getSender().sendMessage(
                    Component.text("There " + NumberDisplayer.toStringWithModifierAndPrefix(channels.size(), " channel", " channels", "is ", "are "))
                            .append(Component.text(showAll ? ". " : " you can access. "))
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
                                context -> {
                                    boolean showAll = context.flags().hasFlag("all");
                                    return Math.toIntExact(basicsModule.getChatChannelManager().getChannels().stream()
                                            .filter(channel -> {
                                                if (showAll) {
                                                    return true;
                                                }
                                                if (context.getSender() instanceof Player) {
                                                    return channel.hasUserRights((Player) context.getSender());
                                                }
                                                return true;
                                            })
                                            .count());
                                },
                                ITEMS_PER_PAGE
                        ),
                        ArgumentDescription.of("page")
                )
                .flag(
                        CommandFlag
                                .newBuilder("all")
                                .withAliases("a")
                                .build()
                );
    }

}
