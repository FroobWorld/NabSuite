package com.froobworld.nabsuite.hook;

import com.froobworld.nabsuite.NabSuite;
import com.froobworld.nabsuite.hook.scheduler.BukkitSchedulerHook;
import com.froobworld.nabsuite.hook.scheduler.RegionisedSchedulerHook;
import com.froobworld.nabsuite.hook.scheduler.SchedulerHook;

public class HookManager {
    private final LuckPermsHook luckPermsHook;
    private final DynmapHook dynmapHook;
    private final DiscordSRVHook discordSRVHook;
    private final SchedulerHook schedulerHook;

    public HookManager(NabSuite nabSuite) {
        luckPermsHook = new LuckPermsHook();
        dynmapHook = new DynmapHook();
        discordSRVHook = new DiscordSRVHook();
        if (RegionisedSchedulerHook.isCompatible()) {
            schedulerHook = new RegionisedSchedulerHook(nabSuite);
        } else {
            schedulerHook = new BukkitSchedulerHook(nabSuite);
        }
    }

    public LuckPermsHook getLuckPermsHook() {
        return luckPermsHook;
    }

    public DynmapHook getDynmapHook() {
        return dynmapHook;
    }

    public DiscordSRVHook getDiscordSRVHook() {
        return discordSRVHook;
    }

    public SchedulerHook getSchedulerHook() {
        return schedulerHook;
    }
}
