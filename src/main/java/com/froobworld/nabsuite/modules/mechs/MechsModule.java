package com.froobworld.nabsuite.modules.mechs;

import com.froobworld.nabsuite.NabModule;
import com.froobworld.nabsuite.NabSuite;
import com.froobworld.nabsuite.modules.mechs.command.EffectiveViewDistanceCommand;
import com.froobworld.nabsuite.modules.mechs.command.PvpCommand;
import com.froobworld.nabsuite.modules.mechs.command.ToggleViewDistanceCommand;
import com.froobworld.nabsuite.modules.mechs.config.MechsConfig;
import com.froobworld.nabsuite.modules.mechs.signedit.SignEditDisabler;
import com.froobworld.nabsuite.modules.mechs.worldsize.WorldSizeManager;
import com.froobworld.nabsuite.modules.mechs.mobgriefing.MobGriefingManager;
import com.froobworld.nabsuite.modules.mechs.pvp.PvpManager;
import com.froobworld.nabsuite.modules.mechs.trees.TreeManager;
import com.froobworld.nabsuite.modules.mechs.viewdistance.ViewDistanceManager;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;

public class MechsModule extends NabModule {
    private MechsConfig mechsConfig;
    private PvpManager pvpManager;
    private TreeManager treeManager;
    private ViewDistanceManager viewDistanceManager;

    public MechsModule(NabSuite nabSuite) {
        super(nabSuite, "mechs");
    }

    @Override
    public void onEnable() {
        mechsConfig = new MechsConfig(this);
        try {
            mechsConfig.load();
        } catch (Exception e) {
            getPlugin().getSLF4JLogger().error("Exception while loading config", e);
            Bukkit.getPluginManager().disablePlugin(getPlugin());
            return;
        }
        this.pvpManager = new PvpManager(this);
        this.treeManager = new TreeManager(this);
        this.viewDistanceManager = new ViewDistanceManager(this);
        new WorldSizeManager(this);
        new MobGriefingManager(this);
        new SignEditDisabler(this);

        Lists.newArrayList(
                new PvpCommand(this),
                new ToggleViewDistanceCommand(this),
                new EffectiveViewDistanceCommand()
        ).forEach(getPlugin().getCommandManager()::registerCommand);
    }

    @Override
    public void onDisable() {
        treeManager.shutdown();
    }

    public MechsConfig getConfig() {
        return mechsConfig;
    }

    public PvpManager getPvpManager() {
        return pvpManager;
    }

    public ViewDistanceManager getViewDistanceManager() {
        return viewDistanceManager;
    }
}
