package com.froobworld.nabsuite.hook;

import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class LuckPermsHook {
    private final LuckPerms luckPerms;

    public LuckPermsHook() {
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
            RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
            if (provider != null) {
                luckPerms = provider.getProvider();
                return;
            }
        }
        luckPerms = null;
    }

    public LuckPerms getLuckPerms() {
        return luckPerms;
    }

}
