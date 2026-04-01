package com.froobworld.nabsuite.modules.mechs.holidayevent;

import com.froobworld.nabsuite.data.playervar.PlayerVars;
import com.froobworld.nabsuite.modules.mechs.MechsModule;
import com.froobworld.nabsuite.modules.mechs.holidayevent.aprilfools26.AprilFools26Event;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class HolidayEventManager {
    private static final String DISALBED_EVENTS_KEY = "disabled-events";
    private final MechsModule mechsModule;
    private HolidayEvent activeEvent;

    public HolidayEventManager(MechsModule mechsModule) {
        this.mechsModule = mechsModule;

        registerEvent(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 8),
                () -> new AprilFools26Event(this.mechsModule)
        );
    }

    private void registerEvent(LocalDate from, LocalDate to, Supplier<HolidayEvent> eventCreator) {
        if (activeEvent != null) {
            return;
        }
        LocalDate now = LocalDate.now();
        if (now.isAfter(from) && now.isBefore(to) || now.isEqual(from) || now.isEqual(to)) {
            activeEvent = eventCreator.get();
        }
    }

    public String currentEvent() {
        if (activeEvent == null) {
            return null;
        }
        return activeEvent.getHolidayKey();
    }

    public boolean toggleEvent(Player player) {
        if (activeEvent == null) {
            return false;
        }

        PlayerVars vars = mechsModule.getPlugin().getPlayerVarsManager().getVars(player.getUniqueId());
        List<String> disabledEvents = vars.getList(DISALBED_EVENTS_KEY, String.class);
        if (disabledEvents == null) {
            disabledEvents = new ArrayList<>();
        } else {
            disabledEvents = new ArrayList<>(disabledEvents);
        }

        if (disabledEvents.contains(activeEvent.getHolidayKey())) {
            disabledEvents.remove(activeEvent.getHolidayKey());
            activeEvent.toggleOn(player);
        } else {
            disabledEvents.add(activeEvent.getHolidayKey());
            activeEvent.toggleOff(player);
        }

        vars.putCollection(DISALBED_EVENTS_KEY, disabledEvents);
        return !disabledEvents.contains(activeEvent.getHolidayKey());
    }

    public boolean isEnabled(Player player, String eventKey) {
        PlayerVars vars = mechsModule.getPlugin().getPlayerVarsManager().getVars(player.getUniqueId());
        List<String> disabledEvents = vars.getList(DISALBED_EVENTS_KEY, String.class);

        if (disabledEvents != null) {
            return !disabledEvents.contains(eventKey);
        }
        return true;
    }


}
