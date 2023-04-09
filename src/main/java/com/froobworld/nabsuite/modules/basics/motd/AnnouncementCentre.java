package com.froobworld.nabsuite.modules.basics.motd;

import com.froobworld.nabsuite.modules.basics.BasicsModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class AnnouncementCentre {
    private int listPosition = 0;

    public AnnouncementCentre(BasicsModule basicsModule) {
        long frequency = basicsModule.getConfig().messages.announcements.frequency.get();
        if (frequency <= 0) {
            return;
        }
        List<String> messages = basicsModule.getConfig().messages.announcements.messages.get();
        if (messages.isEmpty()) {
            return;
        }
        basicsModule.getPlugin().getHookManager().getSchedulerHook().runRepeatingTask(() -> {
            listPosition = listPosition % messages.size();
            Component message = MiniMessage.miniMessage().deserialize(messages.get(listPosition));
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(message);
            }
            listPosition++;
        }, frequency, frequency);
    }

}
