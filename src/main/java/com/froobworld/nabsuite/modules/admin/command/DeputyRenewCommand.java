package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.DurationArgument;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.command.argument.DeputyPlayerArgument;
import com.froobworld.nabsuite.modules.admin.deputy.DeputyManager;
import com.froobworld.nabsuite.modules.admin.deputy.DeputyPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.Optional;

public class DeputyRenewCommand extends NabCommand {

    private AdminModule adminModule;
    private DeputyManager deputyManager;

    public DeputyRenewCommand(AdminModule adminModule) {
        super(
                "renew",
                "Renew deputation.",
                "nabsuite.command.deputy.renew",
                CommandSender.class
        );
        this.adminModule = adminModule;
        this.deputyManager = adminModule.getDeputyManager();
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        DeputyPlayer target = context.get("player");
        Optional<Long> duration = context.getOptional("duration");
        long durationMillis = duration.orElse(target.getDeputyLevel().getDefaultDuration());
        deputyManager.addDeputy(context.getSender(), target.getDeputyLevel(), target.getUuid(), durationMillis);
        context.getSender().sendMessage(
                Component.text("Deputation of " + target.getPlayerIdentity().getLastName() + " has been renewed.")
                        .color(NamedTextColor.YELLOW)
        );
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(new DeputyPlayerArgument<>(
                        true,
                        "player",
                        deputyManager,
                        new ArgumentPredicate<>(
                                false,
                                (context, deputy) -> deputy.checkManagePermission(context.getSender()),
                                "You lack permission to renew that deputy."
                        ),
                        new ArgumentPredicate<>(
                                false,
                                // Ensure that players is still a valid candidate
                                (context, deputy) -> deputy.getDeputyLevel().isEligible(adminModule.getGroupManager().getUser(deputy.getUuid())),
                                "That player is not a valid candidate for renewal."
                        )
                ))
                .argument(new DurationArgument<>(
                        false,
                        "duration",
                        new ArgumentPredicate<>(
                                false,
                                (context, dur) -> ((DeputyPlayer)context.get("player")).getDeputyLevel().getMaximumDuration() >= dur,
                                "Duration exceeds maximum duration."
                        )
                ));
    }

    @Override
    public String getUsage() {
        return "/deputy renew <player> [duration]";
    }

}
