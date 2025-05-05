package com.froobworld.nabsuite.modules.admin.deputy;

import com.froobworld.nabsuite.modules.admin.config.AdminConfig;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.Flag;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.permissions.Permissible;

import java.util.*;

public class DeputyLevel {

    private final String name;
    private final AdminConfig.DeputySettings settings;

    public DeputyLevel(String name, AdminConfig.DeputySettings settings) {
        this.name = name;
        this.settings = settings;
    }

    public String getName() {
        return name;
    }

    public Long getMaximumDuration() {
        return settings.maximumDuration.get();
    }

    public long getDefaultDuration() {
        return settings.defaultDuration.get();
    }

    public String getDeputyGroup() {
        return settings.deputyGroup.get();
    }

    public List<String> getCandidateGroups() {
        return settings.candidateGroups.get();
    }

    public long getExpiryNotificationTime() {
        return settings.expiryNotificationTime.get();
    }

    public boolean isEligible(User user) {
        QueryOptions queryOptions = QueryOptions.nonContextual().toBuilder().flag(Flag.RESOLVE_INHERITANCE, false).build();
        return user.getInheritedGroups(queryOptions).stream()
                .anyMatch(group -> getCandidateGroups().contains(group.getName()));
    }

    public boolean checkManagePermission(Permissible sender) {
        return sender.hasPermission(DeputyManager.MANAGE_DEPUTY_PREFIX + name);
    }

    public boolean checkListPermission(Permissible sender) {
        return sender.hasPermission(DeputyManager.LIST_DEPUTY_PREFIX + name);
    }
}
