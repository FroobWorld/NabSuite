package com.froobworld.nabsuite.modules.admin.deputy;

import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class DeputyPlayer implements Comparable<DeputyPlayer> {

    final DeputyLevel deputyLevel;
    final PlayerIdentity playerIdentity;
    final long expiry;

    public DeputyPlayer(DeputyLevel deputyLevel, PlayerIdentity playerIdentity, long expiry) {
        this.deputyLevel = deputyLevel;
        this.playerIdentity = playerIdentity;
        this.expiry = expiry;
    }

    @Override
    public int compareTo(@NotNull DeputyPlayer deputyPlayer) {
        return expiry == 0 ? 1 : deputyPlayer.expiry == 0 ? -1 : Long.compare(expiry, deputyPlayer.expiry);
    }

    public DeputyLevel getDeputyLevel() {
        return deputyLevel;
    }

    public PlayerIdentity getPlayerIdentity() {
        return playerIdentity;
    }

    public long getExpiry() {
        return expiry;
    }

    public UUID getUuid() {
        return playerIdentity.getUuid();
    }

    public boolean checkManagePermission(Permissible sender) {
        return deputyLevel.checkManagePermission(sender);
    }

    public boolean checkListPermission(Permissible sender) {
        return deputyLevel.checkListPermission(sender);
    }
}
