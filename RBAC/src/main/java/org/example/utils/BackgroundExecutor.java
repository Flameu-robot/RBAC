package org.example.utils;

import java.util.concurrent.*;

public class BackgroundExecutor {

    private final ExecutorService executor;

    public BackgroundExecutor() {
        this(4);
    }

    public BackgroundExecutor(int poolSize) {
        this.executor = Executors.newFixedThreadPool(poolSize, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("Background-" + t.threadId());
            return t;
        });
    }

    public Future<?> submit(Runnable task) {
        return executor.submit(task);
    }

    public <T> Future<T> submit(Callable<T> task) {
        return executor.submit(task);
    }

    public void execute(Runnable task) {
        executor.execute(task);
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public boolean isShutdown() {
        return executor.isShutdown();
    }
}
