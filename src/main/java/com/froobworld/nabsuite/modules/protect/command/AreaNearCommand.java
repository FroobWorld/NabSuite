package com.froobworld.nabsuite.modules.protect.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.NumberArgument;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.froobworld.nabsuite.modules.protect.area.Area;
import com.froobworld.nabsuite.util.NumberDisplayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AreaNearCommand extends NabCommand {
    private final ProtectModule protectModule;

    public AreaNearCommand(ProtectModule protectModule) {
        super(
                "near",
                "Get a list of areas near your location.",
                "nabsuite.command.area.near",
                Player.class
        );
        this.protectModule = protectModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {

        Optional<Integer> radiusArg = context.getOptional("radius");
        Integer radius = radiusArg.orElse(protectModule.getConfig().areaNearSettings.radius.get());
        Integer limit = protectModule.getConfig().areaNearSettings.limit.get();

        Player sender = (Player) context.getSender();
        Location loc = sender.getLocation();
        List<AreaDistance> areas = protectModule.getAreaManager().getAreasNear(loc, radius).stream()
                .map(area -> new AreaDistance(area, area.closestLocation(loc).distance(loc)))
                .sorted(Comparator.comparingDouble(a -> a.distance))
                .limit(limit)
                .toList();

        String radiusStr = NumberDisplayer.toStringWithModifier(radius, " block", " blocks", false);

        if (areas.isEmpty()) {
            sender.sendMessage(
                    Component.text("There are no areas within "+radiusStr+" of your location.").color(NamedTextColor.YELLOW)
            );
        } else {
            List<Component> list = areas.stream()
                    .map(a -> Component.empty()
                            .append(
                                Component.text(a.area.getLongFormName())
                                        .clickEvent(ClickEvent.runCommand("/area info "+a.area.getLongFormName()))
                                        .color(NamedTextColor.RED)
                            )
                            .append(Component.text(" "))
                            .append(
                                Component.text("(" + (a.distance == 0 ? "here" : (int)Math.ceil(a.distance)) + ")")
                                        .clickEvent(ClickEvent.runCommand("/area visualise "+a.area.getLongFormName()))
                            )
                    )
                    .collect(Collectors.toList());
            sender.sendMessage(
                    Component.text("There " + NumberDisplayer.toStringWithModifierAndPrefix(areas.size(), " area", " areas", "is "+(areas.size()==limit?"at least ":""), "are "+(areas.size()==limit?"at least ":"")))
                            .append(Component.text(" within "+radiusStr+" of your location.")).color(NamedTextColor.YELLOW)
            );
            sender.sendMessage(Component.join(JoinConfiguration.separator(Component.text(", ")), list));
        }
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(
                        new NumberArgument<>(
                                false,
                                "radius",
                                context -> 1,
                                context -> protectModule.getConfig().areaNearSettings.maxRadius.get()
                        )
                );
    }

    @Override
    public String getUsage() {
        return "/area near [radius]";
    }

    private static class AreaDistance {
        private final Area area;
        private final double distance;

        public AreaDistance(Area area, double distance) {
            this.area = area;
            this.distance = distance;
        }
    }

}
