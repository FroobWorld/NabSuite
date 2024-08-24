package com.froobworld.nabsuite.user;

import com.froobworld.nabsuite.NabSuite;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryMode;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class GroupUserManager implements Listener {
    private final LuckPerms luckPerms;
    private final Map<UUID, Set<String>> groupMembershipsMap = new WeakHashMap<>();
    private final ExecutorService executorService;
    private final Set<String> allowableGroups;

    public GroupUserManager(NabSuite nabSuite) {
        this.executorService = Executors.newSingleThreadExecutor();
        luckPerms = nabSuite.getHookManager().getLuckPermsHook().getLuckPerms();
        if (luckPerms != null) {
            luckPerms.getEventBus().subscribe(UserDataRecalculateEvent.class, this::onUserDataRecalculate);
            Bukkit.getPluginManager().registerEvents(this, nabSuite);
            this.allowableGroups = luckPerms.getGroupManager().getLoadedGroups().stream()
                    .map(Group::getName)
                    .collect(Collectors.toSet());
            Bukkit.getOnlinePlayers().forEach(this::updatePlayer);
        } else {
            this.allowableGroups = Collections.emptySet();
        }
    }

    public Set<String> getAllowableGroups() {
        return allowableGroups;
    }

    public Set<String> getGroupMemberships(Player player) {
        return groupMembershipsMap.getOrDefault(player.getUniqueId(), Collections.emptySet());
    }

    private void updatePlayer(UUID uuid) {
        User user = luckPerms.getUserManager().getUser(uuid);
        if (user == null) {
            return;
        }
        Group primaryGroup =  luckPerms.getGroupManager().getGroup(user.getPrimaryGroup());
        if (primaryGroup == null) {
            return;
        }
        Set<String> groupMemberships = primaryGroup.getInheritedGroups(QueryOptions.builder(QueryMode.NON_CONTEXTUAL).build()).stream()
                .map(Group::getName)
                .collect(Collectors.toSet());
        groupMemberships.add(primaryGroup.getName());
        groupMembershipsMap.put(uuid, groupMemberships);
    }

    private void updatePlayer(Player player) {
        updatePlayer(player.getUniqueId());
    }

    @EventHandler
    private void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
        updatePlayer(event.getUniqueId());
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        executorService.submit(() -> updatePlayer(event.getPlayer()));
    }

    private void onUserDataRecalculate(UserDataRecalculateEvent event) {
        Player player = Bukkit.getPlayer(event.getUser().getUniqueId());
        if (player != null) {
            executorService.submit(() -> updatePlayer(player));
        }
    }

}
