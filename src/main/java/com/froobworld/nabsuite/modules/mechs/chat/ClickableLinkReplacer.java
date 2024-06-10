package com.froobworld.nabsuite.modules.mechs.chat;

import com.froobworld.nabsuite.modules.mechs.MechsModule;
import com.froobworld.nabsuite.util.ComponentUtils;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ClickableLinkReplacer implements Listener {

    public ClickableLinkReplacer(MechsModule mechsModule) {
        Bukkit.getPluginManager().registerEvents(this, mechsModule.getPlugin());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onChat(AsyncChatEvent event) {
        event.message(ComponentUtils.clickableUrls(event.message()));
    }

}
