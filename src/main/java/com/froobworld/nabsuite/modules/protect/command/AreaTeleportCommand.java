package com.froobworld.nabsuite.modules.protect.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.froobworld.nabsuite.modules.protect.area.Area;
import com.froobworld.nabsuite.modules.protect.command.argument.AreaArgument;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Consumer;

public class AreaTeleportCommand extends NabCommand {
    private final ProtectModule protectModule;
    private final Cache<String, UUID> recentTeleports = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(5)).build();

    public AreaTeleportCommand(ProtectModule protectModule) {
        super(
                "teleport",
                "Teleport to an area.",
                "nabsuite.command.area.teleport",
                Player.class
        );
        this.protectModule = protectModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        Area area = context.get("area");

        int xCentre = (area.getCorner1().getBlockX() + area.getCorner2().getBlockX()) / 2;
        int zCentre = (area.getCorner1().getBlockZ() + area.getCorner2().getBlockZ()) / 2;
        Location location = new Location(area.getWorld(), xCentre + 0.5, 0, zCentre + 0.5);

        UUID recentTeleport = recentTeleports.getIfPresent(area.getName());
        boolean coordinateWarning = !area.isApproved() && recentTeleport != null && recentTeleport != player.getUniqueId();
        if (recentTeleport == null) {
            recentTeleports.put(area.getName(), player.getUniqueId());
        }

        area.getWorld().getChunkAtAsync(location, (Consumer<Chunk>)  chunk -> {
            Location newLocation = chunk.getWorld().getHighestBlockAt(location).getLocation();
            newLocation.setY(newLocation.getY() + 1);
            protectModule.getPlugin().getModule(BasicsModule.class).getPlayerTeleporter().teleport(player, newLocation);
            player.sendMessage(Component.text("Teleported to area '" + area.getName() + "'.", NamedTextColor.YELLOW));

            if (coordinateWarning && Bukkit.getPlayer(recentTeleport) instanceof Player other) {
                player.sendMessage(other.displayName()
                        .append(Component.text(" recently teleported to this area, please coordinate review.").color(NamedTextColor.YELLOW)));
            }
        });
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(new AreaArgument<>(
                        true,
                        "area",
                        protectModule.getAreaManager()
                ));
    }

    @Override
    public String getUsage() {
        return "/area teleport <area>";
    }
}
