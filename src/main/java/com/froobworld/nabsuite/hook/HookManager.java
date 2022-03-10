package com.froobworld.nabsuite.hook;

public class HookManager {
    private final DynmapHook dynmapHook;

    public HookManager() {
        dynmapHook = new DynmapHook();
    }

    public DynmapHook getDynmapHook() {
        return dynmapHook;
    }

}
