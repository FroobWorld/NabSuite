package com.froobworld.nabsuite.modules.admin.suspicious;

import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.suspicious.monitors.ActivityMonitor;
import com.froobworld.nabsuite.modules.admin.suspicious.monitors.GriefMonitor;
import com.froobworld.nabsuite.modules.admin.suspicious.monitors.LavaCastMonitor;
import com.froobworld.nabsuite.modules.admin.suspicious.monitors.TheftMonitor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class SuspiciousActivityMonitor {
    private static final double SUSPICION_THRESHOLD = 2.0;
    private static final long TIME_PLAYED_THRESHOLD = TimeUnit.MINUTES.toSeconds(90) * 20; // 90 minutes in ticks
    private final NamespacedKey pdcKey;
    private final List<ActivityMonitor> monitors;
    private final AdminModule adminModule;

    public SuspiciousActivityMonitor(AdminModule adminModule) {
        this.pdcKey = NamespacedKey.fromString("tripped-suspicion-monitor", adminModule.getPlugin());
        monitors = List.of(
                new TheftMonitor(adminModule),
                new LavaCastMonitor(adminModule),
                new GriefMonitor(adminModule)
        );
        this.adminModule = adminModule;
        adminModule.getTicketManager().registerTicketType("suspicion", (ticket, subject) -> Component
                .text("Player ")
                .append(subject.displayName())
                .append(Component.text(" - Suspicious activity"))
        );
        Bukkit.getScheduler().scheduleSyncRepeatingTask(adminModule.getPlugin(), this::checkAllPlayers, 1200, 1200);
    }

    private void checkAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getStatistic(Statistic.PLAY_ONE_MINUTE) < TIME_PLAYED_THRESHOLD && !player.getPersistentDataContainer().has(pdcKey)) {
                if (isSuspicious(player)) {
                    if (adminModule.getPunishmentManager().getPunishments(player.getUniqueId()).getRestrictionPunishment() == null) {
                        PlayerIdentity playerIdentity = adminModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(player);
                        adminModule.getPunishmentManager().getRestrictionEnforcer().restrict(playerIdentity, Bukkit.getConsoleSender(), "Suspicious activity");
                    }
                    adminModule.getTicketManager().createSystemTicket(
                            player.getLocation(),
                            player.getUniqueId(),
                            "suspicion",
                            "Player " + player.getName() + " has suspicious activity that could indicate they are breaking the rules. Please investigate.\n\n" +
                                    getSuspicionSummary(player)
                    );
                    player.getPersistentDataContainer().set(pdcKey, PersistentDataType.BYTE, (byte) 1);

                }
            }
        }
    }

    private String getSuspicionSummary(Player player) {
        StringBuilder summary = new StringBuilder();
        for (ActivityMonitor monitor : monitors) {
            if (!summary.toString().equals("")) {
                summary.append("\n");
            }
            summary.append(monitor.getClass().getSimpleName()).append(": ").append(String.format("%.1f", monitor.getSuspicionLevel(player))).append(" / 2.0");
        }
        return summary.toString();
    }

    public boolean isSuspicious(Player player) {
        return monitors.stream().mapToDouble(monitor -> monitor.getSuspicionLevel(player)).sum() >= SUSPICION_THRESHOLD;
    }

}
