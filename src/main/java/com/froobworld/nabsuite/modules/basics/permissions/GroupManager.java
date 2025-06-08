package com.froobworld.nabsuite.modules.basics.permissions;

import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.event.user.track.UserTrackEvent;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class GroupManager implements Listener {
    private final BasicsModule basicsModule;
    private final AdminModule adminModule;
    private final LuckPerms luckPerms;

    public static final String SETGROUP_IMMUNE_PERMISSION = "nabsuite.setgroup.immune";
    public static final String SETGROUP_PERMISSION_PREFIX = "nabsuite.setgroup.group.";

    public GroupManager(BasicsModule basicsModule) {
        this.basicsModule = basicsModule;
        this.adminModule = basicsModule.getPlugin().getModule(AdminModule.class);
        luckPerms = basicsModule.getPlugin().getHookManager().getLuckPermsHook().getLuckPerms();
        if (luckPerms != null) {
            Bukkit.getPluginManager().registerEvents(this, basicsModule.getPlugin());
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateDisplayName(player);
            }
            luckPerms.getEventBus().subscribe(UserDataRecalculateEvent.class, this::onUserDataRecalculate);
            luckPerms.getEventBus().subscribe(UserTrackEvent.class, this::onTrackChangePosition);
            new AutoGroupChecker(basicsModule, luckPerms);
            new AutoGroupDemoter(basicsModule);
        }
    }

    public void postStartup() {
        if (adminModule != null) {
            adminModule.getNotificationCentre().registerNotificationKey("change-group-alert", "nabsuite.changegroupalert");
        }
    }

    private void onTrackChangePosition(UserTrackEvent event) {
        if (adminModule != null) {
            if (event.getGroupTo().isEmpty()) {
                return;
            }
            adminModule.getNotificationCentre().sendNotification("change-group-alert",
                    Component.text(event.getUser().getUsername() + " has been added to group '" + event.getGroupTo().get() + "'.", NamedTextColor.YELLOW)
            );
            Player player = Bukkit.getPlayer(event.getUser().getUniqueId());
            if (player != null) {
                player.sendMessage(Component.text("You have been added to group '" + event.getGroupTo().get() + "'.", NamedTextColor.YELLOW));
            }
        }
    }

    private void onUserDataRecalculate(UserDataRecalculateEvent event) {
        Player player = Bukkit.getPlayer(event.getUser().getUniqueId());
        if (player != null) {
            updateDisplayName(player);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        updateDisplayName(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onPlayerChat(AsyncChatEvent event) {
        event.renderer(
                ChatRenderer.viewerUnaware((source, displayName, message) -> displayName
                        .append(Component.text(": ", NamedTextColor.WHITE).append(message))
                )
        );
    }

    private void updateDisplayName(Player player) {
        if (Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTaskAsynchronously(basicsModule.getPlugin(), () -> updateDisplayName(player));
            return;
        }
        updateDisplayName(player, luckPerms.getUserManager().getUser(player.getUniqueId()).getPrimaryGroup());
    }

    private void updateDisplayName(Player player, String group) {
        String format = basicsModule.getConfig().displayNameFormats.of(group).get();
        player.displayName(MiniMessage.miniMessage().deserialize(format, TagResolver.resolver("name", Tag.inserting(Component.text(player.getName())))));
    }

    public final void changePrimaryGroup(UUID uuid, String group) {
        PlayerIdentity playerIdentity = basicsModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(uuid);
        if (playerIdentity == null) {
            throw new IllegalArgumentException("Unknown user");
        }
        User user = basicsModule.getPlugin().getModule(AdminModule.class).getGroupManager().getUser(uuid);
        if (user == null) {
            throw new IllegalArgumentException("Unknown user");
        }

        String currentGroup = user.getPrimaryGroup();
        if (group.equalsIgnoreCase(currentGroup)) {
            throw new IllegalArgumentException("User " + playerIdentity.getLastName() + " is already a member of group '" + group + "'.");
        }
        Group targetGroup = luckPerms.getGroupManager().getGroup(group);
        if (targetGroup == null) {
            throw new IllegalArgumentException("Group '" + group + "' doesn't exist.");
        }
        user.data().add(InheritanceNode.builder(targetGroup).build());
        user.data().remove(InheritanceNode.builder(currentGroup).build());
        luckPerms.getUserManager().saveUser(user);
        basicsModule.getPlugin().getPlayerVarsManager().getVars(user.getUniqueId()).put("last-group-change", System.currentTimeMillis());

    }

}
