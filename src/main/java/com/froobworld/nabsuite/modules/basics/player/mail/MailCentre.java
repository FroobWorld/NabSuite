package com.froobworld.nabsuite.modules.basics.player.mail;

import com.froobworld.nabsuite.data.DataLoader;
import com.froobworld.nabsuite.data.DataSaver;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.util.ConsoleUtils;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class MailCentre implements Listener {
    private static final Pattern fileNamePattern = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.json$");
    private static final long reminderFrequency = TimeUnit.MINUTES.toMillis(30);
    private final BasicsModule basicsModule;
    protected final DataSaver mailBoxSaver;
    private final BiMap<UUID, MailBox> mailBoxMap = HashBiMap.create();
    private final File directory;
    private final Map<Player, Long> lastReminderMap = new WeakHashMap<>();

    public MailCentre(BasicsModule basicsModule) {
        this.basicsModule = basicsModule;
        directory = new File(basicsModule.getDataFolder(), "mail/");
        mailBoxSaver = new DataSaver(basicsModule.getPlugin(), 1200);
        mailBoxMap.putAll(DataLoader.loadAll(
                directory,
                fileName -> fileNamePattern.matcher(fileName.toLowerCase()).matches(),
                bytes -> MailBox.fromJsonString(this, new String(bytes)),
                (fileName, mailBox) -> mailBox.getUuid()
        ));
        mailBoxSaver.start();
        mailBoxSaver.addDataType(MailBox.class, mailBox -> mailBox.toJsonString().getBytes(), mailBox -> new File(directory, mailBox.getUuid().toString() + ".json"));
        Bukkit.getPluginManager().registerEvents(this, basicsModule.getPlugin());
        Bukkit.getScheduler().scheduleSyncRepeatingTask(basicsModule.getPlugin(), this::doReminderCycle, 0, 300);
    }

    public void shutdown() {
        mailBoxSaver.stop();
    }

    public MailBox getMailBox(UUID player) {
        if (!mailBoxMap.containsKey(player)) {
            MailBox mailBox = new MailBox(this, player);
            mailBoxMap.put(player, mailBox);
            mailBoxSaver.scheduleSave(mailBox);
        }
        return mailBoxMap.get(player);
    }

    public Mail sendSystemMail(UUID recipient, String message) {
        return sendMail(recipient, ConsoleUtils.CONSOLE_UUID, message);
    }

    public Mail sendMail(UUID recipient, UUID sender, String message) {
        Mail mail = new Mail(System.currentTimeMillis(), sender, message);
        getMailBox(recipient).addMail(mail);
        Player player = Bukkit.getPlayer(recipient);
        if (player != null) {
            player.sendMessage(
                    Component.text("You have received some mail (/mail read).", NamedTextColor.YELLOW)
                            .clickEvent(ClickEvent.runCommand("/mail read"))
            );
            lastReminderMap.put(player, System.currentTimeMillis());
        }
        return mail;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerJoin(PlayerJoinEvent event) {
        if (getMailBox(event.getPlayer().getUniqueId()).hasUnread()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(basicsModule.getPlugin(), () -> {
                event.getPlayer().sendMessage(
                        Component.text("You've got mail (/mail read).").color(NamedTextColor.YELLOW)
                                .clickEvent(ClickEvent.runCommand("/mail read"))
                );
            }, 20);
            lastReminderMap.put(event.getPlayer(), System.currentTimeMillis());
        }
    }

    private void doReminderCycle() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (getMailBox(player.getUniqueId()).hasUnread()) {
                if (System.currentTimeMillis() - lastReminderMap.computeIfAbsent(player, p -> System.currentTimeMillis()) >= reminderFrequency) {
                    player.sendMessage(
                            Component.text("You have unread mail (/mail read).").color(NamedTextColor.YELLOW)
                                    .clickEvent(ClickEvent.runCommand("/mail read"))
                    );
                    lastReminderMap.put(player, System.currentTimeMillis());
                }
            }
        }
    }

}
