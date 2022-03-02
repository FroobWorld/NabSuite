package com.froobworld.nabsuite.modules.mechs.nabdimension;

import com.froobworld.nabsuite.modules.mechs.MechsModule;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public class NabModeManager {
    private final NamespacedKey nabModePdcKey;
    private final String nabModePerm = "nabsuite.nabmode";

    public NabModeManager(MechsModule mechsModule) {
        this.nabModePdcKey = NamespacedKey.fromString("nab-mode", mechsModule.getPlugin());
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

}
