package com.froobworld.nabsuite.hook;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;

public class DynmapHook {
    private final DynmapAPI dynmapAPI;

    public DynmapHook() {
        Plugin dynmap = Bukkit.getPluginManager().getPlugin("dynmap");
        if (dynmap != null && dynmap.isEnabled()) {
            dynmapAPI = (DynmapAPI) dynmap;
        } else {
            dynmapAPI = null;
        }
    }

    public DynmapAPI getDynmapAPI() {
        return dynmapAPI;
    }

}
