package com.froobworld.nabsuite.modules.admin.deputy;

import com.froobworld.nabsuite.modules.admin.config.AdminConfig;
import org.bukkit.permissions.Permissible;

import java.util.*;

public class DeputyLevel {

    private final String name;
    private final AdminConfig.DeputySettings settings;
    private Set<UUID> candidates;

    public DeputyLevel(String name, AdminConfig.DeputySettings settings) {
        this.name = name;
        this.settings = settings;
        this.candidates = Collections.emptySet();
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

    public Set<UUID> getCandidates() {
        return candidates;
    }

    public void setCandidates(Set<UUID> candidates) {
        this.candidates = candidates;
    }

    public boolean checkManagePermission(Permissible sender) {
        return sender.hasPermission(DeputyManager.MANAGE_DEPUTY_PREFIX + name);
    }

    public boolean checkListPermission(Permissible sender) {
        return sender.hasPermission(DeputyManager.LIST_DEPUTY_PREFIX + name);
    }
}
