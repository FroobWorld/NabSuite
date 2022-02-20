package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.DoubleArgument;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.StringArgument;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.command.argument.predicate.predicates.PatternArgumentPredicate;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.jail.Jail;
import com.froobworld.nabsuite.modules.admin.jail.JailManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetJailCommand extends NabCommand {
    private final AdminModule adminModule;

    public SetJailCommand(AdminModule adminModule) {
        super(
                "setjail",
                "Set a jail at your location.",
                "nabsuite.command.setjail",
                Player.class
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        String jailName = context.get("name");
        double radius = context.get("radius");
        Jail jail = adminModule.getJailManager().createJail(jailName, radius, player);
        player.sendMessage(
                Component.text("Created jail '" + jail.getName() + "' at your location").color(NamedTextColor.YELLOW)
        );
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(
                        new StringArgument<>(
                                true,
                                "name",
                                false,
                                new PatternArgumentPredicate<>(
                                        JailManager.jailNamePattern,
                                        "Name must only contain letters, numbers, underscores and dashes"
                                ),
                                new ArgumentPredicate<>(
                                        true,
                                        (context, string) -> (adminModule.getJailManager().getJail(string) == null),
                                        "Jail already exists"
                                )
                        ),
                        ArgumentDescription.of("name")
                )
                .argument(DoubleArgument.<CommandSender>newBuilder("radius")
                        .withMin(1)
                        .build()
                );
    }
}
