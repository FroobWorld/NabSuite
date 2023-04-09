package com.froobworld.nabsuite.modules.mechs.end;

import com.froobworld.nabsuite.modules.mechs.MechsModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class EndManager {
    private static final int REMINDER_DISTANCE = 1500;
    private static final int REMINDER_PERIOD = 1200 * 15; // 15 minutes
    private static final int INITIAL_DELAY = REMINDER_PERIOD + 1200;
    private final MechsModule mechsModule;

    public EndManager(MechsModule mechsModule) {
        this.mechsModule = mechsModule;
        mechsModule.getPlugin().getHookManager().getSchedulerHook().runRepeatingTask(this::sendReminders, REMINDER_PERIOD, INITIAL_DELAY);
    }

    private void sendReminders() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            mechsModule.getPlugin().getHookManager().getSchedulerHook().runEntityTaskAsap(() -> {
                if (player.getWorld().getEnvironment() == World.Environment.THE_END) {
                    if (Math.max(Math.abs(player.getLocation().getBlockX()), Math.abs(player.getLocation().getBlockZ())) > REMINDER_DISTANCE) {
                        player.sendMessage(Component.text("Please note: the area of the End you are in may be reset without notice.", NamedTextColor.RED));
                        player.sendMessage(Component.text("Do not place anything you are not willing to lose.", NamedTextColor.RED));
                    }
                }
            }, null, player);
        }
    }

}
