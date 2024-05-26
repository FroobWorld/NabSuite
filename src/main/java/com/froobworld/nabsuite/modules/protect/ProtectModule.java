package com.froobworld.nabsuite.modules.protect;

import com.froobworld.nabsuite.NabModule;
import com.froobworld.nabsuite.NabSuite;
import com.froobworld.nabsuite.modules.protect.area.AreaManager;
import com.froobworld.nabsuite.modules.protect.area.PlayerSelectionManager;
import com.froobworld.nabsuite.modules.protect.command.*;
import com.froobworld.nabsuite.modules.protect.config.ProtectConfig;
import com.froobworld.nabsuite.modules.protect.horse.HorseManager;
import com.froobworld.nabsuite.modules.protect.lock.LockManager;
import com.froobworld.nabsuite.modules.protect.user.GroupUserManager;
import com.froobworld.nabsuite.modules.protect.user.UserManager;
import com.froobworld.nabsuite.modules.protect.vehicle.VehicleTracker;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;

public class ProtectModule extends NabModule {
    private ProtectConfig protectConfig;
    private final PlayerSelectionManager playerSelectionManager = new PlayerSelectionManager();
    private UserManager userManager;
    private GroupUserManager groupUserManager;
    private AreaManager areaManager;
    private HorseManager horseManager;
    private LockManager lockManager;
    private VehicleTracker vehicleTracker;


    public ProtectModule(NabSuite nabSuite) {
        super(nabSuite, "protect");
    }

    @Override
    public void onEnable() {
        protectConfig = new ProtectConfig(this);
        try {
            protectConfig.load();
        } catch (Exception e) {
            getPlugin().getSLF4JLogger().error("Exception while loading config", e);
            Bukkit.getPluginManager().disablePlugin(getPlugin());
            return;
        }
        vehicleTracker = new VehicleTracker(this);
        userManager = new UserManager(this);
        groupUserManager = new GroupUserManager(this);
        areaManager = new AreaManager(this);
        horseManager = new HorseManager(this);
        lockManager = new LockManager(this);

        Lists.newArrayList(
                new AreaCommand(this),
                new AreasCommand(this),
                new Corner1Command(this),
                new Corner2Command(this),
                new SetAreaCommand(this),
                new DelAreaCommand(this),
                new RedefineAreaCommand(this),
                new ClaimCommand(this),
                new ClaimHorseCommand(this),
                new UnclaimHorseCommand(this),
                new LockCommand(this),
                new GlobalAreaCommand(this),
                new HorseCommand(this)
        ).forEach(getPlugin().getCommandManager()::registerCommand);

    }

    @Override
    public void onDisable() {
        areaManager.shutdown();
        horseManager.shutdown();
    }

    public ProtectConfig getConfig() {
        return protectConfig;
    }

    @Override
    public void postModulesEnable() {
        areaManager.postStartup();
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public GroupUserManager getGroupUserManager() {
        return groupUserManager;
    }

    public AreaManager getAreaManager() {
        return areaManager;
    }

    public PlayerSelectionManager getPlayerSelectionManager() {
        return playerSelectionManager;
    }

    public LockManager getLockManager() {
        return lockManager;
    }

    public HorseManager getHorseManager() {
        return horseManager;
    }

    public VehicleTracker getVehicleTracker() {
        return vehicleTracker;
    }
}
