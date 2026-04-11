package org.example.test;

public class StressTestResult {

    private final int threadCount;
    private final int iterationsPerThread;
    private final long durationMs;
    private final StressTestStats stats;
    private final int finalUserCount;
    private final int finalRoleCount;
    private final int finalAssignmentCount;
    private final boolean hasDuplicates;
    private final boolean integrityOk;

    public StressTestResult(
            int threadCount,
            int iterationsPerThread,
            long durationMs,
            StressTestStats stats,
            int finalUserCount,
            int finalRoleCount,
            int finalAssignmentCount,
            boolean hasDuplicates,
            boolean integrityOk
    ) {
        this.threadCount         = threadCount;
        this.iterationsPerThread = iterationsPerThread;
        this.durationMs          = durationMs;
        this.stats               = stats;
        this.finalUserCount      = finalUserCount;
        this.finalRoleCount      = finalRoleCount;
        this.finalAssignmentCount = finalAssignmentCount;
        this.hasDuplicates       = hasDuplicates;
        this.integrityOk         = integrityOk;
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Stress Test Result ===\n\n");

        sb.append("--- Configuration ---\n");
        sb.append(String.format("  Threads:             %d%n", threadCount));
        sb.append(String.format("  Iterations/thread:   %d%n", iterationsPerThread));
        sb.append(String.format("  Total operations:    %d%n", threadCount * iterationsPerThread));
        sb.append(String.format("  Duration:            %d ms%n", durationMs));
        sb.append(String.format("  Throughput:          %.0f ops/sec%n",
                durationMs > 0 ? (threadCount * iterationsPerThread * 1000.0 / durationMs) : 0));

        sb.append("\n--- Operations ---\n");
        sb.append(String.format("  Users created:       %d%n", stats.getUsersCreated()));
        sb.append(String.format("  Users updated:       %d%n", stats.getUsersUpdated()));
        sb.append(String.format("  Assignments created: %d%n", stats.getAssignmentsCreated()));
        sb.append(String.format("  Filters executed:    %d%n", stats.getFiltersExecuted()));
        sb.append(String.format("  Duplicates skipped:  %d%n", stats.getDuplicatesSkipped()));
        sb.append(String.format("  Errors:              %d%n", stats.getErrorsCount()));

        sb.append("\n--- Final State ---\n");
        sb.append(String.format("  Users in system:     %d%n", finalUserCount));
        sb.append(String.format("  Roles in system:     %d%n", finalRoleCount));
        sb.append(String.format("  Assignments total:   %d%n", finalAssignmentCount));

        sb.append("\n--- Integrity Checks ---\n");
        sb.append(String.format("  Duplicates found:    %s%n", hasDuplicates ? "YES !!!" : "No"));
        sb.append(String.format("  Integrity OK:        %s%n", integrityOk ? "YES" : "FAILED !!!"));

        if (!stats.getErrors().isEmpty()) {
            sb.append("\n--- Errors (first 50) ---\n");
            for (String err : stats.getErrors()) {
                sb.append("  ! ").append(err).append("\n");
            }
        }

        sb.append("\n=== ").append(integrityOk && !hasDuplicates ? "PASSED" : "FAILED").append(" ===\n");
        return sb.toString();
    }

    public boolean isPassed() {
        return integrityOk && !hasDuplicates;
    }
}
