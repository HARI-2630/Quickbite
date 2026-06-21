package com.quickbite.security;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadPoolManager {
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    public static void submit(Runnable task) {
        if (task != null) {
            executor.submit(task);
        }
    }

    public static void shutdown() {
        System.out.println("[ThreadPoolManager] Initiating shutdown of background executor service...");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                System.err.println("[ThreadPoolManager] Force shutting down executor service...");
                executor.shutdownNow();
            } else {
                System.out.println("[ThreadPoolManager] Executor service terminated cleanly.");
            }
        } catch (InterruptedException e) {
            System.err.println("[ThreadPoolManager] Interrupted during termination. Force halting...");
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
