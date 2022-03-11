package com.froobworld.nabsuite.hook;

public class HookManager {
    private final LuckPermsHook luckPermsHook;
    private final DynmapHook dynmapHook;

    public HookManager() {
        luckPermsHook = new LuckPermsHook();
        dynmapHook = new DynmapHook();
    }

    public LuckPermsHook getLuckPermsHook() {
        return luckPermsHook;
    }

    public DynmapHook getDynmapHook() {
        return dynmapHook;
    }

}
