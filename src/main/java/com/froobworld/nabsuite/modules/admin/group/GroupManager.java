package com.froobworld.nabsuite.modules.admin.group;

import com.froobworld.nabsuite.modules.admin.AdminModule;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.event.user.UserLoadEvent;
import net.luckperms.api.model.user.User;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper class for LuckPerms for cases where we need to work with offline players synchronously
 */
public class GroupManager {
    private final AdminModule adminModule;
    private final LuckPerms luckPerms;
    private final Map<UUID, User> persistentUserCache = new HashMap<>();

    public GroupManager(AdminModule adminModule) {
        this.adminModule = adminModule;
        this.luckPerms = adminModule.getPlugin().getHookManager().getLuckPermsHook().getLuckPerms();
        loadAllUsers();
        luckPerms.getEventBus().subscribe(UserDataRecalculateEvent.class, this::onUserDataRecalculate);
        luckPerms.getEventBus().subscribe(UserLoadEvent.class, this::onUserLoad);
    }

    private void loadAllUsers() {
        // load all users with LuckPerms data into a persistent user data cache
        adminModule.getPlugin().getPlayerIdentityManager().getAllPlayerIdentities()
                .parallelStream() // load data across multiple threads
                .map(playerIdentity -> luckPerms.getUserManager().loadUser(playerIdentity.getUuid()).join())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet())
                .forEach(user -> persistentUserCache.put(user.getUniqueId(), user));
    }

    private void onUserDataRecalculate(UserDataRecalculateEvent event) {
        persistentUserCache.put(event.getUser().getUniqueId(), event.getUser());
    }

    private void onUserLoad(UserLoadEvent event) {
        persistentUserCache.put(event.getUser().getUniqueId(), event.getUser());
    }

    public User getUser(UUID uuid) {
        return persistentUserCache.get(uuid);
    }

    public Collection<User> getUsers() {
        return persistentUserCache.values();
    }

}
