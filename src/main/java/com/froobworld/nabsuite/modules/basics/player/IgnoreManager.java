package com.froobworld.nabsuite.modules.basics.player;

import com.froobworld.nabsuite.modules.basics.BasicsModule;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class IgnoreManager implements Listener {
    private final BasicsModule basicsModule;

    public IgnoreManager(BasicsModule basicsModule) {
        this.basicsModule = basicsModule;
        Bukkit.getPluginManager().registerEvents(this, basicsModule.getPlugin());
    }

    public boolean isIgnoring(UUID ignorer, UUID ignoring) {
        PlayerData ignorerData = basicsModule.getPlayerDataManager().getPlayerData(ignorer);
        if (ignorerData != null) {
            return ignorerData.isIgnoring(ignoring);
        }
        return false;
    }

    public boolean isIgnoring(UUID ignorer, Player ignoring) {
        return isIgnoring(ignorer, ignoring.getUniqueId());
    }

    public boolean isIgnoring(Player ignorer, UUID ignoring) {
        return isIgnoring(ignorer.getUniqueId(), ignoring);
    }

    public boolean isIgnoring(Player ignorer, Player ignoring) {
        return isIgnoring(ignorer.getUniqueId(), ignoring.getUniqueId());
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        event.viewers().removeIf(audienceMember -> audienceMember instanceof Player && isIgnoring((Player) audienceMember, event.getPlayer()));
    }

}
