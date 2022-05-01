package com.froobworld.nabsuite.modules.admin.punishment;

import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.util.ConsoleUtils;
import com.froobworld.nabsuite.util.DurationDisplayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerKickEvent;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public class BanEnforcer implements Listener {
    private final AdminModule adminModule;
    private final PunishmentManager punishmentManager;

    public BanEnforcer(AdminModule adminModule, PunishmentManager punishmentManager) {
        this.adminModule = adminModule;
        this.punishmentManager = punishmentManager;
        Bukkit.getPluginManager().registerEvents(this, adminModule.getPlugin());
    }

    public BanPunishment ban(PlayerIdentity player, CommandSender mediator, String reason, long duration) {
        Punishments punishments = punishmentManager.getPunishments(player.getUuid());
        BanPunishment banPunishment = new BanPunishment(reason, ConsoleUtils.getSenderUUID(mediator), System.currentTimeMillis(), duration);
        punishments.setBanPunishment(banPunishment);
        Player onlinePlayer = player.asPlayer();
        Component broadcastMessage = player.displayName().append(Component.text(" has been banned"));
        if (banPunishment.getReason() != null) {
            broadcastMessage = broadcastMessage.append(Component.text(" ("))
                    .append(Component.text(banPunishment.getReason()))
                    .append(Component.text(")"));
        }
        broadcastMessage = broadcastMessage.append(Component.text("."));
        Bukkit.broadcast(broadcastMessage);
        if (onlinePlayer != null) {
            onlinePlayer.kick(getKickMessage(banPunishment), PlayerKickEvent.Cause.BANNED);
        }
        punishments.addPunishmentLogItem(new PunishmentLogItem(
                punishmentManager.adminModule.getPlugin().getPlayerIdentityManager(),
                PunishmentLogItem.Type.BAN,
                player.getUuid(),
                banPunishment.getMediator(),
                banPunishment.getTime(),
                banPunishment.getDuration(),
                banPunishment.getReason()
        ));
        Bukkit.getBanList(BanList.Type.NAME).addBan(player.getUuid().toString(), banPunishment.getReason(), null, mediator.toString());
        return banPunishment;
    }

    public void unban(UUID player, CommandSender mediator) {
        unban(false, player, ConsoleUtils.getSenderUUID(mediator));
    }

    private void expireBan(UUID player) {
        unban(true, player, ConsoleUtils.CONSOLE_UUID);
    }

    private void unban(boolean automatic, UUID player, UUID mediator) {
        Punishments punishments = punishmentManager.getPunishments(player);
        punishments.setBanPunishment(null);
        Bukkit.getBanList(BanList.Type.NAME).pardon(player.toString());
        punishments.addPunishmentLogItem(new PunishmentLogItem(
                punishmentManager.adminModule.getPlugin().getPlayerIdentityManager(),
                automatic ? PunishmentLogItem.Type.UNBAN_AUTOMATIC : PunishmentLogItem.Type.UNBAN_MANUAL,
                player,
                mediator,
                System.currentTimeMillis(),
                -1,
                null
        ));
    }

    @EventHandler
    private void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
        BanPunishment banPunishment = punishmentManager.getPunishments(event.getUniqueId()).getBanPunishment();
        if (banPunishment == null) {
            return;
        }
        if (banPunishment.isPermanent() || banPunishment.getTime() + banPunishment.getDuration() > System.currentTimeMillis()) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
            event.kickMessage(getKickMessage(banPunishment));
        } else {
            expireBan(event.getUniqueId());
        }
    }

    private Component getKickMessage(BanPunishment banPunishment) {
        String banDate = new SimpleDateFormat("dd MMMM yyyy").format(Date.from(Instant.ofEpochMilli(banPunishment.getTime())));
        Component kickMessage = Component.text("You were banned on " + banDate + ".");
        if (banPunishment.getReason() != null) {
            kickMessage = kickMessage
                    .append(Component.newline())
                    .append(Component.newline())
                    .append(Component.text("The provided reason is \"" + banPunishment.getReason() + "\"."));
        }
        if (!banPunishment.isPermanent()) {
            kickMessage = kickMessage
                    .append(Component.newline())
                    .append(Component.newline())
                    .append(Component.text("You will be unbanned in "))
                    .append(Component.text(DurationDisplayer.asDurationString(banPunishment.getTime() + banPunishment.getDuration() - System.currentTimeMillis())).color(NamedTextColor.BLUE))
                    .append(Component.text("."));
        }
        kickMessage = kickMessage
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.text("Appeal at "))
                .append(Component.text(adminModule.getAdminConfig().banAppealUrl.get(), NamedTextColor.RED));

        return kickMessage;
    }

}
