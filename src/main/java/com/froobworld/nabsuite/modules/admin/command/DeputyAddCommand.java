package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.DurationArgument;
import com.froobworld.nabsuite.command.argument.arguments.PlayerIdentityArgument;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.command.argument.DeputyLevelArgument;
import com.froobworld.nabsuite.modules.admin.deputy.DeputyLevel;
import com.froobworld.nabsuite.modules.admin.deputy.DeputyManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.Optional;

public class DeputyAddCommand extends NabCommand {

    private AdminModule adminModule;
    private DeputyManager deputyManager;

    public DeputyAddCommand(AdminModule adminModule) {
        super(
                "add",
                "Deputise a player.",
                "nabsuite.command.deputy.add",
                CommandSender.class
        );
        this.adminModule = adminModule;
        this.deputyManager = adminModule.getDeputyManager();
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        DeputyLevel deputyLevel = context.get("deputyLevel");
        PlayerIdentity target = context.get("player");
        Optional<Long> duration = context.getOptional("duration");
        long durationMillis = duration.orElse(deputyLevel.getDefaultDuration());
        deputyManager.addDeputy(context.getSender(), deputyLevel, target.getUuid(), durationMillis);
        context.getSender().sendMessage(Component.text("Player " + target.getLastName() + " has been deputised.").color(NamedTextColor.YELLOW));
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(new DeputyLevelArgument<>(
                        true,
                        "deputyLevel",
                        deputyManager,
                        new ArgumentPredicate<>(
                                true,
                                (context, level) -> level.checkManagePermission(context.getSender()),
                                "Invalid deputy level."
                        )
                ), ArgumentDescription.of("deputy level"))
                .argument(new PlayerIdentityArgument<>(
                        true,
                        "player",
                        adminModule.getPlugin().getPlayerIdentityManager(),
                        true,
                        new ArgumentPredicate<>(
                                false,
                                (context, player) ->((DeputyLevel)context.get("deputyLevel")).isEligible(adminModule.getGroupManager().getUser(player.getUuid())),
                                "That player is not a valid deputy candidate."
                        ),
                        new ArgumentPredicate<>(
                                false,
                                (context, player) -> deputyManager.getDeputy(player.getUuid()) == null,
                                "That player is already a deputy."
                        )
                ))
                .argument(new DurationArgument<>(
                        false,
                        "duration",
                        new ArgumentPredicate<>(
                                false,
                                (context, dur) -> ((DeputyLevel)context.get("deputyLevel")).getMaximumDuration() >= dur,
                                "Duration exceeds maximum duration."
                        )
                ));
    }

    @Override
    public String getUsage() {
        return "/deputy add <deputy level> <player> [duration]";
    }
}
