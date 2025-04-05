package com.froobworld.nabsuite.modules.nabmode;

import com.froobworld.nabsuite.NabModule;
import com.froobworld.nabsuite.NabSuite;
import com.froobworld.nabsuite.modules.nabmode.command.NabModeCommand;
import com.froobworld.nabsuite.modules.nabmode.command.NabsBeGoneCommand;
import com.froobworld.nabsuite.modules.nabmode.nabdimension.NabModeManager;
import com.google.common.collect.Lists;

public class NabModeModule extends NabModule {
    private NabModeManager nabModeManager;

    public NabModeModule(NabSuite nabSuite) {
        super(nabSuite, "nabmode");
    }

    @Override
    public void preModulesEnable() {
        this.nabModeManager = new NabModeManager(this);
    }

    @Override
    public void onEnable() {
        Lists.newArrayList(
                new NabModeCommand(this),
                new NabsBeGoneCommand(this)
        ).forEach(getPlugin().getCommandManager()::registerCommand);
    }

    public NabModeManager getNabModeManager() {
        return nabModeManager;
    }

}
