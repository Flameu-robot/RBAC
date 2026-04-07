package org.example.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AuditLog {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final List<AuditEntry> entries = new ArrayList<>();

    private final BlockingQueue<AuditEntry> logQueue = new LinkedBlockingQueue<>();

    private final Thread processorThread;
    private volatile boolean running = true;

    public AuditLog() {
        this.processorThread = new Thread(this::processQueue, "AuditLog-Processor");
        this.processorThread.setDaemon(true);
        this.processorThread.start();
    }

    public synchronized void log(String action, String performer, String target, String details) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        entries.add(new AuditEntry(timestamp, action, performer, target, details));
    }

    public void logAsync(String action, String performer, String target, String details) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        AuditEntry entry = new AuditEntry(timestamp, action, performer, target, details);
        logQueue.offer(entry);
    }

    private void processQueue() {
        while (running || !logQueue.isEmpty()) {
            try {
                AuditEntry entry = logQueue.poll(100, TimeUnit.MILLISECONDS);
                if (entry != null) {
                    synchronized (this) {
                        entries.add(entry);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        AuditEntry remaining;
        while ((remaining = logQueue.poll()) != null) {
            synchronized (this) {
                entries.add(remaining);
            }
        }
    }

    public synchronized List<AuditEntry> getAll() {
        return new ArrayList<>(entries);
    }

    public synchronized List<AuditEntry> getByPerformer(String performer) {
        return entries.stream()
                .filter(e -> e.performer().equalsIgnoreCase(performer))
                .collect(Collectors.toList());
    }

    public synchronized List<AuditEntry> getByAction(String action) {
        return entries.stream()
                .filter(e -> e.action().equalsIgnoreCase(action))
                .collect(Collectors.toList());
    }

    public synchronized void printLog() {
        if (entries.isEmpty()) {
            System.out.println("-+ No audit entries.");
            return;
        }

        System.out.printf("-+ %-20s %-15s %-15s %-15s %-30s%n",
                "Timestamp", "Action", "Performer", "Target", "Details");
        System.out.println("-+ " + "-".repeat(95));
        for (AuditEntry e : entries) {
            System.out.printf("-+ %-20s %-15s %-15s %-15s %-30s%n",
                    e.timestamp(), e.action(), e.performer(), e.target(), e.details());
        }
        System.out.println("-+ Total: " + entries.size());
    }

    public synchronized void saveToFile(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("Timestamp | Action | Performer | Target | Details");
            writer.println("-".repeat(80));
            for (AuditEntry e : entries) {
                writer.printf("%s | %s | %s | %s | %s%n",
                        e.timestamp(), e.action(), e.performer(), e.target(), e.details());
            }
            System.out.println("-+ Audit log saved to " + filename);
        } catch (IOException e) {
            System.out.println("-+ Error saving audit log: " + e.getMessage());
        }
    }

    public int getPendingCount() {
        return logQueue.size();
    }

    public boolean isProcessorRunning() {
        return running && processorThread.isAlive();
    }

    public void shutdown() {
        running = false;
        try {
            processorThread.join(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}