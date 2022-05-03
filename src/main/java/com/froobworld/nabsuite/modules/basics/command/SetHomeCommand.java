package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.StringArgument;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.command.argument.predicate.predicates.PatternArgumentPredicate;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.teleport.home.Home;
import com.froobworld.nabsuite.modules.basics.teleport.home.HomeManager;
import com.froobworld.nabsuite.util.NumberDisplayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetHomeCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public SetHomeCommand(BasicsModule basicsModule) {
        super(
                "sethome",
                "Set a home at your location.",
                "nabsuite.command.sethome",
                Player.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        String homeName = context.get("name");
        int maxHomes = basicsModule.getHomeManager().getMaxHomes(player);
        if (basicsModule.getHomeManager().getHomes(player).getHomes().size() >= maxHomes) {
            player.sendMessage(
                    Component.text("You can only set " + NumberDisplayer.toStringWithModifier(maxHomes, " home", " homes", false) + ".").color(NamedTextColor.RED)
            );
            return;
        }

        Home home = basicsModule.getHomeManager().createHome(player, homeName);
        player.sendMessage(
                Component.text("Created home '" + home.getName() + "' at your location.").color(NamedTextColor.YELLOW)
        );
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(
                        new StringArgument<>(
                                false,
                                "name",
                                "default",
                                false,
                                new PatternArgumentPredicate<>(
                                        HomeManager.homeNamePattern,
                                        "Name must only contain letters, numbers, underscores and dashes"
                                ),
                                new ArgumentPredicate<>(
                                        true,
                                        (context, string) -> (basicsModule.getHomeManager().getHomes((Player) context.getSender()).getHome(string) == null),
                                        "Home already exists. Use /delhome to delete the home first"
                                )
                        ),
                        ArgumentDescription.of("name")
                );
    }
}
