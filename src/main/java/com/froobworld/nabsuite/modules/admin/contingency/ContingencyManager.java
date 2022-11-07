package com.froobworld.nabsuite.modules.admin.contingency;

import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class ContingencyManager implements Listener {
    private final String JOIN_FULL_PERMISSION = "nabsuite.joinfull";
    private final AdminModule adminModule;
    private boolean lockdown = false;

    public ContingencyManager(AdminModule adminModule) {
        this.adminModule = adminModule;
        Bukkit.getPluginManager().registerEvents(this, adminModule.getPlugin());
    }

    public void toggleLockdown() {
        this.lockdown = !lockdown;
    }

    public boolean isLockdown() {
        return lockdown;
    }

    @EventHandler
    private void onPlayerLogin(PlayerLoginEvent event) {
        if (lockdown) {
            BasicsModule basicsModule = adminModule.getPlugin().getModule(BasicsModule.class);
            boolean allowed = event.getPlayer().hasPlayedBefore();
            if (basicsModule != null) {
                allowed = allowed || basicsModule.getPlayerDataManager().getPlayerData(event.getPlayer().getUniqueId()) != null;
            }
            if (!allowed) {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Component.text("The server is currently unable to accept new players, please try again later."));
            }
        }

        if (event.getResult() == PlayerLoginEvent.Result.KICK_FULL) {
            if (event.getPlayer().hasPermission(JOIN_FULL_PERMISSION)) {
                event.allow();
            }
        }
    }

}
