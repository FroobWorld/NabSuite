package com.froobworld.nabsuite.modules.mechs.holidayevent.aprilfools26;

import com.froobworld.nabsuite.modules.mechs.MechsModule;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.*;
import org.bukkit.scoreboard.Criteria;

import java.util.*;

public class AprilFools26Sidebar implements Listener {
    private final MechsModule mechsModule;

    private static final int UPDATE_TICKS = 2 * 20;  // 2 seconds
    private static final int MODE_TICKS = 200;    // 10 seconds per mode
    private static final int TOP_COUNT = 5;

    private enum DisplayMode {
        OVERALL("Top Overall:"),
        ONLINE("Top Online:");

        final String label;
        DisplayMode(String label) { this.label = label; }
    }

    private final AprilFools26Event event;
    private DisplayMode currentMode = DisplayMode.OVERALL;
    private int ticksSinceCycle = 0;

    public AprilFools26Sidebar(AprilFools26Event event, MechsModule mechsModule) {
        this.mechsModule = mechsModule;
        this.event = event;
        Bukkit.getPluginManager().registerEvents(this, mechsModule.getPlugin());
        Bukkit.getScheduler().scheduleSyncRepeatingTask(mechsModule.getPlugin(), this::tick, UPDATE_TICKS, UPDATE_TICKS);
    }

    private void tick() {
        ticksSinceCycle += UPDATE_TICKS;
        if (ticksSinceCycle >= MODE_TICKS) {
            ticksSinceCycle = 0;
            DisplayMode[] modes = DisplayMode.values();
            currentMode = modes[(currentMode.ordinal() + 1) % modes.length];
        }
        Bukkit.getOnlinePlayers().forEach(this::updateSidebar);
    }

    private void updateSidebar(Player player) {
        Scoreboard sb = player.getScoreboard();
        if (sb.equals(Bukkit.getScoreboardManager().getMainScoreboard())) {
            return;
        }

        Objective existing = sb.getObjective("af26");
        if (existing != null) {
            existing.unregister();
        }
        if (!mechsModule.getHolidayEventManager().isEnabled(player, event.getHolidayKey())) {
            return;
        }

        Objective obj = sb.registerNewObjective("af26", Criteria.DUMMY,
                Component.text("The Nab Wars").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);


        int slot = 0;

        if (currentMode == DisplayMode.ONLINE) {
            entry(obj, "disablehint", slot++,
                    Component.text("Disable: /toggleevent", NamedTextColor.YELLOW));
        } else {
            entry(obj, "pscore", slot++,
                    Component.text("Your score: " + event.getScore(player)).color(NamedTextColor.YELLOW));
        }
        entry(obj, "splast", slot++, Component.empty());

        Map<EntityType, Integer> killPoints = event.getKillPoints();
        List<Map.Entry<EntityType, Integer>> entries = new ArrayList<>(killPoints.entrySet());
        entries.sort(Comparator.comparingInt(Map.Entry::getValue));
        for (Map.Entry<EntityType, Integer> entry : entries) {
            entry(obj, "killp" + entry.getKey(), slot++,
                    Component.text("  " + entry.getKey().toString().toLowerCase() + " (" + entry.getValue() + " pts)").color(NamedTextColor.WHITE));
        }
        entry(obj, "killh", slot++,
                Component.text("Current targets: ")
                        .color(NamedTextColor.DARK_AQUA));

        entry(obj, "sp2", slot++, Component.empty());

        List<Component> topPlayers = getTopScorers(AprilFools26Event.TEAM_CHOF);
        for (int i = TOP_COUNT - 1; i >= 0; i--) {
            Component line = i < topPlayers.size() ? Component.text("  ").append(topPlayers.get(i)) : Component.text("  -");
            entry(obj, "top" + i, slot++, line);
        }
        entry(obj, "mode", slot++,
                Component.text(currentMode.label).color(NamedTextColor.WHITE));
//        entry(obj, "sp0", slot++, Component.empty());

        Component topTitle;
        Component bottomTitle;
        if (event.getScoreChof() > event.getScoreFrob()) {
            bottomTitle = Component.text("TEAM FROB  " + event.getScoreFrob())
                    .color(NamedTextColor.BLUE).decorate(TextDecoration.BOLD);
            topTitle = Component.text("TEAM CHOF  " + event.getScoreChof())
                    .color(NamedTextColor.RED).decorate(TextDecoration.BOLD);
        } else {
            topTitle = Component.text("TEAM FROB  " + event.getScoreFrob())
                    .color(NamedTextColor.BLUE).decorate(TextDecoration.BOLD);
            bottomTitle = Component.text("TEAM CHOF  " + event.getScoreChof())
                    .color(NamedTextColor.RED).decorate(TextDecoration.BOLD);
        }
        entry(obj, "bottomh", slot++, bottomTitle);
        entry(obj, "toph", slot++, topTitle);
    }

    private void entry(Objective obj, String key, int slot, Component display) {
        Score score = obj.getScore(key);
        score.setScore(slot);
        score.customName(display);
        score.numberFormat(NumberFormat.blank());
    }

    private List<Component> getTopScorers(String team) {
        return switch (currentMode) {
            case OVERALL -> getTopScorersOverall(TOP_COUNT);
            case ONLINE -> getTopScorersOnline(TOP_COUNT);
        };
    }

    private List<Component> getTopScorersOverall(int limit) {
        return mechsModule.getPlugin().getPlayerIdentityManager().getAllPlayerIdentities().stream()
                .sorted((p1, p2) -> event.getScore(p2.getUuid()) - event.getScore(p1.getUuid()))
                .map(player -> player.displayName().append(
                        Component.text(" (" + event.getScore(player.getUuid()) + ")").color(NamedTextColor.YELLOW)
                ))
                .toList();
    }

    private List<Component> getTopScorersOnline(int limit) {
        return Bukkit.getOnlinePlayers().stream()
                .sorted((p1, p2) -> event.getScore(p2) - event.getScore(p1))
                .map(player -> player.displayName().append(
                        Component.text(" (" + event.getScore(player) + ")").color(NamedTextColor.YELLOW)
                ))
                .limit(limit)
                .toList();
    }

    // HIGH priority so this runs after NameTagManager (NORMAL), reusing its custom scoreboard if present
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.getScoreboard().equals(Bukkit.getScoreboardManager().getMainScoreboard())) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
        updateSidebar(player);
    }
}
