package com.froobworld.nabsuite.modules.basics.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.command.argument.HomeArgument;
import com.froobworld.nabsuite.modules.basics.teleport.home.Home;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeleteHomeCommand extends NabCommand {
    private final BasicsModule basicsModule;

    public DeleteHomeCommand(BasicsModule basicsModule) {
        super(
                "delhome",
                "Delete a home.",
                "nabsuite.command.delhome",
                Player.class
        );
        this.basicsModule = basicsModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        Home home = context.get("home");
        basicsModule.getHomeManager().deleteHome(player, home);
        context.getSender().sendMessage(
                Component.text("Home deleted.").color(NamedTextColor.YELLOW)
        );
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(new HomeArgument<>(
                        false,
                        "home",
                        basicsModule.getHomeManager(),
                        context -> (Player) context.getSender()
                ));
    }
}
