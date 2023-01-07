package com.froobworld.nabsuite.modules.protect.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.predicate.ArgumentPredicate;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.froobworld.nabsuite.modules.protect.area.Area;
import com.froobworld.nabsuite.modules.protect.command.argument.AreaArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;

public class AreaReviewCommand extends NabCommand {
    private final ProtectModule protectModule;

    public AreaReviewCommand(ProtectModule protectModule) {
        super(
                "review",
                "Review a requested area.",
                "nabsuite.command.area.review",
                CommandSender.class
        );
        this.protectModule = protectModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        Area area = context.get("area");

        sender.sendMessage(
                Component.text("----- Information for " + area.getName() + " -----").color(NamedTextColor.YELLOW)
        );
        String creatorName = protectModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(area.getCreator()).getLastName();
        sender.sendMessage(
                Component.text("Requestor: ", NamedTextColor.YELLOW)
                        .append(Component.text(creatorName, NamedTextColor.WHITE))
        );
        sender.sendMessage(
                Component.text("World: ", NamedTextColor.YELLOW)
                        .append(Component.text(area.getWorld().getName(), NamedTextColor.WHITE))
        );
        int xCentre = (area.getCorner1().getBlockX() + area.getCorner2().getBlockX()) / 2;
        int zCentre = (area.getCorner1().getBlockZ() + area.getCorner2().getBlockZ()) / 2;
        String mapLink = protectModule.getConfig().mapReviewUrl.get() +
                "?worldname=" + area.getWorld().getName() +
                "&zoom=6" +
                "&x=" + xCentre +
                "&z=" + zCentre;
        sender.sendMessage(
                Component.text("Centre: ", NamedTextColor.YELLOW)
                        .append(Component.text("(" + xCentre + ", " + zCentre + ")", NamedTextColor.WHITE))
                        .append(Component.text(" [Dynmap]", NamedTextColor.GRAY, TextDecoration.ITALIC).clickEvent(ClickEvent.openUrl(mapLink)))
                        .append(Component.text(" [Teleport]", NamedTextColor.GRAY, TextDecoration.ITALIC).clickEvent(ClickEvent.runCommand("/area teleport " + area.getName())))
                        .append(Component.text(" [Visualise]", NamedTextColor.GRAY, TextDecoration.ITALIC).clickEvent(ClickEvent.runCommand("/area visualise " + area.getName())))

        );
        int xWidth = Math.abs(area.getCorner1().getBlockX() - area.getCorner2().getBlockX()) + 1;
        int zWidth = Math.abs(area.getCorner1().getBlockZ() - area.getCorner2().getBlockZ()) + 1;
        sender.sendMessage(
                Component.text("Dimensions: ", NamedTextColor.YELLOW)
                        .append(Component.text(xWidth + "x" + zWidth, NamedTextColor.WHITE))
        );
        sender.sendMessage(Component.empty());
        sender.sendMessage(
                Component.text("To approve the area, use '", NamedTextColor.YELLOW)
                        .append(Component.text("/area approve " + area.getName(), NamedTextColor.GREEN))
                        .append(Component.text("'.", NamedTextColor.YELLOW))
                        .clickEvent(ClickEvent.suggestCommand("/area approve " + area.getName()))
        );
        sender.sendMessage(
                Component.text("To deny the area, use '", NamedTextColor.YELLOW)
                        .append(Component.text("/area deny " + area.getName(), NamedTextColor.RED))
                        .append(Component.text("'.", NamedTextColor.YELLOW))
                        .clickEvent(ClickEvent.suggestCommand("/area deny " + area.getName() + " "))
        );
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(new AreaArgument<>(
                        true,
                        "area",
                        protectModule.getAreaManager(),
                        new ArgumentPredicate<>(
                                true,
                                (context, area) -> !area.isApproved(),
                                "That area doesn't need reviewing"
                        )
                ));
    }

    @Override
    public String getUsage() {
        return "/area review <area>";
    }
}
