package com.froobworld.nabsuite.modules.admin.theft;

import com.froobworld.nabsuite.data.playervar.PlayerVars;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class TheftPreventionManager implements Listener {
    private static final String THEFT_RULE_ACCEPTED_KEY = "theft-rule-accepted";
    private final AdminModule adminModule;

    public TheftPreventionManager(AdminModule adminModule) {
        this.adminModule = adminModule;
        Bukkit.getPluginManager().registerEvents(new TheftPreventionEnforcer(adminModule, this), adminModule.getPlugin());
    }

    public boolean understandsRules(Player player) {
        PlayerVars playerVars = adminModule.getPlugin().getPlayerVarsManager().getVars(player.getUniqueId());
        return playerVars.getOrDefault(THEFT_RULE_ACCEPTED_KEY, boolean.class, false);
    }

    void setNotUnderstands(Player player) {
        PlayerVars playerVars = adminModule.getPlugin().getPlayerVarsManager().getVars(player.getUniqueId());
        playerVars.put(THEFT_RULE_ACCEPTED_KEY, false);
    }

    public void setUnderstands(Player player) {
        PlayerVars playerVars = adminModule.getPlugin().getPlayerVarsManager().getVars(player.getUniqueId());
        playerVars.put(THEFT_RULE_ACCEPTED_KEY, true);
    }

}
