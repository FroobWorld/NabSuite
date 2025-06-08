package com.froobworld.nabsuite.modules.basics.permissions;

import com.froobworld.nabsuite.data.identity.PlayerIdentity;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.basics.player.PlayerData;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class AutoGroupDemoter {
    private final BasicsModule basicsModule;
    private final Set<String> checkGroups;

    public AutoGroupDemoter(BasicsModule basicsModule) {
        this.basicsModule = basicsModule;
        this.checkGroups = basicsModule.getConfig().autoDemote.groups.get().stream().map(String::toLowerCase).collect(Collectors.toSet());

        // Run 30s after start and then every 4h
        Bukkit.getScheduler().runTaskTimer(basicsModule.getPlugin(), this::demotePlayersTask, 20*30, 20*60*60*4);
    }

    private void demotePlayersTask() {
        for (User user: basicsModule.getPlugin().getModule(AdminModule.class).getGroupManager().getUsers()) {
            if (user.getCachedData().getPermissionData().checkPermission(GroupManager.SETGROUP_IMMUNE_PERMISSION).asBoolean()) {
                continue;
            }
            String groupName = user.getPrimaryGroup();
            if (!checkGroups.contains(groupName.toLowerCase())) {
                continue;
            }
            int inactiveDays = basicsModule.getConfig().autoDemote.inactiveTime.of(groupName).get();
            if (inactiveDays <= 0) {
                continue;
            }
            long cutoff = System.currentTimeMillis() - (long) inactiveDays * 24 * 60 * 60 * 1000;

            PlayerData playerData = basicsModule.getPlayerDataManager().getPlayerData(user.getUniqueId());
            Long lastGroupChange = basicsModule.getPlugin().getPlayerVarsManager().getVars(user.getUniqueId()).getOrDefault("last-group-change", Long.class, 0L);
            if (playerData != null && playerData.getLastPlayed() > 0 && playerData.getLastPlayed() < cutoff && lastGroupChange < cutoff) {
                PlayerIdentity playerIdentity = basicsModule.getPlugin().getPlayerIdentityManager().getPlayerIdentity(user.getUniqueId());
                String targetGroup = basicsModule.getConfig().autoDemote.demoteToGroup.of(groupName).get();
                try {
                    basicsModule.getGroupManager().changePrimaryGroup(user.getUniqueId(), targetGroup);
                    basicsModule.getPlugin().getModule(AdminModule.class).getTicketManager().createSystemTicket("Player " + playerIdentity.getLastName() + " has changed group from '" + groupName + "' to '" + targetGroup + "' due to inactivity. Please ensure their Discord roles are updated.");
                } catch (Exception e) {
                    basicsModule.getPlugin().getSLF4JLogger().error(
                            "AutoGroupDemoter: Failed changing group of {} from {} to {}",
                            playerIdentity.getLastName(), groupName, targetGroup, e
                    );
                }
            }
        }
    }

}
