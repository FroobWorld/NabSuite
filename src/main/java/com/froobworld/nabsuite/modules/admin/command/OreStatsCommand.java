package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PlayerIdentityArgument;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.xray.PlayerOreStatsData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class OreStatsCommand extends NabCommand {
    private final AdminModule adminModule;

    public OreStatsCommand(AdminModule adminModule) {
        super(
                "orestats",
                "Get a player's ore stats.",
                "nabsuite.command.orestats",
                CommandSender.class,
                "os"
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        PlayerIdentity playerIdentity = context.get("player");
        PlayerOreStatsData oreStatsData = adminModule.getOreStatsManager().getOreStatsData(playerIdentity.getUuid());
        Component hoverText = Component.empty();
        oreStatsData.lock.readLock().lock();
        try {
            hoverText = hoverText
                    .append(Component.text("--- Overworld stats ---", NamedTextColor.YELLOW)).append(Component.newline())
                    .append(Component.text("Stone: ", NamedTextColor.GRAY).append(quantityStringOverworld(oreStatsData.getStone(), oreStatsData))).append(Component.newline())
                    .append(Component.text("Coal: ", NamedTextColor.DARK_GRAY).append(quantityStringOverworld(oreStatsData.getCoal(), oreStatsData))).append(Component.newline())
                    .append(Component.text("Copper: ", NamedTextColor.DARK_RED).append(quantityStringOverworld(oreStatsData.getCopper(), oreStatsData))).append(Component.newline())
                    .append(Component.text("Iron: ", NamedTextColor.DARK_GRAY).append(quantityStringOverworld(oreStatsData.getIron(), oreStatsData))).append(Component.newline())
                    .append(Component.text("Lapis: ", NamedTextColor.BLUE).append(quantityStringOverworld(oreStatsData.getLapis(), oreStatsData))).append(Component.newline())
                    .append(Component.text("Gold: ", NamedTextColor.GOLD).append(quantityStringOverworld(oreStatsData.getGold(), oreStatsData))).append(Component.newline())
                    .append(Component.text("Redstone: ", NamedTextColor.RED).append(quantityStringOverworld(oreStatsData.getRedstone(), oreStatsData))).append(Component.newline())
                    .append(Component.text("Emerald: ", NamedTextColor.GREEN).append(quantityStringOverworld(oreStatsData.getEmerald(), oreStatsData))).append(Component.newline())
                    .append(Component.text("Diamond: ", NamedTextColor.AQUA).append(quantityStringOverworld(oreStatsData.getDiamond(), oreStatsData))).append(Component.newline()).append(Component.newline())
                    .append(Component.text("--- Nether stats ---", NamedTextColor.YELLOW)).append(Component.newline())
                    .append(Component.text("Netherrack: ", NamedTextColor.RED).append(quantityStringNether(oreStatsData.getNetherrack(), oreStatsData))).append(Component.newline())
                    .append(Component.text("Quartz: ", NamedTextColor.GRAY).append(quantityStringNether(oreStatsData.getQuartz(), oreStatsData))).append(Component.newline())
                    .append(Component.text("Nether gold: ", NamedTextColor.GOLD).append(quantityStringNether(oreStatsData.getNetherGold(), oreStatsData))).append(Component.newline())
                    .append(Component.text("Ancient debris: ", NamedTextColor.DARK_PURPLE).append(quantityStringNether(oreStatsData.getNetherite(), oreStatsData)));
        } finally {
            oreStatsData.lock.readLock().unlock();
        }
        if (sender instanceof Player) {
            sender.sendMessage(
                    Component.text("[Hover for ore stats of ", NamedTextColor.YELLOW)
                            .append(playerIdentity.displayName())
                            .append(Component.text("]", NamedTextColor.YELLOW))
                            .hoverEvent(
                                    HoverEvent.showText(
                                            hoverText
                                    )
                            )
            );
        } else {
            sender.sendMessage(
                    Component.text("Ore stats for ", NamedTextColor.YELLOW)
                            .append(playerIdentity.displayName())
                            .append(Component.text(":", NamedTextColor.YELLOW))
            );
            sender.sendMessage(hoverText);
        }
    }

    private Component quantityStringOverworld(int value, PlayerOreStatsData oreStatsData) {
        int total = oreStatsData.getStone() + oreStatsData.getCoal() + oreStatsData.getCopper() + oreStatsData.getIron() +
                oreStatsData.getGold() + oreStatsData.getDiamond() + oreStatsData.getEmerald() + oreStatsData.getLapis() + oreStatsData.getRedstone();
        if (value == 0 || total == 0) {
            return Component.text("nil", NamedTextColor.WHITE);
        }
        double percentageValue = new BigDecimal((double) value / total * 100.0).setScale(1, RoundingMode.HALF_UP).doubleValue();

        return Component.text(value + " (" + percentageValue + "%)", NamedTextColor.WHITE);
    }

    private Component quantityStringNether(int value, PlayerOreStatsData oreStatsData) {
        int total = oreStatsData.getNetherrack() + oreStatsData.getNetherGold() + oreStatsData.getQuartz() + oreStatsData.getNetherite();
        if (value == 0 || total == 0) {
            return Component.text("nil", NamedTextColor.WHITE);
        }
        double percentageValue = new BigDecimal((double) value / total * 100.0).setScale(1, RoundingMode.HALF_UP).doubleValue();

        return Component.text(value + " (" + percentageValue + "%)", NamedTextColor.WHITE);
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(
                        new PlayerIdentityArgument<>(
                                true,
                                "player",
                                adminModule.getPlugin().getPlayerIdentityManager(),
                                true
                        )
                );
    }
}
