package com.froobworld.nabsuite.hook.scheduler;

import com.froobworld.nabsuite.NabSuite;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class BukkitSchedulerHook implements SchedulerHook {
    private final NabSuite nabSuite;

    public BukkitSchedulerHook(NabSuite nabSuite) {
        this.nabSuite = nabSuite;
    }

    @Override
    public ScheduledTask runTask(Runnable runnable) {
        return new BukkitScheduledTask(Bukkit.getScheduler().runTask(nabSuite, runnable).getTaskId());
    }

    @Override
    public ScheduledTask runTaskAsync(Runnable runnable) {
        return new BukkitScheduledTask(Bukkit.getScheduler().runTaskAsynchronously(nabSuite, runnable).getTaskId());
    }

    @Override
    public ScheduledTask runTaskDelayed(Runnable runnable, long delayTicks) {
        return new BukkitScheduledTask(Bukkit.getScheduler().runTaskLater(nabSuite, runnable, delayTicks).getTaskId());
    }

    @Override
    public ScheduledTask runRepeatingTask(Runnable runnable, long initDelay, long period) {
        return new BukkitScheduledTask(Bukkit.getScheduler().scheduleSyncRepeatingTask(nabSuite, runnable, initDelay, period));
    }

    @Override
    public ScheduledTask runRepeatingTaskAsync(Runnable runnable, long initDelay, long period) {
        return new BukkitScheduledTask(Bukkit.getScheduler().runTaskTimerAsynchronously(nabSuite, runnable, initDelay, period).getTaskId());
    }

    @Override
    public ScheduledTask runEntityTask(Runnable runnable, Runnable retired, Entity entity) {
        return runTask(runnable);
    }

    @Override
    public ScheduledTask runEntityTaskDelayed(Runnable runnable, Runnable retired, Entity entity, long delayTicks) {
        return runTaskDelayed(runnable, delayTicks);
    }

    @Override
    public ScheduledTask runEntityTaskAsap(Runnable runnable, Runnable retired, Entity entity) {
        if (Bukkit.isPrimaryThread()) {
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
        return runTask(runnable);
    }

    @Override
    public ScheduledTask runLocTask(Runnable runnable, Location location) {
        return runTask(runnable);
    }

    @Override
    public ScheduledTask runLocTaskDelayed(Runnable runnable, Location location, long delayTicks) {
        return runTaskDelayed(runnable, delayTicks);
    }

    @Override
    public ScheduledTask runLocTaskAsap(Runnable runnable, Location location) {
        if (Bukkit.isPrimaryThread()) {
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
        return runTask(runnable);
    }

    private static class BukkitScheduledTask implements ScheduledTask {
        private final int taskId;

        private BukkitScheduledTask(int taskId) {
            this.taskId = taskId;
        }

        @Override
        public void cancel() {
            Bukkit.getScheduler().cancelTask(taskId);
        }

        @Override
        public boolean isCancelled() {
            return !Bukkit.getScheduler().isQueued(taskId) && !Bukkit.getScheduler().isCurrentlyRunning(taskId);
        }
    }

}
