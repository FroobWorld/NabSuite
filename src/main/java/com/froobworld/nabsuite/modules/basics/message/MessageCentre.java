package com.froobworld.nabsuite.modules.basics.message;

import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MessageCentre {
    private static final Component CONSOLE_NAME = Component.text("Console").color(NamedTextColor.RED);
    private static final UUID CONSOLE_UUID = new UUID(0,0);
    private final BasicsModule basicsModule;
    private final Map<UUID, UUID> lastMessenger = new ConcurrentHashMap<>();

    public MessageCentre(BasicsModule basicsModule) {
        this.basicsModule = basicsModule;
    }

    public void sendMessage(CommandSender from, CommandSender to, String message) {
        Component fromName;
        Component toName;
        if (from instanceof Player) {
            fromName = ((Player) from).displayName();
        } else {
            fromName = CONSOLE_NAME;
        }
        if (to instanceof Player) {
            toName = ((Player) to).displayName();
        } else {
            toName = CONSOLE_NAME;
        }
        UUID fromUuid = from instanceof Player ? ((Player) from).getUniqueId() : CONSOLE_UUID;
        UUID toUuid = to instanceof Player ? ((Player) to).getUniqueId() : CONSOLE_UUID;
        if (from instanceof Player) {
            if (!basicsModule.getPlayerDataManager().getFriendManager().areFriends(fromUuid, toUuid) && basicsModule.getPlugin().getModule(AdminModule.class).getPunishmentManager().getMuteEnforcer().testMute((Player) from, false)) {
                from.sendMessage(
                        Component.text("While muted you may only message players on your /friend list.").color(NamedTextColor.RED)
                );
                return;
            }
        }
        if (to instanceof Player && from instanceof Player) {
            if (basicsModule.getPlayerDataManager().getIgnoreManager().isIgnoring(toUuid, fromUuid)) {
                from.sendMessage(
                        Component.text("You cannot message someone who is ignoring you.").color(NamedTextColor.RED)
                );
                return;
            }
        }

        from.sendMessage(
                Component.text("[Me -> ")
                .append(toName)
                .append(Component.text("] "))
                .append(Component.text(message))
        );
        to.sendMessage(
                Component.text("[")
                .append(fromName)
                .append(Component.text(" -> Me] "))
                .append(Component.text(message))
        );
        lastMessenger.put(toUuid, fromUuid);
    }

    public void reply(CommandSender from, String message) {
        UUID toUuid = lastMessenger.get(from instanceof Player ? ((Player) from).getUniqueId() : CONSOLE_UUID);
        CommandSender to = toUuid == null ? null : (toUuid == CONSOLE_UUID ? Bukkit.getConsoleSender() : Bukkit.getPlayer(toUuid));
        if (to == null) {
            from.sendMessage(
                    Component.text("There is no one to reply to.").color(NamedTextColor.RED)
            );
            return;
        }
        sendMessage(from, to, message);
    }

}
