package com.froobworld.nabsuite.modules.basics.group;

import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public class Group {
    private UUID owner;
    private boolean inviteOnly;
    private Set<UUID> whiteList;

    public UUID getOwner() {
        return owner;
    }

    public boolean isPrivate() {
        return inviteOnly;
    }

    public boolean isAllowed(UUID uuid) {
        return whiteList.contains(uuid);
    }

    public boolean isAllowed(Player player) {
        return isAllowed(player.getUniqueId());
    }

}
