package com.froobworld.nabsuite.modules.protect.area.visualiser;

import com.froobworld.nabsuite.modules.protect.ProtectModule;
import com.froobworld.nabsuite.modules.protect.area.Area;
import com.google.common.collect.MapMaker;
import org.bukkit.entity.Player;

import java.util.Map;

public class AreaVisualiser {
    private final ProtectModule protectModule;
    private final Map<Player, AreaVisualisation> activeVisualisations = new MapMaker().weakKeys().makeMap();

    public AreaVisualiser(ProtectModule protectModule) {
        this.protectModule = protectModule;
        protectModule.getPlugin().getHookManager().getSchedulerHook().runRepeatingTaskAsync(this::sendVisualisations, 1, 1);
    }

    public Area getVisualisedArea(Player player) {
        AreaVisualisation areaVisualisation = activeVisualisations.get(player);
        return areaVisualisation == null ? null : areaVisualisation.getArea();
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
