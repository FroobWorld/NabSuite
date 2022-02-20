package com.froobworld.nabsuite.modules.protect.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.froobworld.nabsuite.modules.protect.area.Area;
import com.froobworld.nabsuite.util.NumberDisplayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AreaHereCommand extends NabCommand {
    private final ProtectModule protectModule;

    public AreaHereCommand(ProtectModule protectModule) {
        super(
                "here",
                "Get a list of areas at your location.",
                "nabsuite.command.area.here",
                Player.class
        );
        this.protectModule = protectModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player sender = (Player) context.getSender();
        Set<Area> areas = protectModule.getAreaManager().getTopMostAreasAtLocation(sender.getLocation());
        if (areas.isEmpty()) {
            sender.sendMessage(
                    Component.text("There are no areas at this location.").color(NamedTextColor.YELLOW)
            );
        } else {
            List<Component> list = areas.stream()
                    .map(Area::getLongFormName)
                    .sorted(String::compareToIgnoreCase)
                    .map(Component::text)
                    .collect(Collectors.toList());
            sender.sendMessage(
                    Component.text("There " + NumberDisplayer.toStringWithModifierAndPrefix(areas.size(), " area", " areas", "is ", "are "))
                            .append(Component.text(" at this location.")).color(NamedTextColor.YELLOW)
            );
            sender.sendMessage(Component.join(JoinConfiguration.separator(Component.text(", ")), list));
        }
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder;
    }

    @Override
    public String getUsage() {
        return "/area here";
    }
}
