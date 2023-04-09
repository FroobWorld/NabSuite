package com.froobworld.nabsuite.hook.scheduler;

public interface ScheduledTask {

    void cancel();

    boolean isCancelled();

}
