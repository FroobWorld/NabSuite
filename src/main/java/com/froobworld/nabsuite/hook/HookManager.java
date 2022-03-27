package com.froobworld.nabsuite.hook;

public class HookManager {
    private final LuckPermsHook luckPermsHook;
    private final DynmapHook dynmapHook;
    private final DiscordSRVHook discordSRVHook;

    public HookManager() {
        luckPermsHook = new LuckPermsHook();
        dynmapHook = new DynmapHook();
        discordSRVHook = new DiscordSRVHook();
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
}
