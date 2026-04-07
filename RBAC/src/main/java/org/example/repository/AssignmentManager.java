package org.example.repository;

import org.example.entity.*;
import org.example.assignment.*;
import org.example.filter.AssignmentFilter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AssignmentManager implements Repository<RoleAssignment> {

    private final Map<String, RoleAssignment> assignments = new ConcurrentHashMap<>();

    private final UserManager userManager;
    private final RoleManager roleManager;

    public AssignmentManager(UserManager userManager, RoleManager roleManager) {
        this.userManager = userManager;
        this.roleManager = roleManager;
    }

    @Override
    public synchronized void add(RoleAssignment item) {
        Objects.requireNonNull(item, "Assignment cannot be null");

        if (!userManager.exists(item.user().username())) {
            throw new IllegalArgumentException("User '" + item.user().username() + "' does not exist");
        }
        if (!roleManager.exists(item.role().getName())) {
            throw new IllegalArgumentException("Role '" + item.role().getName() + "' does not exist");
        }

        boolean hasActiveAssignment = assignments.values().stream()
                .anyMatch(a -> a.user().equals(item.user())
                        && a.role().equals(item.role())
                        && a.isActive());

        if (hasActiveAssignment) {
            throw new IllegalArgumentException("User '" + item.user().username() +
                    "' already has active role '" + item.role().getName() + "'");
        }

        assignments.put(item.assignmentId(), item);
    }

    @Override
    public boolean remove(RoleAssignment item) {
        if (item == null) return false;
        return assignments.remove(item.assignmentId()) != null;
    }

    @Override
    public Optional<RoleAssignment> findById(String id) {
        return Optional.ofNullable(assignments.get(id));
    }

    @Override
    public List<RoleAssignment> findAll() {
        return new ArrayList<>(assignments.values());
    }

    @Override
    public int count() {
        return assignments.size();
    }

    @Override
    public void clear() {
        assignments.clear();
    }

    public List<RoleAssignment> findByUser(User user) {
        return assignments.values().stream()
                .filter(a -> a.user().equals(user))
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByRole(Role role) {
        return assignments.values().stream()
                .filter(a -> a.role().equals(role))
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByFilter(AssignmentFilter filter) {
        return assignments.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter) {
        return assignments.values().stream()
                .filter(filter::test)
                .sorted(sorter)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> getActiveAssignments() {
        return assignments.values().stream()
                .filter(RoleAssignment::isActive)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> getExpiredAssignments() {
        return assignments.values().stream()
                .filter(a -> !a.isActive())
                .collect(Collectors.toList());
    }

    public boolean userHasRole(User user, Role role) {
        return assignments.values().stream()
                .anyMatch(a -> a.user().equals(user) && a.role().equals(role) && a.isActive());
    }

    public boolean userHasPermission(User user, String permissionName, String resource) {
        return getUserPermissions(user).stream()
                .anyMatch(p -> p.name().equalsIgnoreCase(permissionName) &&
                        p.resource().equalsIgnoreCase(resource));
    }

    public Set<Permission> getUserPermissions(User user) {
        return assignments.values().stream()
                .filter(a -> a.user().equals(user) && a.isActive())
                .flatMap(a -> a.role().getPermissions().stream())
                .collect(Collectors.toSet());
    }

    public synchronized void revokeAssignment(String assignmentId) {
        RoleAssignment assignment = assignments.get(assignmentId);
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment '" + assignmentId + "' not found");
        }

        if (assignment instanceof PermanentAssignment perm) {
            perm.revoke();
        } else if (assignment instanceof TemporaryAssignment temp) {
            assignments.remove(assignmentId);
        }
    }

    public synchronized void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
        RoleAssignment assignment = assignments.get(assignmentId);
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment '" + assignmentId + "' not found");
        }
        if (!(assignment instanceof TemporaryAssignment temp)) {
            throw new IllegalArgumentException("Only temporary assignments can be extended");
        }
        temp.extend(newExpirationDate);
    }
}