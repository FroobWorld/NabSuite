package com.froobworld.nabsuite.modules.admin.deputy;

import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.player.PlayerData;
import com.froobworld.nabsuite.util.DurationDisplayer;
import com.google.common.collect.Sets;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.Result;
import net.luckperms.api.event.node.NodeMutateEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.matcher.NodeMatcher;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.util.Tristate;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Stream;

public class DeputyManager {

    public static final String LIST_DEPUTY_PREFIX = "nabsuite.deputy.list.";
    public static final String MANAGE_DEPUTY_PREFIX = "nabsuite.deputy.manage.";

    private final BasicsModule basicsModule;
    private final AdminModule adminModule;
    private final LuckPerms luckPerms;

    private List<DeputyPlayer> deputies;
    private final List<DeputyLevel> levels;
    private boolean updatePending = false;

    public DeputyManager(AdminModule adminModule) {
        this.adminModule = adminModule;
        this.basicsModule = adminModule.getPlugin().getModule(BasicsModule.class);
        deputies = Collections.emptyList();
        levels = adminModule.getAdminConfig().deputyLevels.get().stream()
                .map(level -> new DeputyLevel(level, adminModule.getAdminConfig().deputySettings.of(level)))
                .toList();
        luckPerms = basicsModule.getPlugin().getHookManager().getLuckPermsHook().getLuckPerms();
        if (luckPerms != null) {
            luckPerms.getEventBus().subscribe(NodeMutateEvent.class, event -> this.scheduleUpdate());
            luckPerms.getEventBus().subscribe(NodeRemoveEvent.class, this::onNodeRemoved);
            scheduleUpdate();
            Bukkit.getScheduler().scheduleSyncRepeatingTask(adminModule.getPlugin(), this::runExpiryCheckTask, 200, 12000);
        }
    }

    private void sendDeputyExpiredNotification(DeputyPlayer deputyPlayer) {
        adminModule.getDiscordStaffLog().sendDeputyChangeNotification(Bukkit.getConsoleSender(), deputyPlayer, null);
        basicsModule.getMailCentre().sendSystemMail(deputyPlayer.getUuid(), "Your deputation as a " + deputyPlayer.getDeputyLevel().getName() + " deputy has expired.");
    }

    private void sendDeputyExpiryWarning(DeputyPlayer deputyPlayer) {
        // Round up to nearest hour
        long expiryTime = Math.ceilDiv(deputyPlayer.getExpiry() - System.currentTimeMillis(), 3600000) * 3600000;
        String duration = DurationDisplayer.asDurationString(expiryTime);
        adminModule.getTicketManager().createSystemTicket(
                basicsModule.getSpawnManager().getSpawnLocation(),
                "A deputation for player " + deputyPlayer.getPlayerIdentity().getLastName() + " (" + deputyPlayer.getDeputyLevel().getName() + " deputy) expires in less than " + duration + ". Please determine if the deputation should be renewed, if another deputy should be appointed or if no action is needed."
        );
        basicsModule.getMailCentre().sendSystemMail(deputyPlayer.getUuid(), "Your deputation as a " + deputyPlayer.getDeputyLevel().getName() + " deputy will expire in less than " + duration + ".");
    }

    public List<DeputyLevel> getDeputyLevels() {
        return levels;
    }

    private DeputyPlayer deputyFromNode(UUID uuid, Node n) {
        if (!(n instanceof InheritanceNode node)) {
            return null;
        }
        for (DeputyLevel deputyLevel: getDeputyLevels()) {
            if (!deputyLevel.getDeputyGroup().equalsIgnoreCase(node.getGroupName())) {
                continue;
            }
            PlayerIdentity playerIdentity = adminModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(uuid);
            if (playerIdentity == null) {
                return null;
            }
            return new DeputyPlayer(deputyLevel, playerIdentity, node.getExpiry() == null ? 0 : node.getExpiry().toEpochMilli());
        }
        return null;
    }

    private void onNodeRemoved(NodeRemoveEvent event) {
        if (event.getTarget() instanceof User user && event.getNode() instanceof InheritanceNode node) {
            DeputyPlayer deputyPlayer = deputyFromNode(user.getUniqueId(), node);
            if (deputyPlayer != null && node.hasExpired()) {
                sendDeputyExpiredNotification(deputyPlayer);
            }
        }
    }

    private void scheduleUpdate() {
        Bukkit.getScheduler().runTaskLater(adminModule.getPlugin(), () -> {
            if (!updatePending) {
                updatePending = true;
                this.updateDeputies()
                        .thenCompose(v -> this.updateCandidates())
                        .thenRunAsync(
                                () -> updatePending = false,
                                Bukkit.getScheduler().getMainThreadExecutor(adminModule.getPlugin())
                        );
            }
        }, 20L);
    }

    private CompletableFuture<Void> updateDeputies() {
        List<CompletableFuture<Stream<DeputyPlayer>>> futures = getDeputyLevels().stream().map(deputyLevel -> {
                if (deputyLevel.getDeputyGroup().isEmpty()) {
                    return null;
                }
                return luckPerms.getUserManager()
                        .searchAll(NodeMatcher.key(InheritanceNode.builder(deputyLevel.getDeputyGroup()).build()))
                        .thenApply(users -> users.entrySet().stream().map(entry -> {
                            long expiry = 0;
                            Iterator<InheritanceNode> it = entry.getValue().iterator();
                            if (it.hasNext()) {
                                InheritanceNode node = it.next();
                                if (node != null && node.getExpiry() != null) {
                                    expiry = node.getExpiry().toEpochMilli();
                                }
                            }
                            PlayerIdentity playerIdentity = adminModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(entry.getKey());
                            if (playerIdentity != null) {
                                return new DeputyPlayer(deputyLevel, playerIdentity, expiry);
                            }
                            return null;
                        }));
        }).toList();
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRunAsync(
                () -> deputies = futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .flatMap(stream -> stream)
                        .filter(Objects::nonNull)
                        .toList(),
                Bukkit.getScheduler().getMainThreadExecutor(adminModule.getPlugin())
        );
    }

    private CompletableFuture<Void> updateCandidates() {
        if (getDeputyLevels().isEmpty()) {
            return CompletableFuture.completedFuture((Void) null);
        }
        return CompletableFuture.allOf(getDeputyLevels().stream().map(deputyLevel -> {
            if (!deputyLevel.getCandidateGroups().isEmpty()) {
                List<CompletableFuture<Set<UUID>>> futures = deputyLevel.getCandidateGroups().stream()
                        .map(groupName -> basicsModule.getGroupManager().getUsersInGroup(groupName,user -> user.getPrimaryGroup().equalsIgnoreCase(groupName)))
                        .toList();

                return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                        .thenRunAsync(() -> deputyLevel.setCandidates(futures.stream()
                                        .map(CompletableFuture::join)
                                        .reduce(Sets::union)
                                        .orElse(Collections.emptySet())),
                                Bukkit.getScheduler().getMainThreadExecutor(adminModule.getPlugin())
                        );
            }
            return CompletableFuture.completedFuture((Void) null);
        }).toArray(CompletableFuture[]::new));
    }

    private void runExpiryCheckTask() {
        getDeputies().stream()
                .filter(deputyPlayer -> deputyPlayer.getExpiry() > 0 &&
                        deputyPlayer.getDeputyLevel().getExpiryNotificationTime() > 0 &&
                        deputyPlayer.getExpiry() < (System.currentTimeMillis() + deputyPlayer.getDeputyLevel().getExpiryNotificationTime()))
                .forEach(deputyPlayer -> luckPerms.getUserManager().loadUser(deputyPlayer.getUuid())
                        .thenAcceptAsync(user -> {
                            PlayerData playerData = basicsModule.getPlayerDataManager().getPlayerData(user.getUniqueId());
                            if (playerData != null && playerData.getLastDeputyExpireNotification() < deputyPlayer.getExpiry()) {
                                playerData.setLastDeputyExpireNotification(deputyPlayer.getExpiry());
                                sendDeputyExpiryWarning(deputyPlayer);
                            }
                        }, Bukkit.getScheduler().getMainThreadExecutor(adminModule.getPlugin())
                ));
    }

    public List<DeputyPlayer> getDeputies() {
        return deputies;
    }

    public DeputyPlayer getDeputy(UUID uuid) {
        for (DeputyPlayer deputyPlayer : deputies) {
            if (deputyPlayer.getUuid().equals(uuid)) {
                return deputyPlayer;
            }
        }
        return null;
    }

    public CompletableFuture<DeputyPlayer> addDeputy(DeputyLevel deputyLevel, UUID uuid, long duration) {
        if (luckPerms == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("LuckPerms is not loaded"));
        }
        return luckPerms.getUserManager().loadUser(uuid).thenCompose(user -> {
            if (user.getCachedData().getPermissionData().queryPermission("group." + deputyLevel.getDeputyGroup()).node() instanceof InheritanceNode oldNode) {
                user.data().remove(oldNode);
            }
            InheritanceNode node = InheritanceNode.builder(deputyLevel.getDeputyGroup())
                    .expiry(duration, TimeUnit.MILLISECONDS)
                    .build();
            user.data().add(node);
            return luckPerms.getUserManager().saveUser(user).thenApply(v -> new DeputyPlayer(
                    deputyLevel,
                    adminModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(uuid),
                    node.getExpiry() != null ? node.getExpiry().toEpochMilli() : 0
            ));
        });
    }

    public CompletableFuture<Void> removeDeputy(DeputyLevel deputyLevel, UUID uuid) {
        if (luckPerms == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("LuckPerms is not loaded"));
        }
        return luckPerms.getUserManager().modifyUser(uuid, user -> {
            Result<Tristate, Node> result = user.getCachedData().getPermissionData().queryPermission("group." + deputyLevel.getDeputyGroup());
            if (result.result().equals(Tristate.TRUE) && result.node() instanceof InheritanceNode node) {
                user.data().remove(node);
            }
        });
    }

}
