package com.froobworld.nabsuite.modules.protect.area.flag;

import java.util.Set;

public final class Flags {

    private Flags() {}

    public static final String NO_BUILD = "no-build";
    public static final String NO_INTERACT = "no-interact";
    public static final String NO_EXPLODE = "no-explode";
    public static final String NO_FIRE_SPREAD = "no-fire-spread";
    public static final String NO_FIRE_DESTROY = "no-fire-destroy";
    public static final String NO_PVP = "no-pvp";
    public static final String NO_MOB_SPAWN = "no-mob-spawn";
    public static final String NO_MOB_TARGET = "no-mob-target";
    public static final String NO_MOB_DAMAGE = "no-mob-damage";
    public static final String NO_MOB_GRIEF = "no-mob-grief";
    public static final String KEEP_INVENTORY = "keep-inventory";
    public static final String NO_WITHER = "no-wither";
    public static final String NO_HOME = "no-home";

    public static final Set<String> flags = Set.of(
            NO_BUILD,
            NO_INTERACT,
            NO_EXPLODE,
            NO_FIRE_SPREAD,
            NO_FIRE_DESTROY,
            NO_PVP,
            NO_MOB_SPAWN,
            NO_MOB_TARGET,
            NO_MOB_DAMAGE,
            NO_MOB_GRIEF,
            KEEP_INVENTORY,
            NO_WITHER,
            NO_HOME
    );

}
