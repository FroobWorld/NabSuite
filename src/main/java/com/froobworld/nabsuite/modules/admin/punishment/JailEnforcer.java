package com.froobworld.nabsuite.modules.admin.punishment;

import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.jail.Jail;
import com.froobworld.nabsuite.util.ConsoleUtils;
import com.froobworld.nabsuite.util.DurationDisplayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.UUID;

public class JailEnforcer implements Listener {
    private final AdminModule adminModule;
    private final PunishmentManager punishmentManager;

    public JailEnforcer(AdminModule adminModule, PunishmentManager punishmentManager) {
        this.adminModule = adminModule;
        this.punishmentManager = punishmentManager;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(adminModule.getPlugin(), () -> Bukkit.getOnlinePlayers().forEach(this::containToJail), 20, 20);
        Bukkit.getPluginManager().registerEvents(this, adminModule.getPlugin());
    }

    public JailPunishment jail(PlayerIdentity player, CommandSender mediator, Jail jail, String reason, long duration) {
        Punishments punishments = punishmentManager.getPunishments(player.getUuid());
        JailPunishment jailPunishment = new JailPunishment(adminModule.getJailManager(), jail.getName(), reason, ConsoleUtils.getSenderUUID(mediator), System.currentTimeMillis(), duration);
        punishments.setJailPunishment(jailPunishment);
        punishments.addPunishmentLogItem(new PunishmentLogItem(
                punishmentManager.adminModule.getPlugin().getPlayerIdentityManager(),
                PunishmentLogItem.Type.JAIL,
                player.getUuid(),
                jailPunishment.getMediator(),
                jailPunishment.getTime(),
                jailPunishment.getDuration(),
                jailPunishment.getReason()
        ));
        Player onlinePlayer = player.asPlayer();
        if (onlinePlayer != null) {
            Component message = Component.newline()
                    .append(Component.text("You have been jailed"));
            if (reason != null) {
                message = message.append(Component.text(" ("))
                        .append(Component.text(reason, NamedTextColor.GOLD))
                        .append(Component.text(")"));
            }
            message = message.append(Component.text(".")).color(NamedTextColor.RED);
            if (duration > 0) {
                message = message.append(Component.newline())
                        .append(Component.newline())
                        .append(Component.text("You will be released in "))
                        .append(Component.text(DurationDisplayer.asDurationString(jailPunishment.getTime() + jailPunishment.getDuration() - System.currentTimeMillis()), NamedTextColor.GOLD))
                        .append(Component.text("."))
                        .color(NamedTextColor.RED);
            }
            String banAppealUrl = adminModule.getAdminConfig().banAppealUrl.get();
            if (banAppealUrl != null && !banAppealUrl.isEmpty()) {
                message = message.append(Component.newline())
                        .append(Component.newline())
                        .append(Component.text("Appeal at "))
                        .append(Component.text(banAppealUrl).color(NamedTextColor.GOLD))
                        .append(Component.text("."))
                        .color(NamedTextColor.RED)
                        .clickEvent(ClickEvent.openUrl("https://" + banAppealUrl));
            }
            message = message.append(Component.newline());
            Component finalMessage = message;
            onlinePlayer.teleportAsync(jail.getLocation())
                    .thenAccept(b -> {
                        onlinePlayer.sendMessage(finalMessage);
                    });
        }
        return jailPunishment;
    }

    public void unjail(PlayerIdentity player, CommandSender mediator) {
        unjail(false, player, ConsoleUtils.getSenderUUID(mediator));
    }

    private void expireJail(PlayerIdentity player) {
        unjail(true, player, ConsoleUtils.CONSOLE_UUID);
    }

    private void unjail(boolean automatic, PlayerIdentity player, UUID mediator) {
        Punishments punishments = punishmentManager.getPunishments(player.getUuid());
        punishments.setJailPunishment(null);
        punishments.addPunishmentLogItem(new PunishmentLogItem(
                punishmentManager.adminModule.getPlugin().getPlayerIdentityManager(),
                automatic ? PunishmentLogItem.Type.UNJAIL_AUTOMATIC : PunishmentLogItem.Type.UNJAIL_MANUAL,
                player.getUuid(),
                mediator,
                System.currentTimeMillis(),
                -1,
                null
        ));
        Player onlinePlayer = player.asPlayer();
        if (onlinePlayer != null) {
            onlinePlayer.sendMessage(
                    Component.text("You have been unjailed.").color(NamedTextColor.YELLOW)
            );
        }
    }

    private void verifyJailStatus(Player player) {
        JailPunishment jailPunishment = punishmentManager.getPunishments(player.getUniqueId()).getJailPunishment();
        if (jailPunishment != null && jailPunishment.getDuration() > 0) {
            if (System.currentTimeMillis() >= jailPunishment.getTime() + jailPunishment.getDuration()) {
                expireJail(adminModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(player));
            }
        }
    }

    public boolean testJail(Player player, boolean notifyOnFail) {
        verifyJailStatus(player);
        JailPunishment jailPunishment = punishmentManager.getPunishments(player.getUniqueId()).getJailPunishment();
        if (jailPunishment != null) {
            if (notifyOnFail) {
                sendJailMessage(player, jailPunishment);
            }
            return true;
        }
        return false;
    }

    private void sendJailMessage(Player player, JailPunishment jailPunishment) {
        Component message = Component.newline()
                .append(Component.text("You are jailed"));
        if (jailPunishment.getReason() != null) {
            message = message.append(Component.text(" ("))
                    .append(Component.text(jailPunishment.getReason(), NamedTextColor.GOLD))
                    .append(Component.text(")"));
        }
        message = message.append(Component.text(".")).color(NamedTextColor.RED);
        if (jailPunishment.getDuration() > 0) {
            message = message.append(Component.newline())
                    .append(Component.newline())
                    .append(Component.text("You will be released in "))
                    .append(Component.text(DurationDisplayer.asDurationString(jailPunishment.getTime() + jailPunishment.getDuration() - System.currentTimeMillis()), NamedTextColor.GOLD))
                    .append(Component.text("."))
                    .color(NamedTextColor.RED);
        }
        String banAppealUrl = adminModule.getAdminConfig().banAppealUrl.get();
        if (banAppealUrl != null && !banAppealUrl.isEmpty()) {
            message = message.append(Component.newline())
                    .append(Component.newline())
                    .append(Component.text("Appeal at "))
                    .append(Component.text(banAppealUrl).color(NamedTextColor.GOLD))
                    .append(Component.text("."))
                    .color(NamedTextColor.RED)
                    .clickEvent(ClickEvent.openUrl("https://" + banAppealUrl));
        }
        message = message.append(Component.newline());
        player.sendMessage(message);
    }

    private void containToJail(Player player) {
        verifyJailStatus(player);
        JailPunishment jailPunishment = punishmentManager.getPunishments(player.getUniqueId()).getJailPunishment();
        if (jailPunishment != null) {
            Jail jail = jailPunishment.getJail();
            if (player.getLocation().getWorld() != jail.getLocation().getWorld() || player.getLocation().distanceSquared(jail.getLocation()) > jail.getRadius() * jail.getRadius()) {
                player.teleportAsync(jail.getLocation())
                        .thenAccept(b -> {
                            sendJailMessage(player, jailPunishment);
                        });
            }
        }
    }

    @EventHandler
    private void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (testJail(event.getPlayer(), true)) {
            if (!adminModule.getAdminConfig().jailCommandWhitelist.get().contains(event.getMessage().toLowerCase().split(" ")[0])) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    private void onBlockBreak(BlockBreakEvent event) {
        if (testJail(event.getPlayer(), false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onBlockPlace(BlockPlaceEvent event) {
        if (testJail(event.getPlayer(), false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_AIR) {
            return;
        }
        if (testJail(event.getPlayer(), false)) {
            event.setCancelled(true);
        }
    }

}
