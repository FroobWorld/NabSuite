package com.froobworld.nabsuite.modules.admin.punishment;

import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.util.ConsoleUtils;
import com.froobworld.nabsuite.util.DurationDisplayer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class MuteEnforcer implements Listener {
    private final AdminModule adminModule;
    private final PunishmentManager punishmentManager;

    public MuteEnforcer(AdminModule adminModule, PunishmentManager punishmentManager) {
        this.adminModule = adminModule;
        this.punishmentManager = punishmentManager;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(adminModule.getPlugin(), () -> Bukkit.getOnlinePlayers().forEach(this::verifyMuteStatus), 100, 100);
        Bukkit.getPluginManager().registerEvents(this, adminModule.getPlugin());
    }

    public MutePunishment mute(PlayerIdentity player, CommandSender mediator, String reason, long duration) {
        Punishments punishments = punishmentManager.getPunishments(player.getUuid());
        MutePunishment mutePunishment = new MutePunishment(reason, ConsoleUtils.getSenderUUID(mediator), System.currentTimeMillis(), duration);
        punishments.setMutePunishment(mutePunishment);
        punishments.addPunishmentLogItem(new PunishmentLogItem(
                punishmentManager.adminModule.getPlugin().getPlayerIdentityManager(),
                PunishmentLogItem.Type.MUTE,
                player.getUuid(),
                mutePunishment.getMediator(),
                mutePunishment.getTime(),
                mutePunishment.getDuration(),
                mutePunishment.getReason()
        ));
        Player onlinePlayer = player.asPlayer();
        if (onlinePlayer != null) {
            Component message = Component.text("You have been muted");
            if (reason != null) {
                message = message.append(Component.text(" ("))
                        .append(Component.text(reason))
                        .append(Component.text(")"));
            }
            message = message.append(Component.text(".")).color(NamedTextColor.YELLOW);
            if (duration > 0) {
                message = message.append(Component.newline())
                        .append(Component.text("You will be unmuted in "))
                        .append(Component.text(DurationDisplayer.asMinutesHoursDays(duration)))
                        .append(Component.text("."))
                        .color(NamedTextColor.YELLOW);
            }
            onlinePlayer.sendMessage(message);
        }
        return mutePunishment;
    }

    public void unmute(PlayerIdentity player, CommandSender mediator) {
        unmute(false, player, ConsoleUtils.getSenderUUID(mediator));
    }

    private void expireMute(PlayerIdentity player) {
        unmute(true, player, ConsoleUtils.CONSOLE_UUID);
    }

    private void unmute(boolean automatic, PlayerIdentity player, UUID mediator) {
        Punishments punishments = punishmentManager.getPunishments(player.getUuid());
        punishments.setMutePunishment(null);
        punishments.addPunishmentLogItem(new PunishmentLogItem(
                punishmentManager.adminModule.getPlugin().getPlayerIdentityManager(),
                automatic ? PunishmentLogItem.Type.UNMUTE_AUTOMATIC : PunishmentLogItem.Type.UNMUTE_MANUAL,
                player.getUuid(),
                mediator,
                System.currentTimeMillis(),
                -1,
                null
        ));
        Player onlinePlayer = player.asPlayer();
        if (onlinePlayer != null) {
            onlinePlayer.sendMessage(
                    Component.text("You have been unmuted.").color(NamedTextColor.YELLOW)
            );
        }
    }

    private void verifyMuteStatus(Player player) {
        MutePunishment mutePunishment = punishmentManager.getPunishments(player.getUniqueId()).getMutePunishment();
        if (mutePunishment != null && mutePunishment.getDuration() > 0) {
            if (System.currentTimeMillis() >= mutePunishment.getTime() + mutePunishment.getDuration()) {
                expireMute(adminModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(player));
            }
        }
    }

    public boolean testMute(Player player, boolean notifyOnFail) {
        verifyMuteStatus(player);
        MutePunishment mutePunishment = punishmentManager.getPunishments(player.getUniqueId()).getMutePunishment();
        if (mutePunishment != null) {
            if (notifyOnFail) {
                Component message = Component.text("You are muted");
                if (mutePunishment.getReason() != null) {
                    message = message.append(Component.text(" ("))
                            .append(Component.text(mutePunishment.getReason()))
                            .append(Component.text(")"));
                }
                message = message.append(Component.text(".")).color(NamedTextColor.YELLOW);
                if (mutePunishment.getDuration() > 0) {
                    message = message.append(Component.newline())
                            .append(Component.text("You will be unmuted in "))
                            .append(Component.text(DurationDisplayer.asMinutesHoursDays(mutePunishment.getTime() + mutePunishment.getDuration() - System.currentTimeMillis())))
                            .append(Component.text("."))
                            .color(NamedTextColor.YELLOW);
                }
                player.sendMessage(message);
            }
            return true;
        }
        return false;
    }

    @EventHandler
    private void onChat(AsyncChatEvent event) {
        if (testMute(event.getPlayer(), true)) {
            event.setCancelled(true);
        }
    }

}
