package com.froobworld.nabsuite.modules.mechs.holidayevent;

import org.bukkit.entity.Player;

public abstract class HolidayEvent {
    private final String holidayKey;

    protected HolidayEvent(String holidayKey) {
        this.holidayKey = holidayKey;
    }

    public final String getHolidayKey() {
        return holidayKey;
    }

    public abstract void toggleOff(Player player);

    public abstract void toggleOn(Player player);

}
