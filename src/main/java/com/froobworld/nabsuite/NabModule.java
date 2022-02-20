package com.froobworld.nabsuite;

import java.io.File;
import java.io.InputStream;

public abstract class NabModule {
    protected final String name;
    protected final NabSuite nabSuite;
    protected final File dataDir;

    public NabModule(NabSuite nabSuite, String name) {
        this.name = name;
        this.dataDir = new File(nabSuite.getDataFolder(), name + "/");
        this.nabSuite = nabSuite;
    }

    public void onEnable() {}

    public void onDisable() {}

    public void postModulesEnable() {}

    public File getDataFolder() {
        return dataDir;
    }

    public NabSuite getPlugin() {
        return nabSuite;
    }

    public InputStream getResource(String fileName) {
        return nabSuite.getResource("resources/" + name + "/" + fileName);
    }

}
