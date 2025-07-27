package com.froobworld.nabsuite.modules.admin.deputy;

import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.data.playervar.PlayerVars;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.util.DurationDisplayer;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.Result;
import net.luckperms.api.event.node.NodeMutateEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.util.Tristate;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public class DeputyManager {

    public static final String LIST_DEPUTY_PREFIX = "nabsuite.deputy.list.";
    public static final String MANAGE_DEPUTY_PREFIX = "nabsuite.deputy.manage.";

    private final BasicsModule basicsModule;
    private final AdminModule adminModule;
    private final LuckPerms luckPerms;

    private final List<DeputyPlayer> deputies = new ArrayList<>();
    private final List<DeputyLevel> levels;

    public DeputyManager(AdminModule adminModule) {
        this.adminModule = adminModule;
        this.basicsModule = adminModule.getPlugin().getModule(BasicsModule.class);
        levels = adminModule.getAdminConfig().deputyLevels.get().stream()
                .map(level -> new DeputyLevel(level, adminModule.getAdminConfig().deputySettings.of(level)))
                .toList();
        luckPerms = basicsModule.getPlugin().getHookManager().getLuckPermsHook().getLuckPerms();
        if (luckPerms != null) {
            luckPerms.getEventBus().subscribe(NodeMutateEvent.class, this::onNodeMutate);
            luckPerms.getEventBus().subscribe(NodeRemoveEvent.class, this::onNodeRemoved);
            updateAllDeputies();
            Bukkit.getScheduler().scheduleSyncRepeatingTask(adminModule.getPlugin(), this::runExpiryCheckTask, 200, 600);
        }
        levels.forEach(level -> adminModule.getTicketManager().registerTicketType(
                "deputy-expiry-"+level.getName(),
                (ticket,subject) -> Component
                        .text("Player ")
                        .append(subject.displayName())
                        .append(Component.text(" - "+level.getName()+" deputy expiry"))
        ));
    }

    private void sendDeputyAddNotification(CommandSender sender, DeputyPlayer previous, DeputyPlayer current, long duration) {
        adminModule.getDiscordStaffLog().sendDeputyChangeNotification(sender == null ? Bukkit.getConsoleSender() : sender, previous, current);
        if (previous == null) {
            basicsModule.getMailCentre().sendSystemMail(current.getUuid(), "You have been appointed " + current.getDeputyLevel().getName() + " deputy for " +
                    DurationDisplayer.asDurationString(duration) + ". Please review the responsibilities and powers associated with this role on the wiki."
            );
        } else {
            basicsModule.getMailCentre().sendSystemMail(
                    current.getUuid(),
                    "Your appointment as " + current.getDeputyLevel().getName() + " deputy has been renewed and is valid for " + DurationDisplayer.asDurationString(duration) + "."
            );
        }
    }

    private void sendDeputyRemovedNotification(CommandSender sender, DeputyPlayer deputyPlayer, boolean expired) {
        adminModule.getDiscordStaffLog().sendDeputyChangeNotification(sender == null ? Bukkit.getConsoleSender() : sender, deputyPlayer, null);
        if (expired) {
            basicsModule.getMailCentre().sendSystemMail(deputyPlayer.getUuid(), "Your appointment as " + deputyPlayer.getDeputyLevel().getName() + " deputy has expired.");
        } else {
            basicsModule.getMailCentre().sendSystemMail(deputyPlayer.getUuid(), "Your appointment as " + deputyPlayer.getDeputyLevel().getName() + " deputy was revoked.");
        }
    }

    private void sendDeputyExpiryWarning(DeputyPlayer deputyPlayer) {
        // Round up to nearest hour
        long expiryTime = Math.ceilDiv(deputyPlayer.getExpiry() - System.currentTimeMillis(), 3600000) * 3600000;
        String duration = DurationDisplayer.asDurationString(expiryTime);
        adminModule.getTicketManager().createSystemTicket(
                deputyPlayer.getUuid(),
                "deputy-expiry-"+deputyPlayer.getDeputyLevel().getName(),
                "Appointment of " + deputyPlayer.getPlayerIdentity().getLastName() + " as " + deputyPlayer.getDeputyLevel().getName() + " deputy expires in less than " + duration + ". Please determine if it should be renewed, if another deputy should be appointed, or if no action is needed."
        );
        basicsModule.getMailCentre().sendSystemMail(deputyPlayer.getUuid(), "Your appointment as " + deputyPlayer.getDeputyLevel().getName() + " deputy will expire in less than " + duration + ".");
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
                sendDeputyRemovedNotification(null, deputyPlayer, true);
            }
        }
    }

    private void onNodeMutate(NodeMutateEvent event) {
        if (event.getTarget() instanceof User user) {
            updateDeputy(user, true);
        }
    }

    private void updateAllDeputies() {
        this.deputies.clear();
        for (User user : adminModule.getGroupManager().getUsers()) {
            updateDeputy(user, false);
        }
    }

    private void updateDeputy(User user, boolean removeExisting) {
        if (removeExisting) {
            // remove existing entries for player
            this.deputies.removeIf(deputyPlayer -> deputyPlayer.getUuid().equals(user.getUniqueId()));
        }
        for (DeputyLevel deputyLevel : levels) {
            if (deputyLevel.getDeputyGroup().isEmpty()) {
                continue;
            }
            for (InheritanceNode node : user.getNodes(NodeType.INHERITANCE)) {
                if (!node.getGroupName().equalsIgnoreCase(deputyLevel.getDeputyGroup())) {
                    continue;
                }
                PlayerIdentity playerIdentity = adminModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(user.getUniqueId());
                long expiry = 0;
                if (node.getExpiry() != null) {
                    expiry = node.getExpiry().toEpochMilli();
                }
                if (playerIdentity != null) {
                    this.deputies.add(new DeputyPlayer(deputyLevel, playerIdentity, expiry));
                }
            }
        }
    }

    private void runExpiryCheckTask() {
        getDeputies().stream()
                .filter(deputyPlayer -> deputyPlayer.getExpiry() > 0 &&
                        deputyPlayer.getDeputyLevel().getExpiryNotificationTime() > 0 &&
                        deputyPlayer.getExpiry() < (System.currentTimeMillis() + deputyPlayer.getDeputyLevel().getExpiryNotificationTime()))
                .forEach(deputyPlayer -> {
                    if (deputyPlayer.getExpiry() < System.currentTimeMillis()) {
                        // load user to force expiry of node
                        if (!luckPerms.getUserManager().isLoaded(deputyPlayer.getUuid())) {
                            luckPerms.getUserManager().loadUser(deputyPlayer.getUuid());
                        }
                    } else {
                        PlayerVars playerVars = adminModule.getPlugin().getPlayerVarsManager().getVars(deputyPlayer.getUuid());
                        if (playerVars.getOrDefault("last-deputy-expire-notification", long.class, 0L) < deputyPlayer.getExpiry()) {
                            playerVars.put("last-deputy-expire-notification", deputyPlayer.getExpiry());
                            sendDeputyExpiryWarning(deputyPlayer);
                        }
                    }
                });
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

    public DeputyPlayer addDeputy(CommandSender sender, DeputyLevel deputyLevel, UUID uuid, long duration) {
        DeputyPlayer previous = getDeputy(uuid);
        User user = adminModule.getGroupManager().getUser(uuid);
        if (user.getCachedData().getPermissionData().queryPermission("group." + deputyLevel.getDeputyGroup()).node() instanceof InheritanceNode oldNode) {
            user.data().remove(oldNode);
        }
        InheritanceNode node = InheritanceNode.builder(deputyLevel.getDeputyGroup())
                .expiry(duration, TimeUnit.MILLISECONDS)
                .build();
        user.data().add(node);
        luckPerms.getUserManager().saveUser(user);
        DeputyPlayer deputyPlayer = new DeputyPlayer(
                deputyLevel,
                adminModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(uuid),
                node.getExpiry() != null ? node.getExpiry().toEpochMilli() : 0
        );
        sendDeputyAddNotification(sender, previous, deputyPlayer, duration);
        return deputyPlayer;
    }

    public void removeDeputy(CommandSender sender, DeputyLevel deputyLevel, UUID uuid) {
        User user = adminModule.getGroupManager().getUser(uuid);
        DeputyPlayer deputyPlayer = getDeputy(uuid);
        Result<Tristate, Node> result = user.getCachedData().getPermissionData().queryPermission("group." + deputyLevel.getDeputyGroup());
        if (result.result().equals(Tristate.TRUE) && result.node() instanceof InheritanceNode node) {
            user.data().remove(node);
            luckPerms.getUserManager().saveUser(user);
            if (deputyPlayer != null) {
                sendDeputyRemovedNotification(sender, deputyPlayer, false);
            }
        }
    }

}
