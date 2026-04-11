package org.example.test;

import org.example.assignment.*;
import org.example.entity.*;
import org.example.filter.*;
import org.example.repository.*;
import org.example.system.RBACSystem;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class StressTestWorker implements Runnable {

    private final int workerId;
    private final RBACSystem system;
    private final int iterations;
    private final StressTestStats stats;

    public StressTestWorker(int workerId, RBACSystem system, int iterations, StressTestStats stats) {
        this.workerId = workerId;
        this.system = system;
        this.iterations = iterations;
        this.stats = stats;
    }

    @Override
    public void run() {
        for (int i = 0; i < iterations; i++) {
            try {
                int action = ThreadLocalRandom.current().nextInt(5);
                switch (action) {
                    case 0 -> doCreateUser(i);
                    case 1 -> doUpdateUser();
                    case 2 -> doAssignRole(i);
                    case 3 -> doFilterUsers();
                    case 4 -> doSearchAssignments();
                }
            } catch (Exception e) {
                stats.recordError(e.getMessage());
            }
        }
    }

    private void doCreateUser(int iteration) {
        String username = "worker" + workerId + "_user" + iteration;
        String email = username + "@test.com";
        try {
            User user = new User(username, "Worker " + workerId + " User " + iteration, email);
            system.getUserManager().add(user);
            stats.recordUserCreated();
            system.getAuditLog().logAsync("CREATE_USER", "worker-" + workerId, username, "Created by stress test");
        } catch (IllegalArgumentException e) {
            stats.recordDuplicateSkipped();
        }
    }

    private void doUpdateUser() {
        List<User> users = system.getUserManager().findAll();
        if (users.isEmpty()) return;

        User user = users.get(ThreadLocalRandom.current().nextInt(users.size()));
        try {
            system.getUserManager().update(
                    user.username(),
                    "Updated by worker " + workerId,
                    user.email()
            );
            stats.recordUserUpdated();
        } catch (IllegalArgumentException e) {
            stats.recordError("Update failed: " + e.getMessage());
        }
    }

    private void doAssignRole(int iteration) {
        List<User> users = system.getUserManager().findAll();
        List<Role> roles = system.getRoleManager().findAll();
        if (users.isEmpty() || roles.isEmpty()) return;

        User user = users.get(ThreadLocalRandom.current().nextInt(users.size()));
        Role role = roles.get(ThreadLocalRandom.current().nextInt(roles.size()));

        try {
            AssignmentMetadata meta = AssignmentMetadata.now(
                    "worker-" + workerId, "Stress test assignment");
            PermanentAssignment assignment = new PermanentAssignment(user, role, meta);
            system.getAssignmentManager().add(assignment);
            stats.recordAssignmentCreated();
        } catch (IllegalArgumentException e) {
            stats.recordDuplicateSkipped();
        }
    }

    private void doFilterUsers() {
        UserFilter filter = UserFilters.byEmailDomain("test.com");
        List<User> result = system.getUserManager().findByFilterParallel(filter);
        stats.recordFilterExecuted();
        stats.recordUsersFound(result.size());
    }

    private void doSearchAssignments() {
        AssignmentFilter filter = AssignmentFilters.activeOnly();
        List<RoleAssignment> result = system.getAssignmentManager().findByFilterParallel(filter);
        stats.recordFilterExecuted();
        stats.recordAssignmentsFound(result.size());
    }
}
