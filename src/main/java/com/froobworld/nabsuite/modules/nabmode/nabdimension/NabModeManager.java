package com.froobworld.nabsuite.modules.nabmode.nabdimension;

import com.froobworld.nabsuite.modules.nabmode.NabModeModule;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public class NabModeManager {
    private final NamespacedKey nabModePdcKey;
    private final String nabModePerm = "nabsuite.nabmode";
    private final NabDimensionManager nabDimensionManager;

    public NabModeManager(NabModeModule nabModeModule) {
        this.nabModePdcKey = NamespacedKey.fromString("nab-mode", nabModeModule.getPlugin());
        this.nabDimensionManager = new NabDimensionManager(nabModeModule);
    }

    public void setNabMode(Player player, boolean nabMode) {
        if (nabMode) {
            player.getPersistentDataContainer().set(nabModePdcKey, PersistentDataType.BYTE, (byte) 1);
        } else {
            player.getPersistentDataContainer().remove(nabModePdcKey);
        }
    }

    public boolean isNabMode(Player player) {
        return player.getPersistentDataContainer().has(nabModePdcKey) && player.hasPermission(nabModePerm);
    }

    public NabDimensionManager getNabDimensionManager() {
        return nabDimensionManager;
    }
}
