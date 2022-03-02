package com.froobworld.nabsuite;

import com.froobworld.nabsuite.command.NabCommandManager;
import com.froobworld.nabsuite.data.identity.PlayerIdentityManager;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.basics.BasicsModule;
import com.froobworld.nabsuite.modules.mechs.MechsModule;
import com.froobworld.nabsuite.modules.protect.ProtectModule;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class NabSuite extends JavaPlugin {
    private final Map<Class<?>, NabModule> modules = new LinkedHashMap<>();
    private NabCommandManager commandManager;
    private PlayerIdentityManager playerIdentityManager;

    @Override
    public void onEnable() {
        try {
            commandManager = new NabCommandManager(this);
        } catch (Exception e) {
            getSLF4JLogger().error("Unable to initiate command manager", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        playerIdentityManager = new PlayerIdentityManager(this);
        if (modules.isEmpty()) {
            addModule(new BasicsModule(this));
            addModule(new AdminModule(this));
            addModule(new ProtectModule(this));
            addModule(new MechsModule(this));
        }
        modules.values().forEach(NabModule::preModulesEnable);
        modules.values().forEach(NabModule::onEnable);
        modules.values().forEach(NabModule::postModulesEnable);
    }

    @Override
    public void onDisable() {
        List<NabModule> modulesList = new ArrayList<>(modules.values());
        for (int i = modulesList.size() - 1; i >= 0; i--) {
            modulesList.get(i).onDisable();
        }
    }

    public NabCommandManager getCommandManager() {
        return commandManager;
    }

    public PlayerIdentityManager getPlayerIdentityManager() {
        return playerIdentityManager;
    }

    private void addModule(NabModule module) {
        modules.put(module.getClass(), module);
    }

    public <T extends NabModule> T getModule(Class<T> type) {
        //noinspection unchecked
        return (T) modules.get(type);
    }

}
