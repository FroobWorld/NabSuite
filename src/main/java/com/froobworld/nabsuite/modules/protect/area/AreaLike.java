package com.froobworld.nabsuite.modules.protect.area;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface AreaLike {

    boolean hasFlag(String flag);

    boolean containsLocation(Location location);

    boolean hasUserRights(Player player);

}
