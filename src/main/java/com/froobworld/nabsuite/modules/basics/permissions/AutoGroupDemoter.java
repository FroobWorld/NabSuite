package com.froobworld.nabsuite.modules.basics.permissions;

import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.player.PlayerData;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.matcher.NodeMatcher;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class AutoGroupDemoter {
    private final BasicsModule basicsModule;
    private final LuckPerms luckPerms;

    public AutoGroupDemoter(BasicsModule basicsModule, LuckPerms luckPerms) {
        this.basicsModule = basicsModule;
        this.luckPerms = luckPerms;

        // Run 30s after start and then every 4h
        Bukkit.getScheduler().runTaskTimerAsynchronously(basicsModule.getPlugin(), this::demotePlayersTask, 20*30, 20*60*60*4);
    }

    private void demotePlayersTask() {
        try {
            UserManager lpUserManager = luckPerms.getUserManager();
            for (String groupName: basicsModule.getConfig().autoDemote.groups.get()) {
                int inactiveDays = basicsModule.getConfig().autoDemote.inactiveTime.of(groupName).get();
                String targetGroup = basicsModule.getConfig().autoDemote.demoteToGroup.of(groupName).get();
                long cutoff = System.currentTimeMillis() - (long) inactiveDays * 24 * 60 * 60 * 1000;
                NodeMatcher<InheritanceNode> matcher = NodeMatcher.key(InheritanceNode.builder(groupName).build());
                for (UUID uuid : lpUserManager.searchAll(matcher).get().keySet()) {
                    User user = lpUserManager.loadUser(uuid).get();
                    if (user.getCachedData().getPermissionData().checkPermission(GroupManager.SETGROUP_IMMUNE_PERMISSION).asBoolean()) {
                        continue;
                    }
                    if (groupName.equalsIgnoreCase(user.getPrimaryGroup())) {
                        PlayerData playerData = basicsModule.getPlayerDataManager().getPlayerData(uuid);
                        if (playerData != null && playerData.getLastPlayed() > 0 && playerData.getLastPlayed() < cutoff && playerData.getLastGroupChange() < cutoff) {
                            basicsModule.getGroupManager().changePrimaryGroup(uuid, targetGroup).handleAsync((v, e) -> {
                                PlayerIdentity playerIdentity = basicsModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(uuid);
                                if (e != null) {
                                    basicsModule.getPlugin().getSLF4JLogger().error(
                                            "AutoGroupDemoter: Failed changing group of {} from {} to {}",
                                            playerIdentity.getLastName(), groupName, targetGroup, e.getCause()
                                    );
                                } else {
                                    basicsModule.getPlugin().getSLF4JLogger().info(
                                            "AutoGroupDemoter: Changed group of {} from {} to {}",
                                            playerIdentity.getLastName(), groupName, targetGroup
                                    );
                                    basicsModule.getPlugin().getModule(AdminModule.class).getTicketManager().createSystemTicket(basicsModule.getSpawnManager().getSpawnLocation(),
                                            "Player " + playerIdentity.getLastName() + " has changed group from '" + groupName + "' to '" + targetGroup + "' due to inactivity. Please ensure their Discord roles are updated."
                                    );
                                }
                                return null;
                            }, Bukkit.getScheduler().getMainThreadExecutor(basicsModule.getPlugin()));
                        }
                    }

                }
            }
        } catch (InterruptedException | ExecutionException e) {
            basicsModule.getPlugin().getSLF4JLogger().error("AutoGroupDemoter: Interrupted while waiting for luckperms", e);
        }
    }

}
