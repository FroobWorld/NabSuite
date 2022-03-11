package com.froobworld.nabsuite.modules.protect.user;

import com.froobworld.nabsuite.modules.protect.ProtectModule;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.query.QueryMode;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class GroupUserManager implements Listener {
    private final LuckPerms luckPerms;
    private final Map<Player, Set<String>> groupMembershipsMap = new WeakHashMap<>();
    private final ExecutorService executorService;
    private final Set<String> allowableGroups;

    public GroupUserManager(ProtectModule protectModule) {
        this.executorService = Executors.newSingleThreadExecutor();
        luckPerms = protectModule.getPlugin().getHookManager().getLuckPermsHook().getLuckPerms();
        if (luckPerms != null) {
            luckPerms.getEventBus().subscribe(UserDataRecalculateEvent.class, this::onUserDataRecalculate);
            Bukkit.getPluginManager().registerEvents(this, protectModule.getPlugin());
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
        return groupMembershipsMap.getOrDefault(player, Collections.emptySet());
    }

    private void updatePlayer(Player player) {
        String primaryGroup = luckPerms.getUserManager().getUser(player.getUniqueId()).getPrimaryGroup();
        Set<String> groupMemberships = luckPerms.getGroupManager().getGroup(primaryGroup).getInheritedGroups(QueryOptions.builder(QueryMode.NON_CONTEXTUAL).build()).stream()
                .map(Group::getName)
                .collect(Collectors.toSet());
        groupMemberships.add(primaryGroup);
        groupMembershipsMap.put(player, groupMemberships);
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
