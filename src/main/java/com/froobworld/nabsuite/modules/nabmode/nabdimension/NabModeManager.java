package com.froobworld.nabsuite.modules.nabmode.nabdimension;

import com.froobworld.nabsuite.data.playervar.PlayerVars;
import com.froobworld.nabsuite.modules.nabmode.NabModeModule;
import org.bukkit.entity.Player;

public class NabModeManager {
    private static final String NAB_MODE_KEY = "nab-mode";
    private static final String nabModePerm = "nabsuite.nabmode";
    private final NabModeModule nabModeModule;
    private final NabDimensionManager nabDimensionManager;

    public NabModeManager(NabModeModule nabModeModule) {
        this.nabModeModule = nabModeModule;
        this.nabDimensionManager = new NabDimensionManager(nabModeModule);
    }

    public void setNabMode(Player player, boolean nabMode) {
        PlayerVars playerVars = nabModeModule.getPlugin().getPlayerVarsManager().getVars(player.getUniqueId());
        playerVars.put(NAB_MODE_KEY, nabMode);
    }

    public boolean isNabMode(Player player) {
        PlayerVars playerVars = nabModeModule.getPlugin().getPlayerVarsManager().getVars(player.getUniqueId());
        return playerVars.getOrDefault(NAB_MODE_KEY, boolean.class, false) && player.hasPermission(nabModePerm);
    }

    public NabDimensionManager getNabDimensionManager() {
        return nabDimensionManager;
    }
}
