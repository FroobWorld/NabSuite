package com.froobworld.nabsuite.data;

import com.froobworld.nabsuite.NabSuite;
import com.froobworld.nabsuite.hook.scheduler.ScheduledTask;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;

public class DataSaver {
    private final NabSuite nabSuite;
    private ExecutorService ioService = null;
    private final long cyclePeriod;
    private ScheduledTask scheduledTask;
    private final Map<Class<?>, Function<?, byte[]>> dataSerialisers = new HashMap<>();
    private final Map<Class<?>, Function<?, File>> fileMappers = new HashMap<>();
    private final Map<Object, QueuedSave> saveQueue = new LinkedHashMap<>();

    public DataSaver(NabSuite nabSuite, long cyclePeriod) {
        if (cyclePeriod < 1) {
            throw new IllegalArgumentException("Cycle period must be positive");
        }
        this.nabSuite = nabSuite;
        this.cyclePeriod = cyclePeriod;
    }

    public void start() {
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            throw new IllegalStateException("Already running.");
        }
        ioService = Executors.newSingleThreadExecutor();
        scheduledTask = nabSuite.getHookManager().getSchedulerHook().runRepeatingTask(this::flush, cyclePeriod, cyclePeriod);
    }

    public void stop() {
        if (scheduledTask == null || scheduledTask.isCancelled()) {
            throw new IllegalStateException("Already stopped.");
        }
        scheduledTask.cancel();
        flush();
        ioService.shutdown();
        while (!ioService.isTerminated()) {}
        scheduledTask = null;
    }

    public void flush() {
        synchronized (saveQueue) {
            for (QueuedSave queuedSave : saveQueue.values()) {
                byte[] data = queuedSave.dataSupplier.get();
                ioService.submit(() -> {
                    //noinspection ResultOfMethodCallIgnored
                    queuedSave.location.getParentFile().mkdirs();
                    try {
                        Files.write(queuedSave.location.toPath(), data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            saveQueue.clear();
        }
    }

    public <T> void addDataType(Class<T> dataType, Function<T, byte[]> serialiser, Function<T, File> fileMapper) {
        dataSerialisers.put(dataType, serialiser);
        fileMappers.put(dataType, fileMapper);
    }

    public <T> void scheduleSave(T data) {
        @SuppressWarnings("unchecked") Function<T, byte[]> serialiser = (Function<T, byte[]>) dataSerialisers.get(data.getClass());
        @SuppressWarnings("unchecked") Function<T, File> fileMapper = (Function<T, File>) fileMappers.get(data.getClass());
        if (serialiser == null || fileMapper == null) {
            throw new IllegalArgumentException("Unknown data type: " + data.getClass());
        }
        synchronized (saveQueue) {
            saveQueue.put(data, new QueuedSave(() -> serialiser.apply(data), fileMapper.apply(data)));
        }
    }

    public <T> void scheduleDeletion(T data) {
        @SuppressWarnings("unchecked") Function<T, File> fileMapper = (Function<T, File>) fileMappers.get(data.getClass());
        if (fileMapper == null) {
            throw new IllegalArgumentException("Unknown data type: " + data.getClass());
        }
        synchronized (saveQueue) {
            saveQueue.remove(data);
            File file = fileMapper.apply(data);
            ioService.submit(file::delete);
        }
    }

    private static class QueuedSave {
        private final Supplier<byte[]> dataSupplier;
        private final File location;

        private QueuedSave(Supplier<byte[]> dataSupplier, File location) {
            this.dataSupplier = dataSupplier;
            this.location = location;
        }
    }

}
