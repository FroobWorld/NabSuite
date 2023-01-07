package com.froobworld.nabsuite.modules.protect.area.visualiser;

import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.froobworld.nabsuite.modules.protect.area.Area;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.WeakHashMap;

public class AreaVisualiser {
    private final ProtectModule protectModule;
    private final Map<Player, AreaVisualisation> activeVisualisations = new WeakHashMap<>();

    public AreaVisualiser(ProtectModule protectModule) {
        this.protectModule = protectModule;
        Bukkit.getScheduler().runTaskTimerAsynchronously(protectModule.getPlugin(), this::sendVisualisations, 1, 1);
    }

    public Area getVisualisedArea(Player player) {
        if (!activeVisualisations.containsKey(player)) {
            return null;
        }
        return activeVisualisations.get(player).getArea();
    }

    public void visualiseArea(Player player, Area area) {
        activeVisualisations.put(player, new AreaVisualisation(area));
    }

    public void stopVisualisation(Player player) {
        activeVisualisations.remove(player);
    }

    private void sendVisualisations() {
        for (Map.Entry<Player, AreaVisualisation> entry : activeVisualisations.entrySet()) {
            entry.getValue().sendToPlayer(entry.getKey());
        }
    }

}
