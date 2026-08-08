package com.froobworld.nabsuite.hook;

public class HookManager {
    private final LuckPermsHook luckPermsHook;
    private final DynmapHook dynmapHook;
    private final SparkHook sparkHook;

    public HookManager() {
        luckPermsHook = new LuckPermsHook();
        dynmapHook = new DynmapHook();
        sparkHook = new SparkHook();
    }

    public LuckPermsHook getLuckPermsHook() {
        return luckPermsHook;
    }

    public DynmapHook getDynmapHook() {
        return dynmapHook;
    }

    public SparkHook getSparkHook() {
        return sparkHook;
    }

}
