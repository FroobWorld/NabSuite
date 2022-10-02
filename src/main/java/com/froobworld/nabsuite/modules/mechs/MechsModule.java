package com.froobworld.nabsuite.modules.mechs;

import com.froobworld.nabsuite.NabModule;
import com.froobworld.nabsuite.NabSuite;
import com.froobworld.nabsuite.modules.mechs.command.PvpCommand;
import com.froobworld.nabsuite.modules.mechs.end.EndManager;
import com.froobworld.nabsuite.modules.mechs.mobgriefing.MobGriefingManager;
import com.froobworld.nabsuite.modules.mechs.pvp.PvpManager;
import com.froobworld.nabsuite.modules.mechs.trees.TreeManager;
import com.google.common.collect.Lists;

public class MechsModule extends NabModule {
    private PvpManager pvpManager;
    private TreeManager treeManager;

    public MechsModule(NabSuite nabSuite) {
        super(nabSuite, "mechs");
    }

    @Override
    public void onEnable() {
        this.pvpManager = new PvpManager(this);
        this.treeManager = new TreeManager(this);
        new EndManager(this);
        new MobGriefingManager(this);

        Lists.newArrayList(
                new PvpCommand(this)
        ).forEach(getPlugin().getCommandManager()::registerCommand);
    }

    @Override
    public void onDisable() {
        treeManager.shutdown();
    }

    public PvpManager getPvpManager() {
        return pvpManager;
    }

}
