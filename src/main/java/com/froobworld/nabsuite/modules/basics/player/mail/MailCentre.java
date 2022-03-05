package com.froobworld.nabsuite.modules.basics.player.mail;

import com.froobworld.nabsuite.data.DataLoader;
import com.froobworld.nabsuite.data.DataSaver;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.player.mail.MailBox;
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
import java.util.UUID;
import java.util.regex.Pattern;

public class MailCentre implements Listener {
    private static final Pattern fileNamePattern = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.json$");
    private final BasicsModule basicsModule;
    protected final DataSaver mailBoxSaver;
    private final BiMap<UUID, MailBox> mailBoxMap = HashBiMap.create();
    private final File directory;

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
        }
    }

}
