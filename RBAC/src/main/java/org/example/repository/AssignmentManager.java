package org.example.repository;

import org.example.entity.*;
import org.example.assignment.*;
import org.example.filter.AssignmentFilter;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class AssignmentManager implements Repository<RoleAssignment> {

    private final Map<String, RoleAssignment> assignments = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    private final UserManager userManager;
    private final RoleManager roleManager;

    public AssignmentManager(UserManager userManager, RoleManager roleManager) {
        this.userManager = userManager;
        this.roleManager = roleManager;
    }

    @Override
    public void add(RoleAssignment item) {
        Objects.requireNonNull(item, "Assignment cannot be null");

        lock.lock();
        try {
            if (!userManager.exists(item.user().username())) {
                throw new IllegalArgumentException(
                        "User '" + item.user().username() + "' does not exist");
            }
            if (!roleManager.exists(item.role().getName())) {
                throw new IllegalArgumentException(
                        "Role '" + item.role().getName() + "' does not exist");
            }

            boolean hasActive = assignments.values().stream()
                    .anyMatch(a -> a.user().username().equals(item.user().username())
                            && a.role().getName().equals(item.role().getName())
                            && a.isActive());

            if (hasActive) {
                throw new IllegalArgumentException("User '" + item.user().username() +
                        "' already has active role '" + item.role().getName() + "'");
            }

            assignments.put(item.assignmentId(), item);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean remove(RoleAssignment item) {
        if (item == null) return false;
        lock.lock();
        try {
            return assignments.remove(item.assignmentId()) != null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Optional<RoleAssignment> findById(String id) {
        lock.lock();
        try {
            return Optional.ofNullable(assignments.get(id));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<RoleAssignment> findAll() {
        lock.lock();
        try {
            return new ArrayList<>(assignments.values());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int count() {
        lock.lock();
        try {
            return assignments.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        lock.lock();
        try {
            assignments.clear();
        } finally {
            lock.unlock();
        }
    }

    public List<RoleAssignment> findByUser(User user) {
        lock.lock();
        try {
            return assignments.values().stream()
                    .filter(a -> a.user().username().equals(user.username()))
                    .collect(Collectors.toList());
        } finally {
            lock.unlock();
        }
    }

    public List<RoleAssignment> findByRole(Role role) {
        lock.lock();
        try {
            return assignments.values().stream()
                    .filter(a -> a.role().getName().equals(role.getName()))
                    .collect(Collectors.toList());
        } finally {
            lock.unlock();
        }
    }

    public List<RoleAssignment> findByFilter(AssignmentFilter filter) {
        lock.lock();
        try {
            return assignments.values().stream()
                    .filter(filter::test)
                    .collect(Collectors.toList());
        } finally {
            lock.unlock();
        }
    }

    public List<RoleAssignment> findByFilterParallel(AssignmentFilter filter) {
        List<RoleAssignment> snapshot;
        lock.lock();
        try {
            snapshot = new ArrayList<>(assignments.values());
        } finally {
            lock.unlock();
        }
        return snapshot.parallelStream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter) {
        lock.lock();
        try {
            return assignments.values().stream()
                    .filter(filter::test)
                    .sorted(sorter)
                    .collect(Collectors.toList());
        } finally {
            lock.unlock();
        }
    }

    public List<RoleAssignment> getActiveAssignments() {
        lock.lock();
        try {
            return assignments.values().stream()
                    .filter(RoleAssignment::isActive)
                    .collect(Collectors.toList());
        } finally {
            lock.unlock();
        }
    }

    public List<RoleAssignment> getExpiredAssignments() {
        lock.lock();
        try {
            return assignments.values().stream()
                    .filter(a -> !a.isActive())
                    .collect(Collectors.toList());
        } finally {
            lock.unlock();
        }
    }

    public boolean userHasRole(User user, Role role) {
        lock.lock();
        try {
            return assignments.values().stream()
                    .anyMatch(a -> a.user().username().equals(user.username())
                            && a.role().getName().equals(role.getName())
                            && a.isActive());
        } finally {
            lock.unlock();
        }
    }

    public boolean userHasPermission(User user, String permissionName, String resource) {
        return getUserPermissions(user).stream()
                .anyMatch(p -> p.name().equalsIgnoreCase(permissionName) &&
                        p.resource().equalsIgnoreCase(resource));
    }

    public Set<Permission> getUserPermissions(User user) {
        lock.lock();
        try {
            return assignments.values().stream()
                    .filter(a -> a.user().username().equals(user.username()) && a.isActive())
                    .flatMap(a -> a.role().getPermissions().stream())
                    .collect(Collectors.toSet());
        } finally {
            lock.unlock();
        }
    }

    public void revokeAssignment(String assignmentId) {
        lock.lock();
        try {
            RoleAssignment assignment = assignments.get(assignmentId);
            if (assignment == null) {
                throw new IllegalArgumentException(
                        "Assignment '" + assignmentId + "' not found");
            }
            if (assignment instanceof PermanentAssignment perm) {
                perm.revoke();
            } else if (assignment instanceof TemporaryAssignment) {
                assignments.remove(assignmentId);
            }
        } finally {
            lock.unlock();
        }
    }

    public void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
        lock.lock();
        try {
            RoleAssignment assignment = assignments.get(assignmentId);
            if (assignment == null) {
                throw new IllegalArgumentException(
                        "Assignment '" + assignmentId + "' not found");
            }
            if (!(assignment instanceof TemporaryAssignment temp)) {
                throw new IllegalArgumentException(
                        "Only temporary assignments can be extended");
            }
            temp.extend(newExpirationDate);
        } finally {
            lock.unlock();
        }
    }
}