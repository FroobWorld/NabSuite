package com.froobworld.nabsuite.hook;

import github.scarsz.discordsrv.DiscordSRV;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class DiscordSRVHook {
    private final DiscordSRV discordSRV;

    public DiscordSRVHook() {
        Plugin discordSRV = Bukkit.getPluginManager().getPlugin("DiscordSRV");
        if (discordSRV != null) {
            this.discordSRV = (DiscordSRV) discordSRV;
        } else {
            this.discordSRV = null;
        }
    }

    public DiscordSRV getDiscordSRV() {
        return discordSRV;
    }

}
