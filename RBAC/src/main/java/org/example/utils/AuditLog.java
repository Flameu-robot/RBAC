package org.example.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AuditLog {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final List<AuditEntry> entries = new ArrayList<>();

    public void log(String action, String performer, String target, String details) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        entries.add(new AuditEntry(timestamp, action, performer, target, details));
    }

    public List<AuditEntry> getAll() {
        return new ArrayList<>(entries);
    }

    public List<AuditEntry> getByPerformer(String performer) {
        return entries.stream()
                .filter(e -> e.performer().equalsIgnoreCase(performer))
                .collect(Collectors.toList());
    }

    public List<AuditEntry> getByAction(String action) {
        return entries.stream()
                .filter(e -> e.action().equalsIgnoreCase(action))
                .collect(Collectors.toList());
    }

    public void printLog() {
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

    public void saveToFile(String filename) {
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
}

