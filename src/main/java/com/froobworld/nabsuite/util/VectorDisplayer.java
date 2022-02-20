package com.froobworld.nabsuite.util;

import org.bukkit.util.Vector;

public final class VectorDisplayer {

    private VectorDisplayer() {}

    public static String vectorToString(Vector vector, boolean integerValues) {
        if (integerValues) {
            return vector.getBlockX() + "," + vector.getBlockY() + "," + vector.getBlockZ();
        } else {
            return vector.toString();
        }
    }

}
