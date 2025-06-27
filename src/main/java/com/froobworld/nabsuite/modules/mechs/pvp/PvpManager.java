package com.froobworld.nabsuite.modules.mechs.pvp;

import com.froobworld.nabsuite.data.playervar.PlayerVars;
import com.froobworld.nabsuite.modules.mechs.MechsModule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PvpManager {
    private static final String PVP_ENABLED_KEY = "pvp-enabled";
    private final MechsModule mechsModule;

    public PvpManager(MechsModule mechsModule) {
        this.mechsModule = mechsModule;
        Bukkit.getPluginManager().registerEvents(new PvpEnforcer(this, mechsModule), mechsModule.getPlugin());
    }

    public boolean pvpEnabled(Player player) {
        PlayerVars playerVars = mechsModule.getPlugin().getPlayerVarsManager().getVars(player.getUniqueId());
        return playerVars.getOrDefault(PVP_ENABLED_KEY, boolean.class, false);
    }

    public void setPvpEnabled(Player player, boolean enabled) {
        PlayerVars playerVars = mechsModule.getPlugin().getPlayerVarsManager().getVars(player.getUniqueId());
        playerVars.put(PVP_ENABLED_KEY, enabled);
    }

}
