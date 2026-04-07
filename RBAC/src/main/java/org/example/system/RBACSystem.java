package org.example.system;

import org.example.entity.*;
import org.example.repository.*;
import org.example.assignment.*;
import org.example.utils.AuditLog;
import org.example.utils.BackgroundExecutor;

import java.util.*;
import java.util.stream.Collectors;

public class RBACSystem {

    private final UserManager userManager;
    private final RoleManager roleManager;
    private final AssignmentManager assignmentManager;
    private final BackgroundExecutor backgroundExecutor;
    private final AuditLog auditLog;
    private String currentUser;

    public RBACSystem() {
        this.userManager = new UserManager();
        this.roleManager = new RoleManager();
        this.assignmentManager = new AssignmentManager(userManager, roleManager);
        this.backgroundExecutor = new BackgroundExecutor(4);
        this.auditLog = new AuditLog();
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public RoleManager getRoleManager() {
        return roleManager;
    }

    public AssignmentManager getAssignmentManager() {
        return assignmentManager;
    }

    public BackgroundExecutor getBackgroundExecutor() {
        return backgroundExecutor;
    }

    public AuditLog getAuditLog() {
        return auditLog;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(String username) {
        this.currentUser = username;
    }

    public void initialize() {
        Permission readUsers = new Permission("READ", "users", "Read user data");
        Permission writeUsers = new Permission("WRITE", "users", "Modify user data");
        Permission deleteUsers = new Permission("DELETE", "users", "Delete users");

        Permission readRoles = new Permission("READ", "roles", "Read role data");
        Permission writeRoles = new Permission("WRITE", "roles", "Modify role data");
        Permission deleteRoles = new Permission("DELETE", "roles", "Delete roles");

        Permission readReports = new Permission("READ", "reports", "Read reports");
        Permission writeReports = new Permission("WRITE", "reports", "Create and edit reports");
        Permission deleteReports = new Permission("DELETE", "reports", "Delete reports");

        Role admin = new Role("Admin", "Full system access");
        admin.addPermission(readUsers);
        admin.addPermission(writeUsers);
        admin.addPermission(deleteUsers);
        admin.addPermission(readRoles);
        admin.addPermission(writeRoles);
        admin.addPermission(deleteRoles);
        admin.addPermission(readReports);
        admin.addPermission(writeReports);
        admin.addPermission(deleteReports);
        roleManager.add(admin);

        Role manager = new Role("Manager", "Management access");
        manager.addPermission(readUsers);
        manager.addPermission(writeUsers);
        manager.addPermission(readRoles);
        manager.addPermission(readReports);
        manager.addPermission(writeReports);
        roleManager.add(manager);

        Role viewer = new Role("Viewer", "Read-only access");
        viewer.addPermission(readUsers);
        viewer.addPermission(readRoles);
        viewer.addPermission(readReports);
        roleManager.add(viewer);

        User adminUser = new User("admin", "System Administrator", "admin@system.com");
        userManager.add(adminUser);
        currentUser = "admin";

        AssignmentMetadata metadata = AssignmentMetadata.now("system", "Initial setup");
        PermanentAssignment assignment = new PermanentAssignment(adminUser, admin, metadata);
        assignmentManager.add(assignment);

        auditLog.logAsync("INIT", "system", "system", "System initialized with default data");
    }

    public String generateStatistics() {
        StringBuilder sb = new StringBuilder();

        int totalUsers = userManager.count();
        int totalRoles = roleManager.count();
        int totalAssignments = assignmentManager.count();
        List<RoleAssignment> active = assignmentManager.getActiveAssignments();
        List<RoleAssignment> expired = assignmentManager.getExpiredAssignments();
        int activeCount = active.size();
        int expiredCount = expired.size();

        double avgRoles = totalUsers > 0
                ? (double) activeCount / totalUsers
                : 0.0;

        sb.append("-+ System Statistics\n");
        sb.append("-+ Users:       ").append(totalUsers).append("\n");
        sb.append("-+ Roles:       ").append(totalRoles).append("\n");
        sb.append("-+ Assignments: ").append(totalAssignments)
                .append(" (active: ").append(activeCount)
                .append(", expired: ").append(expiredCount).append(")\n");
        sb.append("-+ Avg roles per user: ")
                .append(String.format("%.2f", avgRoles)).append("\n");
        sb.append("-+\n");
        sb.append("-+ Top-3 popular roles:\n");

        Map<String, Long> roleCounts = active.stream()
                .collect(Collectors.groupingBy(
                        a -> a.role().getName(),
                        Collectors.counting()
                ));

        List<Map.Entry<String, Long>> top3 = roleCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .toList();

        if (top3.isEmpty()) {
            sb.append("-+   No active assignments\n");
        } else {
            int rank = 1;
            for (Map.Entry<String, Long> entry : top3) {
                sb.append("-+   ").append(rank++).append(". ")
                        .append(entry.getKey())
                        .append(" — ").append(entry.getValue())
                        .append(" assignment(s)\n");
            }
        }

        return sb.toString();
    }

    public void shutdown() {
        auditLog.logAsync("SHUTDOWN", currentUser != null ? currentUser : "system", "system", "System shutdown");
        auditLog.shutdown();
        backgroundExecutor.shutdown();
    }
}