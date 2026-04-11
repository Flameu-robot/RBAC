package org.example.utils;

import org.example.assignment.RoleAssignment;
import org.example.assignment.TemporaryAssignment;
import org.example.repository.AssignmentManager;
import org.example.system.RBACSystem;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ScheduledTaskService {

    private final ScheduledExecutorService scheduler;
    private final RBACSystem system;
    private final AuditLog auditLog;

    private ScheduledFuture<?> cleanupTask;
    private ScheduledFuture<?> statsTask;

    private final AtomicInteger totalExpiredCleaned = new AtomicInteger(0);
    private final AtomicInteger totalStatsReports   = new AtomicInteger(0);

    public ScheduledTaskService(RBACSystem system) {
        this.system   = system;
        this.auditLog = system.getAuditLog();
        this.scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("Scheduler-" + t.getId());
            return t;
        });
    }

    public void startCleanupTask(int cleanupIntervalSec) {
        if (cleanupTask != null && !cleanupTask.isDone()) {
            System.out.println("-+ [Scheduler] Cleanup task already running.");
            return;
        }

        cleanupTask = scheduler.scheduleAtFixedRate(
                this::runCleanup,
                cleanupIntervalSec,
                cleanupIntervalSec,
                TimeUnit.SECONDS
        );

        System.out.println("-+ [Scheduler] Cleanup task started (every "
                + cleanupIntervalSec + "s).");
    }

    public void startStatsTask(int statsIntervalSec) {
        if (statsTask != null && !statsTask.isDone()) {
            System.out.println("-+ [Scheduler] Stats task already running.");
            return;
        }

        statsTask = scheduler.scheduleAtFixedRate(
                this::runStatsReport,
                statsIntervalSec,
                statsIntervalSec,
                TimeUnit.SECONDS
        );

        System.out.println("-+ [Scheduler] Stats task started (every "
                + statsIntervalSec + "s).");
    }

    public void startAll(int intervalSec) {
        startCleanupTask(intervalSec);
        startStatsTask(intervalSec);
    }

    public void stopCleanupTask() {
        if (cleanupTask != null) {
            cleanupTask.cancel(false);
            System.out.println("-+ [Scheduler] Cleanup task stopped.");
        }
    }

    public void stopStatsTask() {
        if (statsTask != null) {
            statsTask.cancel(false);
            System.out.println("-+ [Scheduler] Stats task stopped.");
        }
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("-+ [Scheduler] Shutdown complete.");
    }

    private void runCleanup() {
        try {
            AssignmentManager am = system.getAssignmentManager();

            List<RoleAssignment> snapshot = am.findAll();

            List<TemporaryAssignment> expired = snapshot.stream()
                    .filter(a -> a instanceof TemporaryAssignment ta && ta.isExpired())
                    .map(a -> (TemporaryAssignment) a)
                    .toList();

            if (expired.isEmpty()) return;

            int cleaned = 0;
            for (TemporaryAssignment ta : expired) {
                try {
                    am.revokeAssignment(ta.assignmentId());
                    cleaned++;
                    auditLog.logAsync(
                            "CLEANUP", "scheduler",
                            ta.user().username(),
                            "Expired temporary assignment removed: role="
                                    + ta.role().getName()
                                    + " expiredAt=" + ta.getExpiresAt()
                    );
                } catch (IllegalArgumentException e) {
                    // уже удалено
                }
            }

            if (cleaned > 0) {
                totalExpiredCleaned.addAndGet(cleaned);
                ConsoleGuard.backgroundPrintln(
                        "-+ [Scheduler] Cleanup: removed " + cleaned + " expired assignment(s)."
                );
            }

        } catch (Exception e) {
            ConsoleGuard.backgroundPrintln("-+ [Scheduler] Cleanup error: " + e.getMessage());
        }
    }

    private void runStatsReport() {
        try {
            int userCount    = system.getUserManager().count();
            int roleCount    = system.getRoleManager().count();
            int totalAssign  = system.getAssignmentManager().count();
            List<RoleAssignment> activeList = system.getAssignmentManager().getActiveAssignments();

            long activeCount  = activeList.size();
            long expiredCount = totalAssign - activeCount;
            long tempCount    = activeList.stream()
                    .filter(a -> a instanceof TemporaryAssignment)
                    .count();
            long permCount = activeCount - tempCount;

            String report = String.format(
                    "users=%d roles=%d assignments=%d "
                            + "(active=%d perm=%d temp=%d expired=%d) "
                            + "cleanedTotal=%d",
                    userCount, roleCount, totalAssign,
                    activeCount, permCount, tempCount, expiredCount,
                    totalExpiredCleaned.get()
            );

            auditLog.logAsync("STATS", "scheduler", "system", report);
            totalStatsReports.incrementAndGet();

            ConsoleGuard.backgroundPrintln("-+ [Scheduler] Stats: " + report);

        } catch (Exception e) {
            ConsoleGuard.backgroundPrintln("-+ [Scheduler] Stats error: " + e.getMessage());
        }
    }

    public boolean isCleanupRunning() {
        return cleanupTask != null && !cleanupTask.isDone();
    }

    public boolean isStatsRunning() {
        return statsTask != null && !statsTask.isDone();
    }

    public int getTotalExpiredCleaned() {
        return totalExpiredCleaned.get();
    }

    public int getTotalStatsReports() {
        return totalStatsReports.get();
    }
}
