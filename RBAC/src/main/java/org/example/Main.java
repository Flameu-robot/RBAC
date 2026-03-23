package org.example;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    private static final int THREAD_COUNT = 5;
    private static final int PROGRESS_BAR_LENGTH = 25;
    private static final int STEP_DELAY_MS = 150;

    private static final ConcurrentHashMap<Integer, ThreadInfo> threadInfoMap = new ConcurrentHashMap<>();
    private static final Object printLock = new Object();

    static void main(String[] args) throws InterruptedException {
        System.out.printf("| Threads: %d | Bar length: %d | Delay: %d ms%n",
                THREAD_COUNT, PROGRESS_BAR_LENGTH, STEP_DELAY_MS);
        System.out.println("=========================================================================");
        System.out.println();

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(THREAD_COUNT);
        AtomicInteger threadNumber = new AtomicInteger(1);

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int orderNum = threadNumber.getAndIncrement();

            Thread workerThread = new Thread(() -> {
                try {
                    startLatch.await();
                    runTask(orderNum, finishLatch);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Calc-" + orderNum);

            workerThread.start();
        }

        startLatch.countDown();

        Thread monitor = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                printAllProgress();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "Monitor");
        monitor.start();

        finishLatch.await();
        monitor.interrupt();
        monitor.join();

        printFinalResults();
    }

    private static void runTask(int orderNum, CountDownLatch finishLatch) {
        long threadId = Thread.currentThread().threadId();
        long startTime = System.currentTimeMillis();

        threadInfoMap.put(orderNum, new ThreadInfo(threadId, 0, 0, false));

        for (int step = 0; step <= PROGRESS_BAR_LENGTH; step++) {
            threadInfoMap.put(orderNum, new ThreadInfo(threadId, step, 0, false));

            if (step < PROGRESS_BAR_LENGTH) {
                try {
                    Thread.sleep(STEP_DELAY_MS + (int)(Math.random() * 60));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        threadInfoMap.put(orderNum, new ThreadInfo(threadId, PROGRESS_BAR_LENGTH, elapsed, true));

        finishLatch.countDown();
    }

    private static void printAllProgress() {
        synchronized (printLock) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n--- Current Progress ---\n");

            for (int i = 1; i <= THREAD_COUNT; i++) {
                ThreadInfo info = threadInfoMap.get(i);
                if (info != null) {
                    sb.append(formatProgressLine(i, info)).append("\n");
                }
            }
            sb.append("------------------------\n");

            System.out.print(sb);
        }
    }

    private static String formatProgressLine(int orderNum, ThreadInfo info) {
        int percent = (info.progress * 100) / PROGRESS_BAR_LENGTH;

        String bar = "#".repeat(info.progress) +
                ".".repeat(PROGRESS_BAR_LENGTH - info.progress);

        String status = info.completed
                ? String.format("COMPLETED in %d ms", info.elapsedTime)
                : "running...";

        return String.format("Thread #%d | ID: %-4d | [%s] %3d%% | %s",
                orderNum, info.threadId, bar, percent, status);
    }

    private static void printFinalResults() {
        System.out.println("\n=========================================================================");
        System.out.println("                          RESULTS");
        System.out.println("=========================================================================");

        long totalTime = 0;
        long minTime = Long.MAX_VALUE;
        long maxTime = 0;

        for (int i = 1; i <= THREAD_COUNT; i++) {
            ThreadInfo info = threadInfoMap.get(i);
            if (info != null) {
                System.out.println(formatProgressLine(i, info));
                totalTime += info.elapsedTime;
                minTime = Math.min(minTime, info.elapsedTime);
                maxTime = Math.max(maxTime, info.elapsedTime);
            }
        }

        System.out.println("=========================================================================");
        System.out.println("                           STATISTICS");
        System.out.println("-------------------------------------------------------------------------");
        System.out.printf("| Average time: %d ms%n", totalTime / THREAD_COUNT);
        System.out.printf("| Min time:     %d ms%n", minTime);
        System.out.printf("| Max time:     %d ms%n", maxTime);
        System.out.println("=========================================================================");
    }

    record ThreadInfo(long threadId, int progress, long elapsedTime, boolean completed) {}
}
