package com.froobworld.nabsuite.hook.scheduler;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public interface SchedulerHook {

    ScheduledTask runTask(Runnable runnable);

    ScheduledTask runTaskAsync(Runnable runnable);

    ScheduledTask runTaskDelayed(Runnable runnable, long delayTicks);

    ScheduledTask runRepeatingTask(Runnable runnable, long initDelay, long period);

    ScheduledTask runRepeatingTaskAsync(Runnable runnable, long initDelay, long period);

    ScheduledTask runEntityTask(Runnable runnable, Runnable retired, Entity entity);

    ScheduledTask runEntityTaskDelayed(Runnable runnable, Runnable retired, Entity entity, long delayTicks);

    ScheduledTask runEntityTaskAsap(Runnable runnable, Runnable retired, Entity entity);

    ScheduledTask runLocTask(Runnable runnable, Location location);

    ScheduledTask runLocTaskDelayed(Runnable runnable, Location location, long delayTicks);

    ScheduledTask runLocTaskAsap(Runnable runnable, Location location);

}
