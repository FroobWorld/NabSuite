package com.froobworld.nabsuite.modules.protect.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.froobworld.nabsuite.modules.protect.area.Area;
import com.froobworld.nabsuite.modules.protect.command.argument.AreaArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AreaVisualiseCommand extends NabCommand {
    private final ProtectModule protectModule;

    public AreaVisualiseCommand(ProtectModule protectModule) {
        super(
                "visualise",
                "Get a graphical visualisation of an area.",
                "nabsuite.command.area.visualise",
                Player.class,
                "visualize"
        );
        this.protectModule = protectModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        Area area = context.get("area");
        protectModule.getAreaManager().getAreaVisualiser().visualiseArea(player, area);
        player.sendMessage(
                Component.text("Now visualising " + area.getName() + ".", NamedTextColor.YELLOW)
        );
        player.sendMessage(
                Component.text("Type ", NamedTextColor.YELLOW)
                        .append(
                                Component.text("/area stopvisualising", NamedTextColor.RED)
                                        .clickEvent(ClickEvent.runCommand("/area stopvisualising"))
                        )
                        .append(Component.text(" to stop the visualisation.", NamedTextColor.YELLOW))
        );
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(
                        new AreaArgument<>(
                                true,
                                "area",
                                protectModule.getAreaManager()
                        )
                );
    }

    @Override
    public String getUsage() {
        return "/area visualise <area>";
    }
}
