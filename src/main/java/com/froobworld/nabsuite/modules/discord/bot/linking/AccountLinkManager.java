package com.froobworld.nabsuite.modules.discord.bot.linking;

import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.discord.DiscordModule;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.api.entities.User;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class AccountLinkManager {
    private final DiscordModule discordModule;
    private final LinkedAccountDataManager linkedAccountDataManager;
    private final AccountLinkBridge accountLinkBridge;
    private final Cache<UUID, Integer> pendingLinks = CacheBuilder.newBuilder().expireAfterWrite(300, TimeUnit.SECONDS).build();
    private final Random random = new Random();

    public AccountLinkManager(DiscordModule discordModule) {
        this.discordModule = discordModule;
        linkedAccountDataManager = new LinkedAccountDataManager(discordModule);
        accountLinkBridge = new AccountLinkBridge(discordModule, this);
    }

    public void shutdown() {
        linkedAccountDataManager.shutdown();
    }

    public List<LinkedAccountData> getAllLinkedAccounts() {
        return linkedAccountDataManager.getAllLinkedAccounts();
    }

    public PlayerIdentity getLinkedMinecraftAccount(User user) {
        LinkedAccountData linkedAccountData = linkedAccountDataManager.getLinkedAccountData(user.getId());
        if (linkedAccountData != null) {
            return discordModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(linkedAccountData.getMinecraftUuid());
        }
        return null;
    }

    public CompletableFuture<User> getLinkedDiscordUser(UUID uuid) {
        LinkedAccountData linkedAccountData = linkedAccountDataManager.getLinkedAccountData(uuid);
        if (linkedAccountData != null) {
            CompletableFuture<User> future = new CompletableFuture<>();
            discordModule.getDiscordBot().getJda().retrieveUserById(linkedAccountData.getDiscordId()).queue(future::complete, future::completeExceptionally);
            return future;
        }
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<User> getLinkedDiscordUser(Player player) {
        return getLinkedDiscordUser(player.getUniqueId());
    }

    public void setLinked(UUID uuid, User user) {
        linkedAccountDataManager.addLinkedAccount(uuid, user.getId());
        pendingLinks.invalidate(uuid);
    }

    public int initiateLink(Player player) {
        int nextInt = 1000 + random.nextInt(9000);
        if (pendingLinks.asMap().containsValue(nextInt)) {
            return initiateLink(player);
        }
        pendingLinks.put(player.getUniqueId(), nextInt);
        return nextInt;
    }

    public UUID getUuidForCode(int code) {
        for (Map.Entry<UUID, Integer> entry : pendingLinks.asMap().entrySet()) {
            if (entry.getValue() == code) {
                return entry.getKey();
            }
        }
        return null;
    }

}
