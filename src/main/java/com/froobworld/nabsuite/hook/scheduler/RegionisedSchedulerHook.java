package com.froobworld.nabsuite.hook.scheduler;

import com.froobworld.nabsuite.NabSuite;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.concurrent.TimeUnit;

public class RegionisedSchedulerHook implements SchedulerHook {
    private final NabSuite nabSuite;

    public RegionisedSchedulerHook(NabSuite nabSuite) {
        this.nabSuite = nabSuite;
    }

    @Override
    public ScheduledTask runTask(Runnable runnable) {
        return new RegionisedScheduledTask(Bukkit.getGlobalRegionScheduler().run(nabSuite, task -> runnable.run()));
    }

    @Override
    public ScheduledTask runTaskAsync(Runnable runnable) {
        return new RegionisedScheduledTask(Bukkit.getAsyncScheduler().runNow(nabSuite, task -> runnable.run()));
    }

    @Override
    public ScheduledTask runTaskDelayed(Runnable runnable, long delayTicks) {
        return new RegionisedScheduledTask(Bukkit.getGlobalRegionScheduler().runDelayed(nabSuite, task -> runnable.run(), delayTicks));
    }

    @Override
    public ScheduledTask runRepeatingTask(Runnable runnable, long initDelay, long period) {
        return new RegionisedScheduledTask(Bukkit.getGlobalRegionScheduler().runAtFixedRate(nabSuite, task -> runnable.run(), initDelay, period));
    }

    @Override
    public ScheduledTask runRepeatingTaskAsync(Runnable runnable, long initDelay, long period) {
        return new RegionisedScheduledTask(Bukkit.getAsyncScheduler().runAtFixedRate(nabSuite, task -> runnable.run(), initDelay * 50, period * 50, TimeUnit.MILLISECONDS));
    }

    @Override
    public ScheduledTask runEntityTask(Runnable runnable, Runnable retired, Entity entity) {
        io.papermc.paper.threadedregions.scheduler.ScheduledTask scheduledTask = entity.getScheduler().run(nabSuite, task -> runnable.run(), retired);
        return scheduledTask == null ? null : new RegionisedScheduledTask(scheduledTask);
    }

    @Override
    public ScheduledTask runEntityTaskDelayed(Runnable runnable, Runnable retired, Entity entity, long delayTicks) {
        io.papermc.paper.threadedregions.scheduler.ScheduledTask scheduledTask = entity.getScheduler().runDelayed(nabSuite, task -> runnable.run(), retired, delayTicks);
        return scheduledTask == null ? null : new RegionisedScheduledTask(scheduledTask);
    }

    @Override
    public ScheduledTask runEntityTaskAsap(Runnable runnable, Runnable retired, Entity entity) {
        if (Bukkit.isOwnedByCurrentRegion(entity)) {
            runnable.run();
            return new ScheduledTask() {
                @Override
                public void cancel() {}

                @Override
                public boolean isCancelled() {
                    return false;
                }
            };
        }
        return runEntityTask(runnable, retired, entity);
    }

    @Override
    public ScheduledTask runLocTask(Runnable runnable, Location location) {
        return new RegionisedScheduledTask(Bukkit.getRegionScheduler().run(nabSuite, location, task -> runnable.run()));
    }

    @Override
    public ScheduledTask runLocTaskDelayed(Runnable runnable, Location location, long delayTicks) {
        return new RegionisedScheduledTask(Bukkit.getRegionScheduler().runDelayed(nabSuite, location, task -> runnable.run(), delayTicks));
    }

    @Override
    public ScheduledTask runLocTaskAsap(Runnable runnable, Location location) {
        if (Bukkit.isOwnedByCurrentRegion(location)) {
            runnable.run();
            return new ScheduledTask() {
                @Override
                public void cancel() {}

                @Override
                public boolean isCancelled() {
                    return false;
                }
            };
        }
        return runLocTask(runnable, location);
    }

    public static boolean isCompatible() {
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }

    private static class RegionisedScheduledTask implements ScheduledTask {
        private final io.papermc.paper.threadedregions.scheduler.ScheduledTask scheduledTask;

        private RegionisedScheduledTask(io.papermc.paper.threadedregions.scheduler.ScheduledTask scheduledTask) {
            this.scheduledTask = scheduledTask;
        }

        @Override
        public void cancel() {
            scheduledTask.cancel();
        }

        @Override
        public boolean isCancelled() {
            return scheduledTask.isCancelled();
        }
    }

}
