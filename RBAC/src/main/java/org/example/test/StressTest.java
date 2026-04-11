package org.example.test;

import org.example.entity.*;
import org.example.assignment.*;
import org.example.system.RBACSystem;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class StressTest {

    private final int threadCount;
    private final int iterationsPerThread;

    public StressTest() {
        this(8, 100);
    }

    public StressTest(int threadCount, int iterationsPerThread) {
        this.threadCount          = threadCount;
        this.iterationsPerThread  = iterationsPerThread;
    }

    public StressTestResult run() throws InterruptedException {
        System.out.println("-+ [StressTest] Starting: "
                + threadCount + " threads x " + iterationsPerThread + " iterations");

        RBACSystem system = new RBACSystem();
        system.initialize();
        seedInitialData(system);

        StressTestStats stats = new StressTestStats();

        ExecutorService executor = Executors.newFixedThreadPool(threadCount, r -> {
            Thread t = new Thread(r);
            t.setName("StressWorker-" + t.getId());
            return t;
        });

        long startMs = System.currentTimeMillis();

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            StressTestWorker worker = new StressTestWorker(i, system, iterationsPerThread, stats);
            futures.add(executor.submit(worker));
        }

        for (Future<?> future : futures) {
            try {
                future.get(60, TimeUnit.SECONDS);
            } catch (ExecutionException e) {
                stats.recordError("Worker crashed: " + e.getCause().getMessage());
            } catch (TimeoutException e) {
                stats.recordError("Worker timed out");
            }
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        long durationMs = System.currentTimeMillis() - startMs;

        Thread.sleep(200);

        StressTestResult result = buildResult(system, stats, durationMs);

        system.shutdown();
        return result;
    }

    private void seedInitialData(RBACSystem system) {
        String[] roleNames = {"Tester", "Developer", "Analyst", "Support"};
        for (String name : roleNames) {
            try {
                Role role = new Role(name, name + " role for stress test");
                role.addPermission(new Permission("READ", "data", "Read data"));
                system.getRoleManager().add(role);
            } catch (IllegalArgumentException ignored) {}
        }

        for (int i = 0; i < 5; i++) {
            try {
                User user = new User("seed_user" + i, "Seed User " + i, "seed" + i + "@test.com");
                system.getUserManager().add(user);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    private StressTestResult buildResult(RBACSystem system, StressTestStats stats, long durationMs) {
        int finalUserCount       = system.getUserManager().count();
        int finalRoleCount       = system.getRoleManager().count();
        int finalAssignmentCount = system.getAssignmentManager().count();

        boolean hasDuplicates = checkDuplicates(system);
        boolean integrityOk   = checkIntegrity(system);

        return new StressTestResult(
                threadCount,
                iterationsPerThread,
                durationMs,
                stats,
                finalUserCount,
                finalRoleCount,
                finalAssignmentCount,
                hasDuplicates,
                integrityOk
        );
    }

    private boolean checkDuplicates(RBACSystem system) {
        List<User> users = system.getUserManager().findAll();
        long distinctUsernames = users.stream()
                .map(User::username)
                .distinct()
                .count();
        if (distinctUsernames != users.size()) {
            System.out.println("-+ [StressTest] DUPLICATE USERS DETECTED! "
                    + users.size() + " users, " + distinctUsernames + " distinct");
            return true;
        }

        List<RoleAssignment> active = system.getAssignmentManager().getActiveAssignments();
        Map<String, Long> countByUserRole = active.stream()
                .collect(Collectors.groupingBy(
                        a -> a.user().username() + "|" + a.role().getName(),
                        Collectors.counting()
                ));

        boolean dupAssignment = countByUserRole.values().stream().anyMatch(c -> c > 1);
        if (dupAssignment) {
            System.out.println("-+ [StressTest] DUPLICATE ACTIVE ASSIGNMENTS DETECTED!");
            countByUserRole.entrySet().stream()
                    .filter(e -> e.getValue() > 1)
                    .forEach(e -> System.out.println("-+   " + e.getKey() + " -> " + e.getValue()));
            return true;
        }

        return false;
    }

    private boolean checkIntegrity(RBACSystem system) {
        List<RoleAssignment> all = system.getAssignmentManager().findAll();

        for (RoleAssignment a : all) {
            if (!system.getUserManager().exists(a.user().username())) {
                System.out.println("-+ [StressTest] INTEGRITY: orphan assignment, user not found: "
                        + a.user().username());
                return false;
            }
            if (!system.getRoleManager().exists(a.role().getName())) {
                System.out.println("-+ [StressTest] INTEGRITY: orphan assignment, role not found: "
                        + a.role().getName());
                return false;
            }
        }

        return true;
    }
}
