package com.froobworld.nabsuite.modules.admin.punishment;

import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.util.ConsoleUtils;
import com.froobworld.nabsuite.util.DurationDisplayer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
        Bukkit.getScheduler().scheduleSyncRepeatingTask(adminModule.getPlugin(), () -> Bukkit.getOnlinePlayers().forEach(player -> verifyMuteStatus(player.getUniqueId())), 100, 100);
        Bukkit.getPluginManager().registerEvents(this, adminModule.getPlugin());
    }

    public MutePunishment mute(PlayerIdentity player, CommandSender mediator, String reason, long duration, boolean shadow) {
        Punishments punishments = punishmentManager.getPunishments(player.getUuid());
        MutePunishment mutePunishment = new MutePunishment(reason, ConsoleUtils.getSenderUUID(mediator), System.currentTimeMillis(), duration, shadow);
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
        if (onlinePlayer != null && !shadow) {
            Component message = Component.newline()
                    .append(Component.text("You have been muted"));
            if (reason != null) {
                message = message.append(Component.text(" ("))
                        .append(Component.text(reason, NamedTextColor.GOLD))
                        .append(Component.text(")"));
            }
            message = message.append(Component.text(".")).color(NamedTextColor.YELLOW);
            if (duration > 0) {
                message = message.append(Component.newline())
                        .append(Component.newline())
                        .append(Component.text("You will be unmuted in "))
                        .append(Component.text(DurationDisplayer.asDurationString(duration), NamedTextColor.GOLD))
                        .append(Component.text("."))
                        .color(NamedTextColor.RED);
            }
            message = message.append(Component.newline());
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
        MutePunishment mutePunishment = punishments.getMutePunishment();
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
        if (onlinePlayer != null && !mutePunishment.isShadow()) {
            onlinePlayer.sendMessage(
                    Component.text("You have been unmuted.").color(NamedTextColor.YELLOW)
            );
        }
    }

    private void verifyMuteStatus(UUID uuid) {
        MutePunishment mutePunishment = punishmentManager.getPunishments(uuid).getMutePunishment();
        if (mutePunishment != null && mutePunishment.getDuration() > 0) {
            if (System.currentTimeMillis() >= mutePunishment.getTime() + mutePunishment.getDuration()) {
                expireMute(adminModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(uuid));
            }
        }
    }

    public boolean testMute(UUID uuid) {
        verifyMuteStatus(uuid);
        MutePunishment mutePunishment = punishmentManager.getPunishments(uuid).getMutePunishment();
        return mutePunishment != null;
    }

    public boolean testMute(Player player, boolean notifyOnFail) {
        verifyMuteStatus(player.getUniqueId());
        MutePunishment mutePunishment = punishmentManager.getPunishments(player.getUniqueId()).getMutePunishment();
        if (testMute(player.getUniqueId())) {
            if (notifyOnFail && !mutePunishment.isShadow()) {
                Component message = Component.newline()
                        .append(Component.text("You are muted"));
                if (mutePunishment.getReason() != null) {
                    message = message.append(Component.text(" ("))
                            .append(Component.text(mutePunishment.getReason(), NamedTextColor.GOLD))
                            .append(Component.text(")"));
                }
                message = message.append(Component.text(".")).color(NamedTextColor.RED);
                if (mutePunishment.getDuration() > 0) {
                    message = message.append(Component.newline())
                            .append(Component.newline())
                            .append(Component.text("You will be unmuted in "))
                            .append(Component.text(DurationDisplayer.asDurationString(mutePunishment.getTime() + mutePunishment.getDuration() - System.currentTimeMillis()), NamedTextColor.GOLD))
                            .append(Component.text("."))
                            .color(NamedTextColor.RED);
                }
                message = message.append(Component.newline());
                player.sendMessage(message);
            }
            return true;
        }
        return false;
    }

    private boolean isShadowMute(Player player) {
        MutePunishment mutePunishment = punishmentManager.getPunishments(player.getUniqueId()).getMutePunishment();
        return mutePunishment != null && mutePunishment.isShadow();
    }

    @EventHandler
    private void onChat(AsyncChatEvent event) {
        if (testMute(event.getPlayer(), true)) {
            if (isShadowMute(event.getPlayer())) {
                //noinspection OverrideOnly
                event.getPlayer().sendMessage(event.renderer().render(event.getPlayer(), event.getPlayer().displayName(), event.message(), event.getPlayer()));
                adminModule.getPlugin().getSLF4JLogger().info("(Shadow muted) {}: {}", event.getPlayer().getName(), PlainTextComponentSerializer.plainText().serialize(event.message()));
            }
            event.setCancelled(true);
        }
    }

}
