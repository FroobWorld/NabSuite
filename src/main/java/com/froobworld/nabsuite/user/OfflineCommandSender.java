package com.froobworld.nabsuite.user;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.froobworld.nabsuite.NabSuite;
import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Abstract CommandSender representing a player that may not be online.
 * Should not be initialized on main thread since a blocking LuckPerms database lookup may be required
 */
abstract public class OfflineCommandSender extends CommandSender.Spigot implements ConsoleCommandSender, OfflinePlayer {

    final PlayerIdentity player;
    final OfflinePlayer offlinePlayer;
    final CachedPermissionData permissionData;

    public OfflineCommandSender(NabSuite plugin, PlayerIdentity player) {
        if (Bukkit.isPrimaryThread()) {
            throw new RuntimeException("OfflineCommandSender may perform blocking operations and should not be initialized on main thread");
        }
        this.player = player;
        this.offlinePlayer = player.asOfflinePlayer();
        LuckPerms luckPerms = plugin.getHookManager().getLuckPermsHook().getLuckPerms();
        if (luckPerms != null) {
            User user = luckPerms.getUserManager().getUser(player.getUuid());
            if (user == null) {
                user = luckPerms.getUserManager().loadUser(player.getUuid()).join();
            }
            if (user != null) {
                this.permissionData = user.getCachedData().getPermissionData();
            } else {
                this.permissionData = null;
            }
        } else {
            this.permissionData = null;
        }
    }

    @Override
    abstract public void sendMessage(@NotNull String message);

    @Override
    public void sendMessage(@NotNull String... strings) {
        sendMessage(String.join("", strings));
    }

    @Override
    public void sendMessage(@Nullable UUID uuid, @NotNull String s) {
        sendMessage(s);
    }

    @Override
    public void sendMessage(@Nullable UUID uuid, @NotNull String... strings) {
        sendMessage(strings);
    }

    @Override
    abstract public void sendRawMessage(@NotNull String s);

    @Override
    public void sendRawMessage(@Nullable UUID uuid, @NotNull String s) {
        sendRawMessage(s);
    }

    @Override
    public void sendMessage(@NotNull BaseComponent component) {
        sendMessage(component.toLegacyText());
    }

    @Override
    public void sendMessage(@NotNull BaseComponent... components) {
        sendMessage(BaseComponent.toLegacyText(components));
    }

    @Override
    public void sendMessage(@Nullable UUID sender, @NotNull BaseComponent component) {
        sendMessage(component);
    }

    @Override
    public void sendMessage(@Nullable UUID sender, @NotNull BaseComponent... components) {
        sendMessage(components);
    }

    @Override
    public @NotNull Server getServer() {
        return Bukkit.getServer();
    }

    @Override
    public boolean isOnline() {
        return offlinePlayer.isOnline();
    }

    @Override
    public boolean isConnected() {
        return offlinePlayer.isConnected();
    }

    @Override
    public @NotNull String getName() {
        return player.getLastName();
    }

    @Override
    public @NotNull UUID getUniqueId() {
        return player.getUuid();
    }

    @Override
    public @NotNull PlayerProfile getPlayerProfile() {
        return offlinePlayer.getPlayerProfile();
    }

    @Override
    public boolean isBanned() {
        return offlinePlayer.isBanned();
    }

    @Override
    public <E extends BanEntry<? super PlayerProfile>> @Nullable E ban(@Nullable String s, @Nullable Date date, @Nullable String s1) {
        return offlinePlayer.ban(s, date, s1);
    }

    @Override
    public <E extends BanEntry<? super PlayerProfile>> @Nullable E ban(@Nullable String s, @Nullable Instant instant, @Nullable String s1) {
        return offlinePlayer.ban(s, instant, s1);
    }

    @Override
    public <E extends BanEntry<? super PlayerProfile>> @Nullable E ban(@Nullable String s, @Nullable Duration duration, @Nullable String s1) {
        return offlinePlayer.ban(s, duration, s1);
    }

    @Override
    public boolean isWhitelisted() {
        return offlinePlayer.isWhitelisted();
    }

    @Override
    public void setWhitelisted(boolean b) {
        offlinePlayer.setWhitelisted(b);
    }

    @Override
    public @Nullable Player getPlayer() {
        return offlinePlayer.getPlayer();
    }

    @Override
    public long getFirstPlayed() {
        return offlinePlayer.getFirstPlayed();
    }

    @Override
    public long getLastPlayed() {
        return offlinePlayer.getLastPlayed();
    }

    @Override
    public boolean hasPlayedBefore() {
        return offlinePlayer.hasPlayedBefore();
    }

    @Override
    public @Nullable Location getBedSpawnLocation() {
        return offlinePlayer.getBedSpawnLocation();
    }

    @Override
    public long getLastLogin() {
        return offlinePlayer.getLastLogin();
    }

    @Override
    public long getLastSeen() {
        return offlinePlayer.getLastSeen();
    }

    @Override
    public @Nullable Location getRespawnLocation() {
        return offlinePlayer.getRespawnLocation();
    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic) throws IllegalArgumentException {
        offlinePlayer.incrementStatistic(statistic);
    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic) throws IllegalArgumentException {
        offlinePlayer.decrementStatistic(statistic);
    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, int i) throws IllegalArgumentException {
        offlinePlayer.incrementStatistic(statistic, i);
    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, int i) throws IllegalArgumentException {
        offlinePlayer.decrementStatistic(statistic, i);
    }

    @Override
    public void setStatistic(@NotNull Statistic statistic, int i) throws IllegalArgumentException {
        offlinePlayer.setStatistic(statistic, i);
    }

    @Override
    public int getStatistic(@NotNull Statistic statistic) throws IllegalArgumentException {
        return offlinePlayer.getStatistic(statistic);
    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull Material material) throws IllegalArgumentException {
        offlinePlayer.incrementStatistic(statistic, material);
    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull Material material) throws IllegalArgumentException {
        offlinePlayer.decrementStatistic(statistic, material);
    }

    @Override
    public int getStatistic(@NotNull Statistic statistic, @NotNull Material material) throws IllegalArgumentException {
        return offlinePlayer.getStatistic(statistic, material);
    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull Material material, int i) throws IllegalArgumentException {
        offlinePlayer.incrementStatistic(statistic, material, i);
    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull Material material, int i) throws IllegalArgumentException {
        offlinePlayer.decrementStatistic(statistic, material, i);
    }

    @Override
    public void setStatistic(@NotNull Statistic statistic, @NotNull Material material, int i) throws IllegalArgumentException {
        offlinePlayer.setStatistic(statistic, material, i);
    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType) throws IllegalArgumentException {
        offlinePlayer.incrementStatistic(statistic, entityType);
    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType) throws IllegalArgumentException {
        offlinePlayer.decrementStatistic(statistic, entityType);
    }

    @Override
    public int getStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType) throws IllegalArgumentException {
        return offlinePlayer.getStatistic(statistic, entityType);
    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int i) throws IllegalArgumentException {
        offlinePlayer.incrementStatistic(statistic, entityType, i);
    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int i) {
        offlinePlayer.decrementStatistic(statistic, entityType, i);
    }

    @Override
    public void setStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int i) {
        offlinePlayer.setStatistic(statistic, entityType, i);
    }

    @Override
    public @Nullable Location getLastDeathLocation() {
        return offlinePlayer.getLastDeathLocation();
    }

    @Override
    public @Nullable Location getLocation() {
        return offlinePlayer.getLocation();
    }

    @Override
    public PersistentDataContainerView getPersistentDataContainer() {
        return offlinePlayer.getPersistentDataContainer();
    }

    @Override
    public @NotNull Spigot spigot() {
        return this;
    }

    @Override
    public @NotNull Component name() {
        return Component.text(player.getLastName());
    }

    @Override
    public boolean isPermissionSet(@NotNull String s) {
        return permissionData != null && permissionData.queryPermission(s).node() != null;
    }

    @Override
    public boolean isPermissionSet(@NotNull Permission permission) {
        return isPermissionSet(permission.getName());
    }

    @Override
    public boolean hasPermission(@NotNull String s) {
        return permissionData != null && permissionData.checkPermission(s).asBoolean();
    }

    @Override
    public boolean hasPermission(@NotNull Permission permission) {
        return hasPermission(permission.getName());
    }

    @Override
    public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String s, boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String s, boolean b, int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAttachment(@NotNull PermissionAttachment permissionAttachment) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recalculatePermissions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Set<PermissionAttachmentInfo> getEffectivePermissions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isOp() {
        return offlinePlayer.isOp();
    }

    @Override
    public void setOp(boolean b) {
        offlinePlayer.setOp(b);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return offlinePlayer.serialize();
    }


    @Override
    public boolean isConversing() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void acceptConversationInput(@NotNull String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean beginConversation(@NotNull Conversation conversation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void abandonConversation(@NotNull Conversation conversation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void abandonConversation(@NotNull Conversation conversation, @NotNull ConversationAbandonedEvent conversationAbandonedEvent) {
        throw new UnsupportedOperationException();
    }


}
